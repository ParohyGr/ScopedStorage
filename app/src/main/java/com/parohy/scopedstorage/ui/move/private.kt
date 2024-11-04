package com.parohy.scopedstorage.ui.move

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.EndScreen
import com.parohy.scopedstorage.ui.SSButton
import java.io.File

/*
* Kedze pracujeme cisto iba s internym uloziskom aplikacie
* a zaroven presuvame subory priamo v nasej app,
* nebudeme pouzivat FileProvider.
* FileProvider by bolo vhodne pouzit, ak by sme chceli
* dany subor z privatneho uloziska zdielat externe s inou aplikaciou
* alebo s inym zariadenim.
* */
private fun moveFile(
  rootDir: File,
  sourceFileName: String,
  destinationDirName: String
): Uri? {
  val sourceFile = File(rootDir, sourceFileName)

  // Subor neexistuje, todo: handle error
  if (!sourceFile.exists())
    return null

  val destination = File(rootDir, destinationDirName)

  // Ak neexistuje, vytvorime
  if (!destination.exists())
    destination.mkdirs()

  val destinationFile = File(destination, sourceFileName.substringAfterLast('/'))
  if (!destinationFile.exists())
    destinationFile.createNewFile()

  sourceFile.outputStream().use { input ->
    destinationFile.inputStream().use { output ->
      output.copyTo(input)
    }
  }

  sourceFile.delete()

  return Uri.fromFile(destinationFile)
}


@Composable
fun MoveFilePrivate() {
  val context = LocalContext.current
  EndScreen(title = "Presunut subor na privatnom ulozisku") {
    val sourceFileUri = mutable<Uri?>(null)
    val destinationFileUri = mutable<Uri?>(null)

    val onClick = {
      val rootDir = context.filesDir
      val sourceFileName = "obrazky/obrazok.jpg"
      val destinationDirName = "obrazky_copy"

      destinationFileUri.value = moveFile(rootDir, sourceFileName, destinationDirName)
      sourceFileUri.value = Uri.fromFile(File(rootDir, sourceFileName))
    }

    BackHandler(sourceFileUri.value != null || destinationFileUri.value != null) {
      sourceFileUri.value = null
      destinationFileUri.value = null
    }

    CenteredColumn {
      sourceFileUri.value?.also {
        Text(text = "Zdrojovy subor: \n$it")
      }
      Spacer(modifier = Modifier.height(24.dp))
      destinationFileUri.value?.also {
        Text(text = "Destinacny subor: \n$it")
      }
    }

    if (sourceFileUri.value == null || destinationFileUri.value == null) {
      SSButton(text = "Presunut obrazky/obrazok.jpg -> obrazky_copy/obrazok.jpg", onClick = onClick)
    }
  }
}