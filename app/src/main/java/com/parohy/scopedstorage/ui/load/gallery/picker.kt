package com.parohy.scopedstorage.ui.load.gallery

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
fun LoadGalleryFromPickerPublic() {
  val activity = LocalContext.current as LoadActivity
  EndScreen(title = "Nacitaj galeriu pomocou pickeru") {
    val images = mutable<List<Uri>>(emptyList())

    BackHandler(images.value.isNotEmpty()) {
      images.value = emptyList()
    }

    if (images.value.isEmpty()) {
      SSButton(text = "Pick single image or video") {
        activity.pickSingleImageOrVideo {
          if (it != null)
            images.value = listOf(it)
        }
      }
      SSButton(text = "Pick multiple image or video") {
        activity.pickMultipleImageOrVideo {
          images.value = it
        }
      }
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