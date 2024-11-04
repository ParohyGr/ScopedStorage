package com.parohy.scopedstorage.ui.save.file

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.SSButton
import com.parohy.scopedstorage.ui.save.SaveActivity

@Composable
fun SaveFilePublic() {
  val activity = LocalContext.current as SaveActivity
  EndScreen(title = "Uloz subor do public") {
    val fileUri = mutable<Uri?>(null)

    val onClick = {
      activity.createCustomFile {
        fileUri.value = it
      }
    }

    BackHandler(fileUri.value != null) {
      fileUri.value = null
    }

    CenteredColumn {
      fileUri.value?.also { uri ->
        Text(text = uri.toString())
      } ?: run {
        SSButton(text = "Generate file", onClick = onClick)
      }
    }
  }
}