package com.parohy.scopedstorage.ui.save.picture

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.SSButton
import com.parohy.scopedstorage.ui.save.SaveActivity

@Composable
fun SavePicturePublicCustom() {
  val activity = LocalContext.current as SaveActivity
  EndScreen(title = "Odfotim obrazok") {
    val imageUri = mutable<Uri?>(null)

    val onClick = {
      activity.capturePhotoAndStoreToCustom {
        imageUri.value = it
      }
    }

    BackHandler(imageUri.value != null) {
      imageUri.value = null
    }

    CenteredColumn {
      imageUri.value?.also {
        Text(text = it.toString())
        Image(
          modifier = Modifier.sizeIn(100.dp, 100.dp).clickable { onClick() },
          painter = rememberAsyncImagePainter(it),
          contentDescription = "Obrazok")
      } ?: run {
        SSButton(text = "Odfotit obrazok", onClick = onClick)
      }
    }
  }
}