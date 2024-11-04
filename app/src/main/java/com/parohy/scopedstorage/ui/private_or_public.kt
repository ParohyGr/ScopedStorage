package com.parohy.scopedstorage.ui

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.navigation.Screen

@Composable
fun WhichType(
  what: Screen,
  goToPrivate: (Screen) -> Unit,
  goToPublic: (Screen) -> Unit
) {
  ButtonsScreen(title = "Privatne alebo verejne?") {
    SSButton(text = "Private", onClick = { goToPrivate(what) })
    SSButton(text = "Public", onClick = { goToPublic(what) })
  }
}