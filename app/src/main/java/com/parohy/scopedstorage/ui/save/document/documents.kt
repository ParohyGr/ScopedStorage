package com.parohy.scopedstorage.ui.save.document

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import kotlinx.coroutines.sync.Mutex
import kotlin.math.sqrt

@Composable
fun SaveDocumentPublicDocuments() {EndScreen(title = "Uloz dokument do Documents") {
  val context = LocalContext.current
  val documentUri = mutable<Uri?>(null)

  val onClick = {
    with(context) {
      val uri = documentUriInsideDocuments("PDF_SS_${System.currentTimeMillis()}.pdf")
      if (uri != null) // TODO: Handluj ak null
        createPdfFile(uri)
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

/*
* Kedze pouzivame MediaStore, nepotrebujeme pytat WRITE_EXTERNAL_STORAGE
* */
private fun Context.documentUriInsideDocuments(fileName: String): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
    put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
  }

  val collection: Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
      MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
      val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

      // POZOR! Je potrebne skontrolovat ci existuje
      if (!dir.exists())
        dir.mkdirs()

      val filePath = dir.absolutePath + fileName

      contentValues.put(MediaStore.Files.FileColumns.DATA, filePath)
      MediaStore.Files.getContentUri("external")
    }

  return contentResolver.insert(collection, contentValues)
}