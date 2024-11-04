package com.parohy.scopedstorage.ui.load.picture

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun WhichPicturePublic(
  goToPictures: () -> Unit,
  goToDownloads: () -> Unit,
  goToCustom: () -> Unit
) {
  ButtonsScreen(title = "Obrazky alebo chcem si vybrat") {
    SSButton(text = "Pictures", onClick = goToPictures)
    SSButton(text = "Downloads", onClick = goToDownloads)
    SSButton(text = "Custom", onClick = goToCustom)
  }
}