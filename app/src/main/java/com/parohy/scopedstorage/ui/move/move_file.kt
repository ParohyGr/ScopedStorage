package com.parohy.scopedstorage.ui.move

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.navigation.Screen
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun MoveWhichType(
  what: Screen,
  goToPrivate: (Screen) -> Unit,
  goToPublic: (Screen) -> Unit,
  goToPrivateToPublic: (Screen) -> Unit,
  goToPublicToPrivate: (Screen) -> Unit
) {
  ButtonsScreen(title = "Koho kam?") {
    SSButton(text = "Private", onClick = { goToPrivate(what) })
    SSButton(text = "Public", onClick = { goToPublic(what) })
    SSButton(text = "Private -> Public", onClick = { goToPrivateToPublic(what) })
    SSButton(text = "Public -> Private", onClick = { goToPublicToPrivate(what) })
  }
}