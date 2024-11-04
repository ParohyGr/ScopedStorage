package com.parohy.scopedstorage.ui.load.video

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun WhichVideoPublic(
  goToVideos: () -> Unit,
  goToDownloads: () -> Unit,
  goToCustom: () -> Unit
) {
  ButtonsScreen(title = "Videa alebo chcem si vybrat") {
    SSButton(text = "Videos", onClick = goToVideos)
    SSButton(text = "Downloads", onClick = goToDownloads)
    SSButton(text = "Custom", onClick = goToCustom)
  }
}