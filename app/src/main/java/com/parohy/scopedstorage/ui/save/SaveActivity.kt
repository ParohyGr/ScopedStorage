package com.parohy.scopedstorage.ui.save

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.parohy.scopedstorage.ui.save.document.createPdfFile

/*Kedy pouzit MediaStore a kedy SAF
* MediaStore
*  - praca s media subormi: Obrazky, Video, Audio a Dokumenty
*  - integorvany ScopedStorage - pri pouziti NETREBA riesit WRITE_EXTERNAL_STORAGE
*  - kompatabilita so starsou verziou Androidu
*  - automaticke skenovanie medii
* SAF
*  - vhodne na pracu so subormi, ktore neukladame na vseobecne miesta
*  - kompatabilita so starsou verziou Androidu
*  - moznost pristupu k suborom na vzdialenych uloziskach
*  - persistencia read/write prav uri suboru
* */

private fun Context.isGranted(permission: String): Boolean =
  ActivityCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED

open class SaveActivity: ComponentActivity() {
  /*region request permission*/
  private var _onPermissionGranted: (() -> Unit)? = null
  private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) //TODO: Handluj ak zakaze
      _onPermissionGranted?.invoke()
    _onPermissionGranted = null
  }

  fun requestPermission(permission: String, onGranted: () -> Unit) {
    _onPermissionGranted = onGranted
    requestPermission.launch(permission)
  }
  /*endregion*/

  /*region request multiplePermission*/
  private var _onMultiplePermissionGranted: (() -> Unit)? = null
  private val requestMultiplePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
    if (granted.values.all { it }) //TODO: Handluj ak zakaze niektoru
      _onMultiplePermissionGranted?.invoke()
    _onMultiplePermissionGranted = null
  }

  fun requestMultiplePermissions(vararg permissions: String, onGranted: () -> Unit) {
    _onMultiplePermissionGranted = onGranted
    requestMultiplePermission.launch(permissions.toList().toTypedArray())
  }
  /*endregion*/

  /*region Camera*/
  private var _onCameraFinished: ((Boolean) -> Unit)? = null
  private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
    _onCameraFinished?.invoke(it)
  }
  private val takeVideo = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
    _onCameraFinished?.invoke(it)
  }

  private fun capturePhoto(toUri: Uri, onResult: (Uri?) -> Unit) {
    _onCameraFinished = {
      onResult(if (it) toUri else null)
    }
    takePicture.launch(toUri)
  }

  private fun captureVideo(toUri: Uri, onResult: (Uri?) -> Unit) {
    _onCameraFinished = {
      onResult(if (it) toUri else null)
    }
    takeVideo.launch(toUri)
  }
  /*endregion*/

  private var _onFileCreated: ((Uri?) -> Unit)? = null

  /*region Odfotim a ulozim na URI*/
  fun capturePhotoAndStoreToUri(uri: Uri, onResult: (Uri?) -> Unit) {
    if (isGranted(android.Manifest.permission.CAMERA))
      capturePhoto(uri, onResult)
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        capturePhoto(uri, onResult)
      }
  }
  /*endregion*/

  /*region Nakamerujema ulozim na URI*/
  fun captureVideoAndStoreToUri(uri: Uri, onResult: (Uri?) -> Unit) {
    if (isGranted(android.Manifest.permission.CAMERA))
      captureVideo(uri, onResult)
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        captureVideo(uri, onResult)
      }
  }
  /*endregion*/

  /*region Odfotim a ulozim do Pictures*/
  private fun pictureUriInsidePictures(fileName: String): Uri? {
    val contentValues = ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
      put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    val collection: Uri =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
      } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        // POZOR! Je potrebne skontrolovat ci existuje
        if (!dir.exists())
          dir.mkdirs()

        val filePath = "${dir.absolutePath}/$fileName"

        contentValues.put(MediaStore.Images.Media.DATA, filePath)
        MediaStore.Images.Media.getContentUri("external")
      }

    return contentResolver.insert(collection, contentValues)
  }

  fun capturePhotoAndStoreToPictures(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = pictureUriInsidePictures("IMG_SS_${System.currentTimeMillis()}.jpg")
      Log.i("SaveActivity", "capturePhotoAndStoreToPictures: $uri")
      if (uri != null) //TODO: Handluj ak null
        capturePhoto(uri, onResult) //TODO: Handluj ak sa nepodari odfotit/cancelne odfotenie
    }

    /*Pre Android 13+ nepotrebujem si pytat WRITE_EXTERNAL_STORAGE*/
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
      if (isGranted(android.Manifest.permission.CAMERA))
        block()
      else
        requestPermission(android.Manifest.permission.CAMERA) {
          block()
        }
    else
      if (isGranted(android.Manifest.permission.CAMERA) && isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        block()
      else
        requestMultiplePermissions(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
          block()
        }
  }
  /*endregion*/

  /*region Odfotim a ulozim do Downloads*/
  private fun pictureUriInsideDownloads(fileName: String): Uri? {
    val contentValues = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, fileName)
      put(MediaStore.Downloads.MIME_TYPE, "image/jpeg")
    }

    val collection: Uri =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        MediaStore.Downloads.EXTERNAL_CONTENT_URI
      } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // POZOR! Je potrebne skontrolovat ci existuje
        if (!dir.exists())
          dir.mkdirs()

        val filePath = "${dir.absolutePath}/$fileName"

        contentValues.put(MediaStore.Downloads.DATA, filePath)
        MediaStore.Files.getContentUri("external")
      }

    return contentResolver.insert(collection, contentValues)
  }

  fun capturePhotoAndStoreToDownloads(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = pictureUriInsideDownloads("IMG_SS_${System.currentTimeMillis()}.jpg")
      Log.i("SaveActivity", "capturePhotoAndStoreToDownloads: $uri")
      if (uri != null) //TODO: Handluj ak null
        capturePhoto(uri, onResult) //TODO: Handluj ak sa nepodari odfotit/cancelne odfotenie
    }

    if (isGranted(android.Manifest.permission.CAMERA))
      block()
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        block()
      }
  }
  /*endregion*/

  /*region Odfotim a ulozim podla vyberu*/
  /*Preco ma kazdy typ vlastny CreateDocument? Pozri si @Deprecated koment pre triedu CreateDocument...*/
  private val createPictureDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { uri: Uri? ->
    _onFileCreated?.invoke(uri)
    _onFileCreated = null
  }

  fun capturePhotoAndStoreToCustom(onResult: (Uri?) -> Unit) {
    val block = {
      _onFileCreated = { uri ->
        if (uri != null) // TODO: Handluj ak sa nepodari vytvorit
          capturePhoto(uri, onResult)
      }
      /*
      * Na vytvaranie suborov na konkretnych miestach pouzivame SAF. Tento uz ma integrovany Scoped Storage
      * To znamena, ze nepotrebujeme WRITE_EXTERNAL_STORAGE permission
      * */
      createPictureDocument.launch("Custom_SS_${System.currentTimeMillis()}.jpg")
    }

    if (isGranted(android.Manifest.permission.CAMERA))
      block()
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        block()
      }
  }
  /*endregion*/

  /*region Nakamerujem a ulozim do Movies*/
  private fun Context.videoUriInsideMovies(fileName: String): Uri? {
    val contentValues = ContentValues().apply {
      put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
      put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
    }

    val collection: Uri =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/")
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
      } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

        // POZOR! Je potrebne skontrolovat ci existuje
        if (!dir.exists())
          dir.mkdirs()

        val filePath = "${dir.absolutePath}/$fileName"

        contentValues.put(MediaStore.Video.Media.DATA, filePath)
        MediaStore.Video.Media.getContentUri("external")
      }

    return contentResolver.insert(collection, contentValues)
  }

  fun captureVideoAndStoreToMovies(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = videoUriInsideMovies("VIDEO_SS_${System.currentTimeMillis()}.mp4")
      Log.i("SaveActivity", "captureVideoAndStoreToMovies: $uri")
      if (uri != null) //TODO: Handluj ak null
        captureVideo(uri, onResult) //TODO: Handluj ak sa nepodari nakamerovat/cancelne nakamerovanie
    }

    /*Pre Android 13+ nepotrebujem si pytat WRITE_EXTERNAL_STORAGE*/
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
      if (isGranted(android.Manifest.permission.CAMERA))
        block()
      else
        requestPermission(android.Manifest.permission.CAMERA) {
          block()
        }
    else
      if (isGranted(android.Manifest.permission.CAMERA) && isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        block()
      else
        requestMultiplePermissions(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
          block()
        }
  }
  /*endregion*/

  /*region Nakamerujem a ulozim do Downloads*/
  private fun videoUriInsideDownloads(fileName: String): Uri? {
    val contentValues = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, fileName)
      put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
    }

    val collection: Uri =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
      } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // POZOR! Je potrebne skontrolovat ci existuje
        if (!dir.exists())
          dir.mkdirs()

        val filePath = "${dir.absolutePath}/$fileName"

        contentValues.put(MediaStore.Downloads.DATA, filePath)
        MediaStore.Files.getContentUri("external")
      }

    return contentResolver.insert(collection, contentValues)
  }

  fun captureVideoAndStoreToDownloads(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = videoUriInsideDownloads("VIDEO_SS_${System.currentTimeMillis()}.mp4")
      Log.i("SaveActivity", "captureVideoAndStoreToDownloads: $uri")
      if (uri != null) //TODO: Handluj ak null
        captureVideo(uri, onResult) //TODO: Handluj ak sa nepodari nakamerovat/cancelne nakamerovanie
    }

    if (isGranted(android.Manifest.permission.CAMERA))
      block()
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        block()
      }
  }
  /*endregion*/

  /*region Nakamerujem a ulozim podla vybery*/
  /*Preco ma kazdy typ vlastny CreateDocument? Pozri si @Deprecated koment pre triedu CreateDocument...*/
  private val createVideoDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri: Uri? ->
    _onFileCreated?.invoke(uri)
    _onFileCreated = null
  }

  fun captureVideoAndStoreToCustom(onResult: (Uri?) -> Unit) {
    val block = {
      _onFileCreated = { uri ->
        if (uri != null) // TODO: Handluj ak sa nepodari vytvorit
          captureVideo(uri, onResult)
      }
      /*
      * Na vytvaranie suborov na konkretnych miestach pouzivame SAF. Tento uz ma integrovany Scoped Storage
      * To znamena, ze nepotrebujeme WRITE_EXTERNAL_STORAGE permission
      * */
      createVideoDocument.launch("Custom_SS_${System.currentTimeMillis()}.mp4")
    }

    if (isGranted(android.Manifest.permission.CAMERA))
      block()
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        block()
      }
  }
  /*endregion*/

  /*region Vytvor dokument v Documents + Downloads*/
  private fun Context.documentUriInsideDocuments(fileName: String): Uri? {
    val contentValues = ContentValues().apply {
      put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
      put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
    }

    val collection: Uri =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
      } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        // POZOR! Je potrebne skontrolovat ci existuje
        if (!dir.exists())
          dir.mkdirs()

        val filePath = "${dir.absolutePath}/$fileName"

        contentValues.put(MediaStore.Files.FileColumns.DATA, filePath)
        MediaStore.Files.getContentUri("external")
      }

    return contentResolver.insert(collection, contentValues)
  }

  private fun Context.documentUriInsideDownloads(fileName: String): Uri? {
    val contentValues = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, fileName)
      put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
    }

    val collection: Uri =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
      } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // POZOR! Je potrebne skontrolovat ci existuje
        if (!dir.exists())
          dir.mkdirs()

        val filePath = "${dir.absolutePath}/$fileName"

        contentValues.put(MediaStore.Downloads.DATA, filePath)
        MediaStore.Files.getContentUri("external")
      }

    return contentResolver.insert(collection, contentValues)
  }

  fun createDocumentInDocuments(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = documentUriInsideDocuments("DOC_SS_${System.currentTimeMillis()}.pdf")
      Log.i("SaveActivity", "createDocumentInDocuments: $uri")
      if (uri != null) { //TODO: Handluj ak null
        createPdfFile(uri)
        onResult(uri)
      }
    }

    /*Pre Android 10+ nie je potrebne pytat si WRITE_EXTERNAL_STORAGE ak chcem zapisovat do Documents*/
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      block()
    else
      if (isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        block()
      else
        requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
          block()
        }
  }

  fun createDocumentInDownloads(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = documentUriInsideDownloads("DOC_SS_${System.currentTimeMillis()}.pdf")
      Log.i("SaveActivity", "createDocumentInDownloads: $uri")
      if (uri != null) { //TODO: Handluj ak null
        createPdfFile(uri)
        onResult(uri)
      }
    }

    /*Pre Android 10+ nie je potrebne pytat si WRITE_EXTERNAL_STORAGE ak chcem zapisovat do Downloads*/
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      block()
    else
      if (isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        block()
      else
        requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
          block()
        }
  }
  /*endregion*/

  /*region Vytvorim PDF dokument a ulozim podla vyberu*/
  /*Preco ma kazdy typ vlastny CreateDocument? Pozri si @Deprecated koment pre triedu CreateDocument...*/
  private val createPdfDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
    _onFileCreated?.invoke(uri)
    _onFileCreated = null
  }
  fun createDocument(onResult: (Uri?) -> Unit) {
    _onFileCreated = {
      if (it != null) // TODO: Handluj ak null
        createPdfFile(it)
      onResult(it)
    }
    /*
    * Na vytvaranie suborov na konkretnych miestach pouzivame SAF. Tento uz ma integrovany Scoped Storage
    * To znamena, ze nepotrebujeme WRITE_EXTERNAL_STORAGE permission
    * */
    createPdfDocument.launch("Custom_SS_${System.currentTimeMillis()}.pdf")
  }
  /*endregion*/

  /*region Vytvor vlastny subor*/
  private val createFile = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
    _onFileCreated?.invoke(uri)
    _onFileCreated = null
  }

  fun createCustomFile(onResult: (Uri?) -> Unit) {
    _onFileCreated = onResult
    createFile.launch("Custom_SS_${System.currentTimeMillis()}")
  }
  /*endregion*/
}