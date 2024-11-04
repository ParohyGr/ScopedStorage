package com.parohy.scopedstorage.ui.save.video

import android.content.ContentValues
import android.content.Context
import android.net.Uri
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
fun SaveVideoPublicVideos() {
  val activity = LocalContext.current as SaveActivity
  val exoPlayer = ExoPlayer.Builder(activity).build()

  EndScreen(title = "Nakamerujem video do Movies") {
    val videoUri = mutable<Uri?>(null)

    val onClick = {
      activity.captureVideoAndStoreToMovies {
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