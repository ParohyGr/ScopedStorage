package com.parohy.scopedstorage.ui.save.picture

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
fun SavePicturePublicPictures() {
  val activity = LocalContext.current as SaveActivity
  EndScreen(title = "Odfotim obrazok do Pictures") {
    val imageUri = mutable<Uri?>(null)

    val onClick = {
      val uri = activity.pictureUriInsidePictures("IMG_SS_${System.currentTimeMillis()}.jpg")
      if (uri != null) //TODO: Handluj ak null
        activity.capturePhotoAndStoreToUri(uri) {
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

private fun Context.pictureUriInsidePictures(fileName: String): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")
//    put(MediaStore.Images.Media.IS_PENDING, 1) TODO: Preskumat mozne vyuzitie
  }

  return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}
