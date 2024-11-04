package com.parohy.scopedstorage.ui.load.gallery

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
private fun Context.loadFromPrivateWithProvider(rootDir: File, dirName: String): List<Uri> {
  val file = File(rootDir, dirName)
  return file.listFiles()?.map { FileProvider.getUriForFile(this, "com.parohy.scopedstorage.fileprovider", it) } ?: emptyList() //TODO: Handluj exception
}

/*
* MOZEM LEN V APLIKACII POUZIVAT !
* rootDir - moze byt cacheDir, filesDir, externalCacheDir, externalFilesDir
* fileName - konkretne meno suboru aj s priponou
*   - Ak obsahuje '/' tak treba escape
*   - idealne bez '/' a bielych znakov
* dirName - vnoreny priecinok v rootDir
* */
private fun loadFromPrivateNoProvider(rootDir: File, dirName: String): List<Uri> {
  val file = File(rootDir, dirName)
  return file.listFiles()?.map { Uri.fromFile(it) } ?: emptyList() //TODO: Handluj excpetion
}



/*Predpokladam, ze nacitanie z privatneho uloziska cestu poznam.*/
@Composable
fun LoadGalleryPrivate() {
  EndScreen(title = "Nacitaj galeriu z privatneho uloziska") {
    val context = LocalContext.current
    val filesUri = mutable<List<Uri>>(emptyList())

    val onClickWithProvider = {
      filesUri.value = context.loadFromPrivateWithProvider(context.filesDir, "obrazky")
    }

    val onClickNoProvider = {
      filesUri.value = loadFromPrivateNoProvider(context.filesDir, "obrazky")
    }

    BackHandler(filesUri.value.isNotEmpty()) {
      filesUri.value = emptyList()
    }

    CenteredColumn {
      if (filesUri.value.isEmpty()) {
        SSButton(text = "With Provider", onClick = onClickWithProvider)
        SSButton(text = "No Provider", onClick = onClickNoProvider)
      } else
        BoxWithConstraints {
          val h = remember { maxWidth / 3 }
          LazyColumn(Modifier.fillMaxSize()) {
            items(filesUri.value.chunked(3)) { row ->
              Row {
                row.forEach {
                  Image(
                    modifier = Modifier.height(h).weight(1f).border(1.dp, Color.Blue),
                    painter = rememberAsyncImagePainter(model = it),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Image",
                  )
                }
                if (3 - row.size > 0) {
                  repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }
    }
  }
}