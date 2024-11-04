package com.parohy.scopedstorage.ui.load.document

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.*
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
fun LoadDocumentPrivate() {
  EndScreen(title = "Nacitaj document.pdf z privatneho uloziska") {
    val context = LocalContext.current
    val documentUri = mutable<Uri?>(null)

    val onClickWithProvider = {
      documentUri.value = context.loadFromPrivateWithProvider(context.filesDir, "dokumenty", "document.pdf")
    }

    val onClickNoProvider = {
      documentUri.value = loadFromPrivateNoProvider(context.filesDir, "dokumenty", "document.pdf")
    }

    BackHandler(documentUri.value != null) {
      documentUri.value = null
    }

    CenteredColumn {
      documentUri.value?.also { uri ->
        Text(text = uri.toString())
        BoxWithConstraints {
          val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
          val height = (width * sqrt(2f)).toInt()

          val mutex = remember { Mutex() }
          val renderer by pdfRendererState(uri, mutex) { /*onError*/ }
          val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
          LazyColumn(Modifier.fillMaxSize()) {
            pdfFilePages(uri, width, height, pageCount, mutex, renderer)
          }
        }
      } ?: run {
        SSButton(text = "With Provider", onClick = onClickWithProvider)
        SSButton(text = "No Provider", onClick = onClickNoProvider)
      }
    }
  }
}