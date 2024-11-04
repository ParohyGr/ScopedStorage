package com.parohy.scopedstorage.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.parohy.scopedstorage.R
import com.parohy.scopedstorage.common.navigation.CenteredColumn

@Composable
fun ButtonsScreen(title: String, content: @Composable ColumnScope.() -> Unit) {
  CenteredColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Text(text = title, style = MaterialTheme.typography.headlineMedium)
    CenteredColumn(modifier = Modifier.padding(top = 48.dp), content = content)
  }
}

@Composable
fun SSButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
  Button(onClick = onClick, enabled = enabled) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium)
  }
}

@Composable
fun EndScreen(title: String, content: @Composable () -> Unit) {
  CenteredColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Text(text = title, style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(48.dp))
    content()
  }
}