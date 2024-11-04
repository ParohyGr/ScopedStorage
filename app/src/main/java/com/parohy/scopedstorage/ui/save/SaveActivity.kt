package com.parohy.scopedstorage.ui.save

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

/*Kedy pouzit MediaStore a kedy SAF
* MediaStore
*  - praca s media subormi: Obrazky, Video, Audio a Dokumenty
*  - integorvany ScopedStorage - pri pouziti NETREBA riesit WRITE_EXTERNAL_STORAGE
*  - kompatabilita so starsou verziou Androidu
*  - automaticke skenovanie medii
* SAF
*  - vhodne na pracu so subormi, ktore neukladame na vseobecne miesta
*  - integorvany ScopedStorage - pri pouziti TREBA riesit WRITE_EXTERNAL_STORAGE
*  - kompatabilita so starsou verziou Androidu
*  - moznost pristupu k suborom na vzdialenych uloziskach
*  - persistencia read/write prav uri suboru
* */

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

  /*
  * Na vytvaranie suborov na konkretnych miestach pouzivame SAF. Tento uz ma integrovany Scoped Storage
  * To znamena, ze nepotrebujeme WRITE_EXTERNAL_STORAGE permission
  * */

  /*Preco ma kazdy typ vlastny CreateDocument? Pozri si @Deprecated koment pre triedu CreateDocument...*/

  /*region Odfotim a ulozim na URI*/
  fun capturePhotoAndStoreToUri(uri: Uri, onResult: (Uri?) -> Unit) {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
      capturePhoto(uri, onResult)
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        capturePhoto(uri, onResult)
      }
  }
  /*endregion*/

  /*region Nakamerujema ulozim na URI*/
  fun captureVideoAndStoreToUri(uri: Uri, onResult: (Uri?) -> Unit) {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
      captureVideo(uri, onResult)
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        captureVideo(uri, onResult)
      }
  }
  /*endregion*/

  /*region Odfotim a ulozim do Pictures*/
  private val EXPIRE_TIME = 24 * 60 * 60 // 24 hours

  private fun pictureUriInsidePictures(fileName: String): Uri? {
    val contentValues = ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
      put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

      // Tento obsah ak nebude publikovany, bude zmazany po EXPIRE_TIME
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.Images.Media.IS_PENDING, 1)
        put(MediaStore.Images.Media.DATE_EXPIRES, System.currentTimeMillis() / 1000 + EXPIRE_TIME)
      }
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

        contentValues.put(MediaStore.Files.FileColumns.DATA, filePath)
        MediaStore.Images.Media.getContentUri("external")
      }

    return contentResolver.insert(collection, contentValues)
  }

  /*
  * Pri vytvarani suboru cez ContentResolver, nastavujem dany obsah ako nepublikovany.
  * Ak uz som s editaciou hotovy, musim ho publikovat. Inak bude zmazany po EXPIRE_TIME
  * */
  private fun Uri.publishPicture() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.IS_PENDING, 0)
      }
      contentResolver.update(this, contentValues, null, null)
    }
  }

  fun capturePhotoAndStoreToPictures(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = pictureUriInsidePictures("IMG_SS_${System.currentTimeMillis()}.jpg")
      Log.i("SaveActivity", "capturePhotoAndStoreToPictures: $uri")
      if (uri != null) //TODO: Handluj ak null
        capturePhoto(uri) {
          onResult(it)
          it?.publishPicture()
        }
    }

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)
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

      // nastavujem ako editovany. Tento obsah ak nebude publikovany, bude zmazany po EXPIRE_TIME
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.Downloads.IS_PENDING, 1)
        put(MediaStore.Downloads.DATE_EXPIRES, System.currentTimeMillis() / 1000 + EXPIRE_TIME)
      }
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

  /*
* Pri vytvarani suboru cez ContentResolver, nastavujem dany obsah ako nepublikovany.
* Ak uz som s editaciou hotovy, musim ho publikovat. Inak bude zmazany po EXPIRE_TIME
* */
  private fun Uri.publishDownloads() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.IS_PENDING, 0)
      }
      contentResolver.update(this, contentValues, null, null)
    }
  }

  fun capturePhotoAndStoreToDownloads(onResult: (Uri?) -> Unit) {
    val block = {
      val uri = pictureUriInsideDownloads("IMG_SS_${System.currentTimeMillis()}.jpg")
      if (uri != null) //TODO: Handluj ak null
        capturePhoto(uri) {
          onResult(it)
          it?.publishDownloads()
        }
    }

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)
      block()
    else
      requestMultiplePermissions(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        block()
      }
  }
  /*endregion*/

  /*region Odfotim a ulozim podla vyberu*/
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
      createPictureDocument.launch("Custom_SS_${System.currentTimeMillis()}.jpg")
    }

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
      block()
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        block()
      }
  }
  /*endregion*/

  /*region Nakamerujem a ulozim podla vybery*/
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
      createVideoDocument.launch("Custom_SS_${System.currentTimeMillis()}.mp4")
    }

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
      block()
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        block()
      }
  }
  /*endregion*/

  /*region Vytvorim PDF dokument a ulozim podla vyberu*/
  private val createPdfDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
    _onFileCreated?.invoke(uri)
    _onFileCreated = null
  }
  fun createDocument(onResult: (Uri?) -> Unit) {
    _onFileCreated = onResult
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