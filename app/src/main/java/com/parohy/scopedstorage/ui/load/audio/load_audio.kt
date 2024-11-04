package com.parohy.scopedstorage.ui.load.audio

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun WhichAudioPublic(
  goToAudio: () -> Unit,
  goToCustom: () -> Unit
) {
  ButtonsScreen(title = "Hudba alebo chcem si vybrat") {
    SSButton(text = "Music", onClick = goToAudio)
    SSButton(text = "Custom", onClick = goToCustom)
  }
}