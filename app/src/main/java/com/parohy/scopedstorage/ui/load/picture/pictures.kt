package com.parohy.scopedstorage.ui.load.picture

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
import com.parohy.scopedstorage.ui.load.LoadActivity

/*
* Pre otvorenie obrazku z Pictures pouzijeme Storage Access Framework (SAF).
* SAF ma v sebe uz podporu pre ScopedStorage, takze nemusime riesit ziadne permissions.
* */
@Composable
fun LoadFromPublicPictures() {
  EndScreen(title = "Vyber obrazok z Pictures") {
    val activity = LocalContext.current as LoadActivity
    val imageUri = mutable<Uri?>(null)

    val onClick = {
      activity.openPictureSAF { pictureUri: Uri? ->
        imageUri.value = pictureUri
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
        SSButton(text = "Vyber obrazok", onClick = onClick)
      }
    }
  }
}