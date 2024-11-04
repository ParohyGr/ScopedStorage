package com.parohy.scopedstorage.ui.save.video

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.save.SaveActivity

@Composable
fun SaveVideoPublicDownloads() {
  val activity = LocalContext.current as SaveActivity
  val exoPlayer = ExoPlayer.Builder(activity).build()

  EndScreen(title = "Nakamerujem video do Downloads") {
    val videoUri = mutable<Uri?>(null)

    val onClick = {
      val uri = activity.videoUriInsideDownloads("IMG_SS_${System.currentTimeMillis()}.jpg")
      if (uri != null) //TODO: Handluj ak null
        activity.captureVideoAndStoreToUri(uri) {
          videoUri.value = it
        }
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
        Button(onClick = onClick) {
          Text("Nakameruj video")
        }
      }
    }
  }
}

private fun Context.videoUriInsideDownloads(fileName: String): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
//    put(MediaStore.Downloads.IS_PENDING, 1) TODO: Preskumat mozne vyuzitie
  }

  val collection: Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
      MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
      val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

      // POZOR! Je potrebne skontrolovat ci existuje
      if (!dir.exists())
        dir.mkdirs()

      val filePath = dir.absolutePath + fileName

      contentValues.put(MediaStore.Downloads.DATA, filePath)
      Uri.parse(filePath)
    }

  return contentResolver.insert(collection, contentValues)
}
