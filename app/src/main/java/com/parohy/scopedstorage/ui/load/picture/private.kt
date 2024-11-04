package com.parohy.scopedstorage.ui.load.picture

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
private fun Context.loadFromPrivateWithProvider(rootDir: File, dirName: String, fileName: String): Uri {
  val file = File(rootDir, "$dirName/$fileName")
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
private fun loadFromPrivateNoProvider(rootDir: File, dirName: String, fileName: String): Uri {
  val file = File(rootDir, "$dirName/$fileName")
  return Uri.fromFile(file) //TODO Handluj exception
}


/*Predpokladam, ze nacitanie z privatneho uloziska cestu poznam.*/
@Composable
fun LoadPicturePrivate() {
  EndScreen(title = "Nacitaj obrazok.jpg z privatneho uloziska") {
    val context = LocalContext.current
    val imageUri = mutable<Uri?>(null)

    val onClickWithProvider = {
      imageUri.value = context.loadFromPrivateWithProvider(context.filesDir, "obrazky", "obrazok.jpg")
    }

    val onClickNoProvider = {
      imageUri.value = loadFromPrivateNoProvider(context.filesDir, "obrazky", "obrazok.jpg")
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
        SSButton(text = "No Provider", onClick = onClickNoProvider)
      }
    }
  }
}