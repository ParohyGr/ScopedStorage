package com.parohy.scopedstorage.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.parohy.scopedstorage.R
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.ui.ButtonsScreen
import com.parohy.scopedstorage.ui.SSButton

@Composable
fun Home(
  goToLoad: () -> Unit,
  goToSave: () -> Unit,
  goToDelete: () -> Unit,
  goToMove: () -> Unit
) {
  ButtonsScreen(title = stringResource(R.string.app_name)) {
    SSButton(text = "Chcem nacitat subor", onClick = goToLoad)
    SSButton(text = "Chcem ulozit subor", onClick = goToSave)

    Spacer(modifier = Modifier.height(32.dp))
    Text(text = "Este to nie je done")

    SSButton(text = "Chcem zmenit subor", onClick = {})
    SSButton(text = "Chcem vymazat subor", onClick = goToDelete)
    SSButton(text = "Chcem presunut subor", onClick = goToMove)
    SSButton(text = "Dalsie divne veci", onClick = {})
  }
}