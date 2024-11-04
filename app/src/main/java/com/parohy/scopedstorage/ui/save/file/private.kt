package com.parohy.scopedstorage.ui.save.file

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.FileProvider
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.*
import com.parohy.scopedstorage.ui.save.document.createPdfFile
import kotlinx.coroutines.sync.Mutex
import java.io.File
import kotlin.math.sqrt

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
fun SaveFilePrivate() {
  EndScreen(title = "Uloz subor do privatneho uloziska") {
    val context = LocalContext.current
    val fileUri = mutable<Uri?>(null)

    val onClickWithProvider = {
      fileUri.value = context.createFileWithProvider(context.filesDir, "sobory", "subor")
    }

    val onClickNoProvider = {
      fileUri.value = createFileNoProvider(context.filesDir, "subory", "subor")
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