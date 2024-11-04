package com.parohy.scopedstorage.ui.load.file

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
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
fun LoadFilePrivate() {
  EndScreen(title = "Nacitaj subor do privatneho uloziska") {
    val context = LocalContext.current
    val fileUri = mutable<Uri?>(null)

    val onClickWithProvider = {
      fileUri.value = context.loadFromPrivateWithProvider(context.filesDir, "sobory", "subor")
    }

    val onClickNoProvider = {
      fileUri.value = loadFromPrivateNoProvider(context.filesDir, "subory", "subor")
    }

    BackHandler(fileUri.value != null) {
      fileUri.value = null
    }

    CenteredColumn {
      fileUri.value?.also { uri ->
        Text(text = uri.toString())
      } ?: run {
        SSButton(text = "With Provider", onClick = onClickWithProvider)
        SSButton(text = "No Provider", onClick = onClickNoProvider)
      }
    }
  }
}