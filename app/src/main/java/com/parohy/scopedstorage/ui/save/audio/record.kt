package com.parohy.scopedstorage.ui.save.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.File

fun Context.recorder(destinationUri: Uri): MediaRecorder {
  val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    MediaRecorder(this)
  else
    MediaRecorder()

  recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
  recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
  recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
  recorder.setOutputFile(File(destinationUri.path!!).parentFile)

  return recorder
}