package com.parohy.scopedstorage.ui.load.document

import androidx.compose.runtime.Composable
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun WhichDocumentPublicSave(
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

@Composable
fun WhichDocumentPublicLoad(
  goToDocuments: () -> Unit,
  goToDownloads: () -> Unit,
  goToCustom: () -> Unit,
  goToCustomMultiple: () -> Unit
) {
  ButtonsScreen(title = "Dokumenty alebo chcem si vybrat") {
    SSButton(text = "Documents", onClick = goToDocuments)
    SSButton(text = "Downloads", onClick = goToDownloads)
    SSButton(text = "Custom", onClick = goToCustom)
    SSButton(text = "Custom viacero naraz", onClick = goToCustomMultiple)
  }
}