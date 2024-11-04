package com.parohy.scopedstorage.ui.load.gallery

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.SSButton
import com.parohy.scopedstorage.ui.load.LoadActivity

@Composable
fun LoadGalleryFromCustomPublic() {
  val activity = LocalContext.current as LoadActivity
  EndScreen(title = "Nacitaj galeriu z vlastneho priecinka") {
    val images = mutable<List<Uri>>(emptyList())

    BackHandler(images.value.isNotEmpty()) {
      images.value = emptyList()
    }

    if (images.value.isEmpty())
      SSButton(text = "Load from directory") {
        activity.loadMediaFromCustomDirectory { images.value = it }
      }
    else
      BoxWithConstraints {
        val h = remember { maxWidth / 3 }
        LazyColumn(Modifier.fillMaxSize()) {
          items(images.value.chunked(3)) { row ->
            Row {
              row.forEach {
                Image(
                  modifier = Modifier.height(h).weight(1f).border(1.dp, Color.Blue),
                  painter = rememberAsyncImagePainter(model = it),
                  contentScale = ContentScale.Crop,
                  contentDescription = "Image",
                )
              }
              if (3 - row.size > 0) {
                repeat(3 - row.size) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
  }
}