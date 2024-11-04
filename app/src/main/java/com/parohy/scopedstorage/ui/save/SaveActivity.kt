package com.parohy.scopedstorage.ui.save

import android.net.Uri
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

  /*Odfotim a ulozim na URI*/
  fun capturePhotoAndStoreToUri(uri: Uri, onResult: (Uri?) -> Unit) {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
      capturePhoto(uri, onResult)
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        capturePhoto(uri, onResult)
      }
  }
  /*endregion*/

  /*Nakamerujema ulozim na URI*/
  fun captureVideoAndStoreToUri(uri: Uri, onResult: (Uri?) -> Unit) {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
      captureVideo(uri, onResult)
    else
      requestPermission(android.Manifest.permission.CAMERA) {
        captureVideo(uri, onResult)
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