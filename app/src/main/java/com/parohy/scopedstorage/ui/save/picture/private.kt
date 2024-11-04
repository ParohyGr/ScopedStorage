package com.parohy.scopedstorage.ui.save.picture

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.SSButton
import com.parohy.scopedstorage.ui.save.SaveActivity
import java.io.File

/*
* VIEM ZDIELAT DANY SUBOR MIMO APLIKACIE !
* rootDir - moze byt cacheDir, filesDir, externalCacheDir, externalFilesDir
* fileName - konkretne meno suboru aj s priponou
*   - Ak obsahuje '/' tak treba escape
*   - idealne bez '/' a bielych znakov
* dirName - vnoreny priecinok v rootDir
*
* FileProvider paths:
*  nastavujem podla toho, ake rootDir pouzivam
*    easy setup cim nic nepokazim:
        <?xml version="1.0" encoding="utf-8"?>
        <paths>
            <cache-path name="app-cache" path="." />
            <files-path name="app-files" path="." />
            <external-files-path name="files" path="." />
            <external-cache-path name="external_cache" path="." />
        </paths>
*
* */
private fun Context.createFileWithProvider(rootDir: File, dirName: String, fileName: String): Uri {
  // Vytvor vnoreny priecinok, ak existuje, neudeje sa nic
  File(rootDir, dirName).mkdirs()

  val file = File(rootDir, "$dirName/$fileName")
  if (!file.exists())
    file.createNewFile()

  return FileProvider.getUriForFile(this, "com.parohy.scopedstorage.fileprovider", file) //TODO Handluj exception
}

/*
* MOZEM LEN V APLIKACII POUZIVAT !
* rootDir - moze byt cacheDir, filesDir, externalCacheDir, externalFilesDir
* fileName - konkretne meno suboru aj s priponou
*   - Ak obsahuje '/' tak treba escape
*   - idealne bez '/' a bielych znakov
* dirName - vnoreny priecinok v rootDir
* */
private fun createFileNoProvider(rootDir: File, dirName: String, fileName: String): Uri {
  // Vytvor vnoreny priecinok, ak existuje, neudeje sa nic
  File(rootDir, dirName).mkdirs()

  val file = File(rootDir, "$dirName/$fileName")
  if (!file.exists())
    file.createNewFile()

  return Uri.fromFile(file) //TODO Handluj exception
}


@Composable
fun SavePicturePrivate() {
  EndScreen(title = "Uloz obrazok.jpg do privatneho uloziska") {
    val activity = LocalContext.current as SaveActivity
    val imageUri = mutable<Uri?>(null)

    val onClickWithProvider = {
      activity.capturePhotoAndStoreToUri(activity.createFileWithProvider(activity.filesDir, "obrazky", "obrazok.jpg")) {
        imageUri.value = it
      }
    }

    val onClickNoProvider = {
      activity.capturePhotoAndStoreToUri(createFileNoProvider(activity.filesDir, "obrazky", "obrazok.jpg")) {
        imageUri.value = it
      }
    }

    BackHandler(imageUri.value != null) {
      imageUri.value = null
    }

    CenteredColumn {
      imageUri.value?.also {
        Text(text = it.toString())
        Image(
          modifier = Modifier.sizeIn(100.dp, 100.dp),
          painter = rememberAsyncImagePainter(it),
          contentDescription = "Obrazok")
      } ?: run {
        SSButton(text = "With Provider", onClick = onClickWithProvider)

        /*POZOR!!! Toto crashne aplikaciu.
        * Kedze createFileNoProvider nepouziva FileProvider, tak sa neda zdielat subor mimo aplikacie
        * co v nasom pripade znamena, ze Kamera aplikacia nevie pouzit to Uri. Nema na to prava.
        * */
        SSButton(text = "No Provider - CRASH", onClick = onClickNoProvider)
      }
    }
  }
}