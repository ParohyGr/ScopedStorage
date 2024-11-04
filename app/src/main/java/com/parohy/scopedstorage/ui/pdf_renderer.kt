package com.parohy.scopedstorage.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.sqrt



@Composable
fun pdfRendererState(fileUri: Uri, mutex: Mutex, onError: (Throwable) -> Unit): State<PdfRenderer?> {
  val context = LocalContext.current
  val rendererScope = rememberCoroutineScope()
  return produceState<PdfRenderer?>(null, fileUri) {
    rendererScope.launch(Dispatchers.IO) {
      try {
        val fileDescriptor = context.contentResolver.openFileDescriptor(fileUri, "r")
        fileDescriptor?.let {
          value = PdfRenderer(fileDescriptor)
        }
      } catch (e: Exception) {
        e.printStackTrace()
        onError(e)
      }
    }
    awaitDispose {
      val currentRenderer = value
      rendererScope.launch(Dispatchers.IO) {
        mutex.withLock {
          currentRenderer?.close()
        }
      }
    }
  }
}

fun LazyListScope.pdfFilePages(fileUri: Uri, width: Int, height: Int, pageCount: Int, mutex: Mutex, renderer: PdfRenderer?) {
  items(
    count = pageCount,
    key   = { index -> "$fileUri-$index" }
  ) { index ->
    val imageLoader       = LocalContext.current.imageLoader
    val imageLoadingScope = rememberCoroutineScope()
    val context           = LocalContext.current

    val cacheKey = MemoryCache.Key("$fileUri-$index")
    var bitmap by remember { mutableStateOf(imageLoader.memoryCache?.get(cacheKey)?.bitmap) }

    if (bitmap == null) {
      DisposableEffect(fileUri, index) {
        val job = imageLoadingScope.launch(Dispatchers.IO) {
          val destinationBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
          mutex.withLock {
            if (!coroutineContext.isActive) return@launch
            try {
              renderer?.let {
                it.openPage(index).use { page ->
                  page.render(
                    destinationBitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
              }
            } catch (e: Exception) {
              //Just catch and return in case the renderer is being closed
              return@launch
            }
          }
          bitmap = destinationBitmap
        }
        onDispose {
          job.cancel()
        }
      }

      Box(Modifier.background(Color.White).aspectRatio(1f / sqrt(2f)).fillMaxWidth())
    } else {
      val request = remember {
        ImageRequest.Builder(context)
          .size(width, height)
          .memoryCacheKey(cacheKey)
          .data(bitmap)
          .build()
      }

      Image(
        modifier           = Modifier.background(Color.White).aspectRatio(1f / sqrt(2f)).fillMaxWidth(),
        contentScale       = ContentScale.Fit,
        painter            = rememberAsyncImagePainter(request),
        contentDescription = "Page ${index + 1} of $pageCount"
      )
    }
  }
}