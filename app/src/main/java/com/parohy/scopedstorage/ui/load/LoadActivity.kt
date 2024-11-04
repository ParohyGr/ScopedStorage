package com.parohy.scopedstorage.ui.load

import android.content.*
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.parohy.scopedstorage.R
import com.parohy.scopedstorage.ui.save.SaveActivity
import java.io.File

/*Preco sa pouziva MediaStore a nepracujem s Enviroment.getExternalStoragePublicDirectory ?
* 1. Enviroment vracia priamo cestu k suboru.
*   - praca priamo so subormi
*   - mikromanazment
*   - Android 10+ obmedzene prava na cesty
*   - potrebujete pytat prava alebo aj MANAGE_EXTERNAL_STORAGE
* 2. MediaStore vracia Uri na priecinok s pozadovanym mediom
*   - konzistenica napriec android zariadeniami - jedna cesta
*   - spolurapcuje s media scanner
*   - MediaStore je scoped storage
* */

/*region SAF Screen.LoadFile.Picture.PublicStorage.Pictures*/
object OpenImageFromPictures: ActivityResultContract<Unit, Uri?>() {
  override fun createIntent(context: Context, input: Unit) =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "image/*"
      /*
      * Z mojho testovania, na novsich verziach Androidu, sa neotvoria Pictures ale posledne otvorena lokalita
      * Neviem preco to tak funguje, ale je to volovina. Vid dokumentacia DocumentsContract.EXTRA_INITIAL_URI co to na robit.
      * */
      putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}
/*endregion*/

/*region SAF Screen.LoadFile.Picture.PublicStorage.Pictures*/
object OpenImageFromDownloads: ActivityResultContract<Unit, Uri?>() {
  override fun createIntent(context: Context, input: Unit) =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "image/*"
      /*
      * Z mojho testovania, na novsich verziach Androidu, sa neotvoria Pictures ale posledne otvorena lokalita
      * Neviem preco to tak funguje, ale je to volovina. Vid dokumentacia DocumentsContract.EXTRA_INITIAL_URI co to na robit.
      * */
      val startFrom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        MediaStore.Downloads.EXTERNAL_CONTENT_URI
      else
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toUri()

      putExtra(DocumentsContract.EXTRA_INITIAL_URI, startFrom)
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}
/*endregion*/

/*region SAF Screen.LoadFile.Picture.PublicStorage.Videos*/
object OpenVideoFromVideos: ActivityResultContract<Unit, Uri?>() {
  override fun createIntent(context: Context, input: Unit) =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "video/*"
      /*
      * Z mojho testovania, na novsich verziach Androidu, sa neotvoria Videos ale posledne otvorena lokalita
      * Neviem preco to tak funguje, ale je to volovina. Vid dokumentacia DocumentsContract.EXTRA_INITIAL_URI co to na robit.
      * */
      putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}
/*endregion*/

/*region SAF Screen.LoadFile.Document.PublicStorage.Documents*/
object OpenPdfDocumentFromDocuments: ActivityResultContract<Unit, Uri?>() {
  override fun createIntent(context: Context, input: Unit) =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/pdf"
      /*
      * Z mojho testovania, na novsich verziach Androidu, sa neotvoria Videos ale posledne otvorena lokalita
      * Neviem preco to tak funguje, ale je to volovina. Vid dokumentacia DocumentsContract.EXTRA_INITIAL_URI co to na robit.
      * */
      putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Files.getContentUri("external"))
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}
/*endregion*/

open class LoadActivity: SaveActivity() {
  private var _onFileResult: ((Uri?) -> Unit)? = null

  /*region SAF Open public root directory*/
  private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

  fun openFileSAF(onResult: (Uri?) -> Unit) {
    _onFileResult = onResult
    openFile.launch(arrayOf("*/*"))
  }
  /*endregion*/

  /*region Screen.LoadFile.Picture.PublicStorage.Pictures*/
  private val openPictureContract = registerForActivityResult(OpenImageFromPictures) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

  fun openPictureSAF(onResult: (Uri?) -> Unit) {
    _onFileResult = onResult
    openPictureContract.launch(Unit)
  }
  /*endregion*/

  /*region Screen.LoadFile.Picture.PublicStorage.Pictures*/
  private val openPictureDownloadsContract = registerForActivityResult(OpenImageFromDownloads) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

  fun openPictureDownloadsSAF(onResult: (Uri?) -> Unit) {
    _onFileResult = onResult
    openPictureDownloadsContract.launch(Unit)
  }
  /*endregion*/

