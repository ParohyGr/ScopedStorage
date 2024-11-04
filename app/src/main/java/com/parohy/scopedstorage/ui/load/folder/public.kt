package com.parohy.scopedstorage.ui.load.folder

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parohy.scopedstorage.R
import com.parohy.scopedstorage.common.navigation.CenteredRow
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.SSButton
import com.parohy.scopedstorage.ui.load.LoadActivity

@Composable
fun LoadFolderPublic() {
  val activity = LocalContext.current as LoadActivity
  EndScreen(title = "Nacitat subory z verejneho uloziska") {
    val filesUri = mutable<List<LoadActivity.FileInfo>>(emptyList())

    if (filesUri.value.isEmpty())
      SSButton(text = "Nacitat subory") {
        activity.loadFilesFromCustomDirectory { filesUri.value = it }
      }
    else
      LazyColumn(Modifier.fillMaxSize()) {
        itemsIndexed(filesUri.value) { index, file ->
          Column {
            CenteredRow(Modifier.fillMaxWidth()) {
              Image(
                painter = painterResource(
                  when {
                    file.mimeType.startsWith("video") -> R.drawable.ic_video_placeholder
                    file.mimeType.startsWith("image") -> R.drawable.ic_image
                    file.mimeType.startsWith("text") -> R.drawable.ic_text
                    file.mimeType.startsWith("audio") -> R.drawable.ic_audio
                    file.mimeType == "application/pdf" -> R.drawable.ic_pdf
                    else -> R.drawable.ic_file
                  }
                ),
                contentDescription = "Image"
              )
              Column(Modifier.padding(start = 8.dp).weight(1f)) {
                Text(text = file.name)
                Text(text = file.uri.toString(), modifier = Modifier.padding(top = 4.dp), fontSize = 8.sp)
              }
            }

            if (index != filesUri.value.lastIndex)
              HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
          }
        }
      }
  }
}