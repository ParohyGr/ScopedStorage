package com.parohy.scopedstorage.ui

import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import com.parohy.scopedstorage.common.navigation.mutable
import com.parohy.scopedstorage.common.navigation.saveable
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive

fun Modifier.debug(
  fill       : Boolean = false,
  stroke     : Boolean = true,
  color      : Color = Color.Red,
  fillAlpha  : Float = 0.15f,
  textColor  : Color = Color.Red,
  textSize   : Float = 45f,
  saveable   : Boolean = false,
  showSize   : Boolean = false,
  showCenter : Boolean = false
) = composed {
  var recompositionCount by if (saveable) saveable(0) else mutable(0)

  SideEffect { recompositionCount ++ }

  this.drawWithContent {
    drawContent()

    val outline = RectangleShape.createOutline(size, layoutDirection, this)

    if (fill)
      drawOutline(
        outline = outline,
        color   = color.copy(alpha = fillAlpha),
        style   = Fill)

    if (stroke)
      drawOutline(
        outline = outline,
        color   = color,
        style   = Stroke(width = 3f))

    if (showCenter) {
      drawLine(start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), color = color)
      drawLine(start = Offset(size.width / 2, 0f), end = Offset(size.width / 2, size.height), color = color)
    }

    drawIntoCanvas {
      val text = if (showSize)
        "W: ${size.width.toDp().value}, H: ${size.height.toDp().value}"
      else
        "$recompositionCount"

      it.nativeCanvas.drawText(
        text,
        10f,
        45f,
        android.graphics.Paint().apply {
          this.textSize = textSize
          this.color    = textColor.toArgb()
        })
    }
  }
}

fun Modifier.debugFps() = composed {
  val context       = LocalContext.current
  val windowService = getSystemService(context, WindowManager::class.java)!!
  val refreshRate = remember {
    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) context.display else windowService.defaultDisplay)!!.refreshRate
  }

  val showingFps = mutable(0)

  LaunchedEffect(Unit) {
    var lastFrameTime: Long? = null
    var cumulativeTime = 0L
    var fpsCounter     = 0

    while (isActive) {
      val frameTime  = awaitFrame()
      val frameDelta = lastFrameTime?.let { frameTime - it } ?: 0
      Log.e("FRAME-TIME", "${frameDelta / 1_000_000} ms")
      lastFrameTime = frameTime

      if (cumulativeTime + frameDelta > 1_000_000_000) {
        showingFps.value = fpsCounter
        Log.e("FPS", "$fpsCounter")
        fpsCounter = 0
        cumulativeTime = 0L
      } else {
        fpsCounter++
        cumulativeTime += frameDelta
      }
    }
  }

  drawWithContent {
    val color = lerp(Color.Red, Color.Green, if (showingFps.value > 0) showingFps.value / refreshRate else 1f)
    drawContent()
    drawOutline(
      outline = RectangleShape.createOutline(size, layoutDirection, this),
      color   = color,
      style   = Stroke(width = 20f))
    drawIntoCanvas {
      it.nativeCanvas.drawText(
        "${showingFps.value} fps",
        80f, 80f, android.graphics.Paint().apply { this.textSize = 45f; this.color = color.toArgb() })
    }
  }
}