  /*region Screen.LoadFile.Video.PublicStorage.Videos*/
  private val openVideoContract = registerForActivityResult(OpenVideoFromVideos) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

  fun openVideoSAF(onResult: (Uri?) -> Unit) {
    _onFileResult = onResult
    openVideoContract.launch(Unit)
  }
  /*endregion*/

  /*region Screen.LoadFile.Document.PublicStorage.Documents*/
  private val openDocumentContract = registerForActivityResult(OpenPdfDocumentFromDocuments) { uri: Uri? ->
    _onFileResult?.invoke(uri)
    _onFileResult = null
  }

  fun openDocumentSAF(onResult: (Uri?) -> Unit) {
    _onFileResult = onResult
    openDocumentContract.launch(Unit)
  }
  /*endregion*/

  /*region Screen.LoadFile.Gallery.PublicStorage.Pictures*/
  /*Tako query nacita vsetky obrazky z telefonu, nie len z Pictures*/
  fun loadPicturesFromGallery(onResult: (List<Uri>) -> Unit) {
    val block: () -> List<Uri> = {
      buildList {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = contentResolver.query(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          projection,
          null,
          null,
          null
        )

        if (cursor != null && cursor.count > 0) {
          cursor.moveToFirst()
          Log.i("MediaScanner", "Found ${cursor.count} images")
          do {
            val imageId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)
            add(imageUri)
          } while (cursor.moveToNext())
          cursor.close()
        }
      }
    }

