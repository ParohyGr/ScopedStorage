package com.parohy.scopedstorage.ui.load.document

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.*
import com.parohy.scopedstorage.ui.load.LoadActivity
import kotlinx.coroutines.sync.Mutex
import kotlin.math.sqrt

@Composable
fun LoadDocumentFromPublicCustomMultiple() {
  val activity = LocalContext.current as LoadActivity
  EndScreen(title = "Viacero dokumentov") {
    val documentUri = mutable<List<Uri>>(emptyList())

    val onClick = {
      activity.openMultipleDocumentsSAF { uris ->
        documentUri.value = uris
      }
    }

    BackHandler(documentUri.value.isNotEmpty()) {
      documentUri.value = emptyList()
    }

    CenteredColumn {
      if (documentUri.value.isEmpty())
        SSButton(text = "Otvor viacero dokumentov", onClick = onClick)
      else {
        documentUri.value.forEach { uri ->
          Text(text = uri.toString())
        }

        BoxWithConstraints {
          val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
          val height = (width * sqrt(2f)).toInt()

          val mutex = remember { Mutex() }
          val renderer by pdfRendererState(documentUri.value.first(), mutex) { /*onError*/ }
          val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
          LazyColumn {
            pdfFilePages(documentUri.value.first(), width, height, pageCount, mutex, renderer)
          }
        }
      }
    }
  }
}