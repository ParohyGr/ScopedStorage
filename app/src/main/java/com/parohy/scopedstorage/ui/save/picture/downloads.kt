package com.parohy.scopedstorage.ui.save.picture

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
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
fun SavePicturePublicDownloads() {
  val activity = LocalContext.current as SaveActivity
  EndScreen(title = "Odfotim obrazok do Downloads") {
    val imageUri = mutable<Uri?>(null)

    val onClick = {
      val uri = activity.pictureUriInsideDownloads("IMG_SS_${System.currentTimeMillis()}.jpg")
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

private fun Context.pictureUriInsideDownloads(fileName: String): Uri? {
  val contentValues = ContentValues().apply {
    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
    put(MediaStore.Downloads.MIME_TYPE, "image/jpeg")
//    put(MediaStore.Downloads.IS_PENDING, 1) TODO: Preskumat mozne vyuzitie
  }

  val collection: Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
      MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
      val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

      // POZOR! Je potrebne skontrolovat ci existuje
      if (!dir.exists())
        dir.mkdirs()

      val filePath = dir.absolutePath + fileName

      contentValues.put(MediaStore.Downloads.DATA, filePath)
      Uri.parse(filePath)
    }

  return contentResolver.insert(collection, contentValues)
}
