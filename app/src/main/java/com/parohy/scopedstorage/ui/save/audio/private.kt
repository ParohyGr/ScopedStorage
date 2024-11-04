package com.parohy.scopedstorage.ui.save.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.request.Disposable
import com.parohy.scopedstorage.common.navigation.CenteredColumn
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.ui.*
import com.parohy.scopedstorage.ui.save.SaveActivity
import java.io.File

/*
* VIEM ZDIELAT DANY SUBOR MIMO APLIKACIE !
* rootDir - moze byt cacheDir, filesDir, externalCacheDir, externalFilesDir
* fileName - konkretne meno suboru aj s priponou
*   - Ak obsahuje '/' tak treba escape
*   - idealne bez '/' a bielych znakov
* dirName - vnoreny priecinok v rootDir
*
* FileProvider paths:
*  nastavujem podla toho, ake rootDir pouzivam
*    easy setup cim nic nepokazim:
        <?xml version="1.0" encoding="utf-8"?>
        <paths>
            <cache-path name="app-cache" path="." />
            <files-path name="app-files" path="." />
            <external-files-path name="files" path="." />
            <external-cache-path name="external_cache" path="." />
        </paths>
*
* */
private fun Context.createFileWithProvider(rootDir: File, dirName: String, fileName: String): Uri {
  // Vytvor vnoreny priecinok, ak existuje, neudeje sa nic
  val dir = File(rootDir, dirName)
  if (!dir.exists())
    dir.mkdirs()

  val file = File(dir, fileName)
  if (!file.exists())
    file.createNewFile()

  return FileProvider.getUriForFile(this, "com.parohy.scopedstorage.fileprovider", file) //TODO Handluj exception
}

/*
* MOZEM LEN V APLIKACII POUZIVAT !
* rootDir - moze byt cacheDir, filesDir, externalCacheDir, externalFilesDir
* fileName - konkretne meno suboru aj s priponou
*   - Ak obsahuje '/' tak treba escape
*   - idealne bez '/' a bielych znakov
* dirName - vnoreny priecinok v rootDir
* */
private fun createFileNoProvider(rootDir: File, dirName: String, fileName: String): Uri {
  // Vytvor vnoreny priecinok, ak existuje, neudeje sa nic
  File(rootDir, dirName).mkdirs()

  val file = File(rootDir, "$dirName/$fileName")
  if (!file.exists())
    file.createNewFile()

  return Uri.fromFile(file) //TODO Handluj exception
}

// TODO: Crashuje nahravanie, zatial neviem preco
@Composable
fun SaveAudioPrivate() {
  val activity = LocalContext.current as SaveActivity
  val mediaPlayer = remember { MediaPlayer() }
  EndScreen(title = "Uloz hudbicka.3gp do privatneho uloziska") {
    val audioUri = mutable<Uri?>(null)
    val recordingType = mutable<String?>(null)

    val onClickWithProvider = {
      activity.requestPermission(android.Manifest.permission.RECORD_AUDIO) {
        recordingType.value = "YES"
      }
    }

    val onClickNoProvider = {
      activity.requestPermission(android.Manifest.permission.RECORD_AUDIO) {
        recordingType.value = "NO"
      }

    }

    BackHandler(audioUri.value != null) {
      recordingType.value = null
      audioUri.value = null
    }

    DisposableEffect(mediaPlayer) {
      onDispose { mediaPlayer.release() }
    }


    CenteredColumn {
      audioUri.value?.also { uri ->
        Text(text = uri.toString())
        val isPlaying = mutable(false)

        LaunchedEffect(isPlaying.value) {
          if (isPlaying.value)
            try {
              mediaPlayer.reset()
              mediaPlayer.setDataSource(uri.path)
              mediaPlayer.prepare()
              mediaPlayer.start()
            } catch (e: Exception) {
              e.printStackTrace()
            }
          else if (mediaPlayer.isPlaying)
            mediaPlayer.stop()
        }

        SSButton(text = if (isPlaying.value) "STOP" else "PLAY", onClick = { isPlaying.value = !isPlaying.value })
      } ?: run {
        when (recordingType.value) {
          "YES" -> {
            val isRecording = mutable(false)
            val fileUri = remember { activity.createFileWithProvider(activity.filesDir, "hudba", "hudbicka.3gp") }
            val recorder = remember { activity.recorder(fileUri) }

            SSButton(text = if (isRecording.value) "STOP" else "RECORD", onClick = {
              if (isRecording.value) {
                recorder.stop()
                audioUri.value = fileUri
              } else {
                recorder.prepare()
                recorder.start()
              }
            })
          }
          "NO" -> {
            val isRecording = mutable(false)
            val fileUri = remember { createFileNoProvider(activity.filesDir, "hudba", "hudbicka.3gp") }
            val recorder = remember { activity.recorder(fileUri) }

            SSButton(text = if (isRecording.value) "STOP" else "RECORD", onClick = {
              if (isRecording.value) {
                recorder.stop()
                audioUri.value = fileUri
              } else {
                recorder.prepare()
                recorder.start()
              }
            })
          }
          else -> {
            SSButton(text = "With Provider", onClick = onClickWithProvider)

            /*POZOR!!! Toto crashne aplikaciu.
            * Kedze createFileNoProvider nepouziva FileProvider, tak sa neda zdielat subor mimo aplikacie
            * co v nasom pripade znamena, ze Kamera aplikacia nevie pouzit to Uri. Nema na to prava.
            * */
            SSButton(text = "No Provider", onClick = onClickNoProvider)
          }
        }
      }
    }
  }
}