    //TODO: Pre 14+ treba handlovat READ_MEDIA_VISUAL_USER_SELECTED
    // Pre Android 13+ je potrebne READ_MEDIA_IMAGES ak chcem nacitat obrazky, ktore som nevytvoril z tejto aplikacie
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
      if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil uplny pristup ku fotkam*/
        onResult(block())
      }
      else if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil ciastocny pristup, to znamena, ze manualne volil media, ktore dovoluje zdielat s aplikaciou. Malo by to byt non-blocking,
        * cize odoslem vysledok ale popytam si opat pristup. Dodatocne pytanie si pristupu by malo byt UI/UX oddelene od hlavnej akcie, napriklad extra tlacidlo,
        * ktore by vyvolal systemovy permission na READ_MEDIA_IMAGES*/
        onResult(block())
        /*TODO: Tento call by mal byt volany dodatocne cez ine tlacidlo akciu, nie hlavnu. Ale toto je len example, ale takto to nekopiruj!*/
        requestPermission(android.Manifest.permission.READ_MEDIA_IMAGES) {
          onResult(block())
        }
      }
      else // Ak este nemam pristup, tak si ho poprosim
        requestPermission(android.Manifest.permission.READ_MEDIA_IMAGES) { // READ_MEDIA_IMAGES alebo READ_MEDIA_VIDEO ma rovnaky vysledok
          onResult(block())
        }
    // Pre Android 12 a nizsie staci READ_EXTERNAL_STORAGE
    else
      if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)
        onResult(block())
      else
        requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
          onResult(block())
        }
  }
  /*endregion*/

  /*region Screen.LoadFile.Gallery.PublicStorage.Multimedia*/
  /*Tako query nacita vsetky obrazky a videa z telefonu, nie len z Pictures a Movies*/
  fun loadMultimediaFromGallery(onResult: (List<Uri>) -> Unit) {
    val block: () -> List<Uri> = {
      // Sucasne viem nacitat iba jednu kolekciu, musim preto pre kazdy MediaStore volat query zvlast
      val images = queryMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      val videos = queryMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI).map(::createVideoThumbnail)

      (images + videos).sortedBy(MediaInfo::dateTaken).map(MediaInfo::uri)
    }

    //TODO: Pre 14+ treba handlovat READ_MEDIA_VISUAL_USER_SELECTED
    // Pre Android 13+ je potrebne READ_MEDIA_IMAGES a READ_MEDIA_VIDEO ak chcem nacitat obrazky aj videa, ktore som nevytvoril z tejto aplikacie
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
      if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil uplny pristup ku fotkam a videam. Staci, ak povoli len jedno z nich "Allow all" a ma automaticky full pristup aj k druhemu. Preto je v podmienke OR*/
        onResult(block())
      } else if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        /*User udelil ciastocny pristup, to znamena, ze manualne volil media, ktore dovoluje zdielat s aplikaciou. Malo by to byt non-blocking,
        * cize odoslem vysledok ale popytam si opat pristup. Dodatocne pytanie si pristupu by malo byt UI/UX oddelene od hlavnej akcie, napriklad extra tlacidlo,
        * ktore by vyvolal systemovy permission na READ_MEDIA_IMAGES a READ_MEDIA_VIDEO*/
        onResult(block())
        /*TODO: Tento call by mal byt volany dodatocne cez ine tlacidlo akciu, nie hlavnu. Ale toto je len example, ale takto to nekopiruj!*/
        requestMultiplePermissions(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) {
          onResult(block())
        }
      }
      else // Ak este nemam pristup, tak si ho poprosim
        requestMultiplePermissions(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) {
          onResult(block())
        }
    // Pre Android 12 a nizsie staci READ_EXTERNAL_STORAGE
    else
      if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)
        onResult(block())
      else
        requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
          onResult(block())
        }
  }

  /*Tato funkcia vytvori z videa thumbnail aby sme vedeli zobrazit na UI nieco*/
  private fun createVideoThumbnail(input: MediaInfo): MediaInfo {
    val (uri, date, mimeType) = input
    if (mimeType != "video/mp4") return input

    val file = File(cacheDir, "video_thumbnail_$date.jpg")

    // Ak uz existuje thumbnail, tak ho len nacitaj
    return if (!file.exists()) {
      // Vytvor thumbnail z videa
      val retriever = MediaMetadataRetriever()
      val thumbnail = try {
        retriever.setDataSource(this, uri)
        retriever.getFrameAtTime(1000000)
      } catch (e: Exception) {
        e.printStackTrace()
        null
      } finally {
        retriever.release()
      }

      // Cachni thumbnail a vrat Uri. Ak failne, tak vrat placeholder
      val thumbUri = thumbnail?.let { bitmap ->
        bitmap.drawVideoTriangle()
        file.outputStream().use {
          bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        Uri.fromFile(file)
      } ?: Uri.parse("android.resource://$packageName/${R.drawable.ic_video_placeholder}")

      input.copy(uri = thumbUri)
    } else
      input.copy(uri = Uri.fromFile(file))
  }

  /*Tato funkcia len nakresli PLAY na obrazok aby sme vedeli rozlisit v UI obrazok od videa*/
  private fun Bitmap.drawVideoTriangle() {
    val canvas = Canvas(this)
    val paint = Paint().apply {
      color = Color.WHITE
      style = Paint.Style.FILL
      isAntiAlias = true
    }

    val centerX = width / 2f
    val centerY = height / 2f
    val triangleSize = width / 6f

    val path = Path().apply {
      moveTo(centerX - triangleSize, centerY - triangleSize) // Left corner
      lineTo(centerX + triangleSize, centerY)               // Right corner
      lineTo(centerX - triangleSize, centerY + triangleSize) // Bottom corner
      close()  // Complete the triangle
    }
    canvas.drawPath(path, paint)
  }

  data class MediaInfo(val uri: Uri, val dateTaken: String, val mimeType: String)
  /*Tato funkcia nacita informacie z danej kolekcie URI a vrati zoznam MediaInfo
  * Tieto vysledky sa miesaju s dalsimi vysledkami z inych kolekcii, aby sme mohli zobrazit vsetky media na jednom mieste
  * Datum sa pouziva na sortovanie finalneho zoznamu
  * MimeType sa pouziva pri mapovani video/\* na obrazok
  * */
  private fun queryMedia(collectionUri: Uri): List<MediaInfo> =
    buildList {
      val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATE_TAKEN, MediaStore.MediaColumns.MIME_TYPE)
      val cursor = contentResolver.query(
        collectionUri,
        projection,
        null,
        null,
        null
      )

      if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        Log.i("MediaScanner", "Found ${cursor.count} media in $collectionUri")
        do {
          add(
            MediaInfo(
              uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))),
              dateTaken = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)),
              mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
            )
          )
        } while (cursor.moveToNext())
        cursor.close()
      }
    }
  /*endregion*/

  /*region Screen.LoadFile.Gallery.PublicStorage.Custom*/
  /*Pri tejto metode si volime manulane priecinok cez SAF
  * Permission na citanie si pytat nemusime, lebo SAF si pyta permission automaticky
  * ! Tato permission nie je persistujuca, takze ak sa obrazovka zavrie, tak sa permission zrusi !
  * Kedze SAF nam vracia DocumentUri, tak musime pouzit DocumentsContract na ziskanie informacii o suboroch
  * */
  private val openDocumentTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { collectionUri: Uri? ->
    _onFileResult?.invoke(collectionUri)
    _onFileResult = null
  }

  fun loadMediaFromCustomDirectory(onResult: (List<Uri>) -> Unit) {
    _onFileResult = { documentTreeUri ->
      if (documentTreeUri != null) {
        /*Ak by som chcel si pamatat zvoleny priecinok, URI by stratilo read a write prava po zavreti aplikacie,
        * user by musel cely proces zopakovat.
        *
        * Touto funkciou nastavit prava na persistujuce, takze po zavreti aplikacie, prava nezmiznu.
        * contentResolver.takePersistableUriPermission(documentTreeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        * Taketo uri viem prepouzivat v aplikacii bez znovu vyziaddania prav.
        * */

        val list = queryCustomMedia(documentTreeUri).map(::createVideoThumbnail)
        onResult(list.map(MediaInfo::uri))
      } else
        onResult(emptyList())
    }
    openDocumentTree.launch(null)
  }

  /*Tato funkcia je velmi podobna queryMedia, len tato pouziva ine stlpce. Kedze sa nepouziva priamo ScopedStorage,
  * ale SAF, vracia nam to inu "databazu" aj ked zvolim priamo priecinok Pictures.
  * 1. Musime si vytvorit kolekiu pomocou DocumentsContract.buildChildDocumentsUriUsingTree
  * 2. Querickujeme DocumentsContract.COLUMN_DOCUMENT_ID a DocumentsContract.COLUMN_MIME_TYPE
  * 3. Kazdy subor vo vysledku je dalsi documentTreeUri, cize musime pre kazdy pouzit DocumentsContract.buildChildDocumentsUriUsingTree
  * */
  private fun queryCustomMedia(documentTreeUri: Uri): List<MediaInfo> =
    buildList {
      require(DocumentsContract.isTreeUri(documentTreeUri)) { "documentTreeUri must be a DocumentTree uri" }
      val collectionUri = DocumentsContract.buildChildDocumentsUriUsingTree(documentTreeUri, DocumentsContract.getTreeDocumentId(documentTreeUri))
      val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE)
      val cursor = contentResolver.query(
        collectionUri,
        projection,
        null,
        null,
        null
      )

      if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        Log.i("MediaScanner", "Found ${cursor.count} media in $collectionUri")
        do {
          val documentId = cursor.getString(0)
          val documentUri = DocumentsContract.buildChildDocumentsUriUsingTree(collectionUri, documentId)
          add(
            MediaInfo(
              uri = documentUri,
              dateTaken = "0", // DocumentsContract neobsahuje sltpec DATE_TAKEN, len DATE_MODIFIED
              mimeType = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
            )
          )
        } while (cursor.moveToNext())
        cursor.close()
      }
    }
  /*endregion*/

  /*region Screen.LoadFile.Folder.PublicStorage*/
  fun loadFilesFromCustomDirectory(onResult: (List<FileInfo>) -> Unit) {
    _onFileResult = { documentTreeUri ->
      if (documentTreeUri != null) {
        /*Ak by som chcel si pamatat zvoleny priecinok, URI by stratilo read a write prava po zavreti aplikacie,
        * user by musel cely proces zopakovat.
        *
        * Touto funkciou nastavit prava na persistujuce, takze po zavreti aplikacie, prava nezmiznu.
        * contentResolver.takePersistableUriPermission(documentTreeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        * Taketo uri viem prepouzivat v aplikacii bez znovu vyziaddania prav.
        * */

        val list = queryFiles(documentTreeUri)
        onResult(list)
      } else
        onResult(emptyList())
    }
    openDocumentTree.launch(null)
  }

  data class FileInfo(val uri: Uri, val name: String, val mimeType: String)
  private fun queryFiles(documentTreeUri: Uri): List<FileInfo> =
    buildList {
      require(DocumentsContract.isTreeUri(documentTreeUri)) { "documentTreeUri must be a DocumentTree uri" }
      val collectionUri = DocumentsContract.buildChildDocumentsUriUsingTree(documentTreeUri, DocumentsContract.getTreeDocumentId(documentTreeUri))
      val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
      val cursor = contentResolver.query(
        collectionUri,
        projection,
        null,
        null,
        null
      )

      if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        Log.i("MediaScanner", "Found ${cursor.count} media in $collectionUri")
        do {
          val documentId = cursor.getString(0)
          val documentUri = DocumentsContract.buildChildDocumentsUriUsingTree(collectionUri, documentId)
          add(
            FileInfo(
              uri = documentUri,
              name = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)),
              mimeType = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
            )
          )
        } while (cursor.moveToNext())
        cursor.close()
      }
    }
  /*endregion*/
}
