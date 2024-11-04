package com.parohy.scopedstorage.ui.load.video

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
fun LoadVideoPrivate() {
  val context = LocalContext.current
  val exoPlayer = ExoPlayer.Builder(context).build()

  EndScreen(title = "Nacitaj video.mp4 z privatneho uloziska") {
    val videoUri = mutable<Uri?>(null)

    val onClickWithProvider = {
      videoUri.value = context.loadFromPrivateWithProvider(context.filesDir, "videa", "video.mp4")
    }

    val onClickNoProvider = {
      videoUri.value = loadFromPrivateNoProvider(context.filesDir, "videa", "video.mp4")
    }

    BackHandler(videoUri.value != null) {
      videoUri.value = null
    }

    LaunchedEffect(videoUri.value) {
      videoUri.value?.also {
        val mediaItem = MediaItem.fromUri(it)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
      }
    }

    DisposableEffect(Unit) {
      onDispose {
        exoPlayer.release()
      }
    }

    CenteredColumn {
      videoUri.value?.also {
        Text(text = it.toString())
        AndroidView(
          factory = { ctx ->
            PlayerView(ctx).apply {
              player = exoPlayer
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
        )
      } ?: run {
        SSButton(text = "With Provider", onClick = onClickWithProvider)
        SSButton(text = "No Provider", onClick = onClickNoProvider)
      }
    }
  }
}
