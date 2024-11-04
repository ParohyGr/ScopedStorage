package com.parohy.scopedstorage.ui.load.gallery

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Text(text = "TODO:", modifier = Modifier.padding(top = 24.dp))
    SSButton(text = "Picker", onClick = goToPicker)
  }
}
