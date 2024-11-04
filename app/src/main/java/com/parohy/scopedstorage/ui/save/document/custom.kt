package com.parohy.scopedstorage.ui.save.document

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
fun SaveDocumentPublicCustom() {
  val activity = LocalContext.current as SaveActivity
  EndScreen(title = "Uloz dokument do Documents") {
    val documentUri = mutable<Uri?>(null)

    val onClick = {
      activity.createDocument { uri ->
        if (uri != null) //TODO: Handluj ak null
          activity.createPdfFile(uri)
        documentUri.value = uri
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