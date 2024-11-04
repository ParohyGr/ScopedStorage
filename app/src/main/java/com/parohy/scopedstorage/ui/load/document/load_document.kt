package com.parohy.scopedstorage.ui.load.document

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun WhichDocumentPublic(
  goToDocuments: () -> Unit,
  goToDownloads: () -> Unit,
  goToCustom: () -> Unit
) {
  ButtonsScreen(title = "Dokumenty alebo chcem si vybrat") {
    SSButton(text = "Documents", onClick = goToDocuments)
    SSButton(text = "Downloads", onClick = goToDownloads)
    SSButton(text = "Custom", onClick = goToCustom)
  }
}