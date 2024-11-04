package com.parohy.scopedstorage.ui.move

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.SSButton

private data class SourceAndDest(val sourceUri: Uri, val destinationDirUri: Uri)
private fun Context.moveExactFileToExactLocation(sourceFileName: String, sourceFileMimeType: String, destinationDirName: String): SourceAndDest? {

  // URI kolekcie, s ktorou pracujem
  val collectionUri = collectionByMimeType(sourceFileMimeType)

  // Najdem menovany subor
  val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME)
  val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
  val selectionArgs = arrayOf(sourceFileName)
  val sourceUri: Uri? =
    try {
      contentResolver.query(collectionUri, projection, selection, selectionArgs, null)?.use { cursor ->
        println("Found: ${cursor.count}")
        if (cursor.moveToFirst()) {
          println("Found: ${cursor.getString(1)}")
          val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
          Uri.withAppendedPath(collectionUri, id.toString())
        } else
          null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }


  // Subor som nenasiel
  if (sourceUri == null)
    return null

  val destinationContentValues = ContentValues().apply {
    // Nastavim meno suboru
    put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFileName)
    // Nastavim MIME typ
    put(MediaStore.MediaColumns.MIME_TYPE, sourceFileMimeType)

    // Nastavim cestu k suboru
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      put(MediaStore.MediaColumns.RELATIVE_PATH, "$destinationDirName/")
    else
      put(MediaStore.MediaColumns.DATA, "$destinationDirName/")
  }

  val destinationUri = contentResolver.insert(MediaStore.Files.getContentUri("external"), destinationContentValues)

  // Nepodarilo sa vlozit
  if (destinationUri == null)
    return null

  contentResolver.openInputStream(sourceUri).use { input ->
    if (input == null) // debilne nullable vsade och
      return null
    contentResolver.openOutputStream(destinationUri).use { output ->
      if (output == null)
        return null

      val buffer = ByteArray(1024)
      var length: Int
      while (input.read(buffer).also { length = it } > 0) {
        output.write(buffer, 0, length)
      }
    }
  }

  // Vymazem povodny subor
  contentResolver.delete(sourceUri, null, null)

  return SourceAndDest(sourceUri, destinationUri)
}

/*Vrat mi URI kolekcie, s ktorou chem pracovat na zaklade mimeType*/
private fun collectionByMimeType(mimeType: String): Uri = when {
  mimeType.startsWith("image") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
  mimeType.startsWith("video") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
  mimeType.startsWith("audio") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
  mimeType.startsWith("text") || mimeType == "application/pdf" ->
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      getDocumentsCollectionSDK29()
    else
      getDocumentsCollectionSDK28()
  else -> MediaStore.Files.getContentUri("external")
}

private val String.isDocumentFile: Boolean
  get() = startsWith("text") || this == "application/pdf"

/*region Vrat mi URI kolekcie DOCUMENTS podla SDK*/
@RequiresApi(Build.VERSION_CODES.Q)
private fun getDocumentsCollectionSDK29(): Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
private fun getDocumentsCollectionSDK28(): Uri {
  val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
  // POZOR! Je potrebne skontrolovat ci existuje
  if (!dir.exists())
    dir.mkdirs()
  return MediaStore.Files.getContentUri("external")
}
/*endregion*/

@Composable
fun MoveFilePublic() {
  val activity = LocalContext.current as MoveActivity
  EndScreen(title = "Presuvam subor na verejnom ulozisku") {
    val fileUris = mutable<SourceAndDest?>(null)

    CenteredColumn {
      if (fileUris.value == null) {
        val what = mutable("IMG_SS_1730193425105.jpg")
        val type = mutable("images/jpeg")
        val where = mutable("Presuvatko")

        TextField(value = what.value, onValueChange = { what.value = it }, label = { Text("Viem co") })
        TextField(value = type.value, onValueChange = { type.value = it }, label = { Text("MimeType") })
        TextField(value = where.value, onValueChange = { where.value = it }, label = { Text("Viem kam (priecinok)") })

        /*
        * Funguje len ak pracujem s Obrazkami, Videami, Audio, Dokumentami alebo Downlaods
        * Vkladat subory do uplne vlastnych priecinkov nie je dovolene z bezpecnostnych dovodov.
        *  - Ak chcem vlozit do vlastneho priecinka, musim pouzit SAF ACTION_OPEN_DOCUMENT_TREE
        *  - Ak chcem vlozit do vlastneho priecinka, ktory je vnoreny z niektorych povolenych (napriklad Obrazky), vtedy mozem priamo definovat URI
        * */
        SSButton(text = "Viem nazov a viem kam", enabled = what.value.isNotBlank() && where.value.isNotBlank() && type.value.isNotBlank()) {
          fileUris.value = activity.moveExactFileToExactLocation(what.value, type.value, where.value)
        }

        SSButton(text = "Viem nazov a neviem kam", enabled = what.value.isNotBlank() && type.value.isNotBlank()) {}
        SSButton(text = "Neviem nazov a viem kam", enabled = where.value.isNotBlank()) {}
        SSButton(text = "Neviem nazov a neviem kam") {}
      } else {
        Text(text = "Z ${fileUris.value!!.sourceUri}")
        Text(text = "do")
        Text(text = "${fileUris.value!!.destinationDirUri}")
      }
    }
  }
}