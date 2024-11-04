package com.parohy.scopedstorage.ui.save.document

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.*
import com.parohy.scopedstorage.ui.save.SaveActivity
import kotlinx.coroutines.sync.Mutex
import kotlin.math.sqrt

@Composable
fun SaveDocumentPublicDocuments() {
  EndScreen(title = "Uloz dokument do Documents") {
    val context = LocalContext.current as SaveActivity
    val documentUri = mutable<Uri?>(null)

    val onClick = {
      context.createDocumentInDocuments {
        documentUri.value = it
      }
    }

    BackHandler(documentUri.value != null) {
      documentUri.value = null
    }

    CenteredColumn {
      documentUri.value?.also { uri ->
        Text(text = uri.toString())
        BoxWithConstraints {
          val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
          val height = (width * sqrt(2f)).toInt()

          val mutex = remember { Mutex() }
          val renderer by pdfRendererState(uri, mutex) { /*onError*/ }
          val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
          LazyColumn(Modifier.fillMaxSize()) {
            pdfFilePages(uri, width, height, pageCount, mutex, renderer)
          }
        }
      } ?: run {
        SSButton(text = "Uloz dokument", onClick = onClick)
      }
    }
  }
}