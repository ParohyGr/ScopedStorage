package com.parohy.scopedstorage.common.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

@Composable
fun rememberSystemUiController(window: Window? = getWindow()): AndroidSystemUiController {
  val view = LocalView.current
  return remember(view, window) { AndroidSystemUiController(view, window) }
}

class AndroidSystemUiController(
  private val view   : View,
  private val window : Window?
) {
  private val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }

  fun setStatusBarColor(
    color     : Color,
    darkIcons : Boolean,
    transformColorForLightContent: (Color) -> Color = { Color(0f, 0f, 0f, 0.3f).compositeOver(it) }
  ) {
    windowInsetsController?.isAppearanceLightStatusBars = darkIcons

    window?.statusBarColor = when {
      darkIcons && windowInsetsController?.isAppearanceLightStatusBars != true -> transformColorForLightContent(color)
      else -> color
    }.toArgb()
  }

  fun setNavigationBarColor(
    color     : Color,
    darkIcons : Boolean,
    navigationBarContrastEnforced : Boolean = color.luminance() > 0.5f,
    transformColorForLightContent : (Color) -> Color = { Color(0f, 0f, 0f, 0.3f).compositeOver(it) }
  ) {
    windowInsetsController?.isAppearanceLightNavigationBars  = darkIcons
    if (Build.VERSION.SDK_INT >= 29) { window?.isNavigationBarContrastEnforced = navigationBarContrastEnforced }

    window?.navigationBarColor = when {
      darkIcons && windowInsetsController?.isAppearanceLightNavigationBars != true -> transformColorForLightContent(color)
      else -> color
    }.toArgb()
  }
}

@Composable
private fun getWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window ?: LocalView.current.context.findWindow()

private tailrec fun Context.findWindow(): Window? = when (this) {
  is Activity       -> window
  is ContextWrapper -> baseContext.findWindow()
  else              -> null
}