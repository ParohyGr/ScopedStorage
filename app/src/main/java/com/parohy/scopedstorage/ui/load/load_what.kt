package com.parohy.scopedstorage.ui.load

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun LoadWhat(
  goToPicture: () -> Unit,
  goToVideo: () -> Unit,
  goToAudio: () -> Unit,
  goToDocument: () -> Unit,
  goToGallery: () -> Unit,
  goToFile: () -> Unit,
  goToCustomDirectory: () -> Unit
) {
  ButtonsScreen(title = "Nacitat co?") {
    SSButton(text = "Obrazok", onClick = goToPicture)
    SSButton(text = "Video", onClick = goToVideo)
    SSButton(text = "Audio", onClick = goToAudio)
    SSButton(text = "Dokument", onClick = goToDocument)
    SSButton(text = "Galeria", onClick = goToGallery)
    SSButton(text = "Subor", onClick = goToFile)
    SSButton(text = "Vlastny adresar", onClick = goToCustomDirectory)
  }
}