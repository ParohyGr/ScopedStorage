package com.parohy.scopedstorage.ui.save

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun SaveWhat(
  goToPicture: () -> Unit,
  goToVideo: () -> Unit,
  goToAudio: () -> Unit,
  goToDocument: () -> Unit,
  goToFile: () -> Unit,
) {
  ButtonsScreen(title = "Ulozit co?") {
    SSButton(text = "Obrazok", onClick = goToPicture)
    SSButton(text = "Video", onClick = goToVideo)
    SSButton(text = "Audio", onClick = goToAudio)
    SSButton(text = "Dokument", onClick = goToDocument)
    SSButton(text = "Subor", onClick = goToFile)
  }
}