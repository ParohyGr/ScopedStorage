package com.parohy.scopedstorage.ui.load.video

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.parohy.scopedstorage.ui.SSButton
import com.parohy.scopedstorage.ui.load.LoadActivity

@Composable
fun LoadVideoFromPublicCustom() {
  val activity = LocalContext.current as LoadActivity
  val exoPlayer = ExoPlayer.Builder(activity).build()

  EndScreen(title = "Vyber video") {
    val videoUri = mutable<Uri?>(null)

    val onClick = {
      activity.openFileSAF { uri: Uri? ->
        videoUri.value = uri
      }
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
        SSButton(text = "Vyber video", onClick = onClick)
      }
    }
  }
}