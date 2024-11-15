package com.parohy.scopedstorage.ui.load.gallery

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun WhichGalleryPublic(
  goToPictures: () -> Unit,
  goToMultimedia: () -> Unit,
  goToCustom: () -> Unit,
  goToPicker: () -> Unit
) {
  ButtonsScreen(title = "Obrazky, Obrazky aj Videa alebo chcem si vybrat priecinok") {
    SSButton(text = "Pictures", onClick = goToPictures)
    SSButton(text = "Multimedia", onClick = goToMultimedia)
    SSButton(text = "Custom", onClick = goToCustom)
    SSButton(text = "Picker", onClick = goToPicker)
  }
}
