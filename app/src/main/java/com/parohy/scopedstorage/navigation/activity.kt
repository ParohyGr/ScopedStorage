@file:OptIn(ExperimentalAnimationApi::class)

package com.parohy.scopedstorage.navigation

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.parohy.scopedstorage.common.navigation.rememberSystemUiController
import com.parohy.scopedstorage.ui.load.LoadActivity
import com.parohy.scopedstorage.ui.move.MoveActivity
import shared.*

private const val EXTRA_NAVIGATION = "navigation"

open class NavigationActivity: MoveActivity() {
  private var navigation = NavController(mutableStateOf(Backstack(emptyList())))

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(EXTRA_NAVIGATION, navigation.backstack.value)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    val restoredNavigation = savedInstanceState?.getParcelable<Backstack>(EXTRA_NAVIGATION)?.let { NavController(backstack = mutableStateOf(it)) }
    navigation = restoredNavigation ?: NavController(mutableStateOf(Backstack(listOf(BackstackItem(Screen.Home)))))

    super.onCreate(savedInstanceState)

    setContent {
      BackHandler(navigation.backstack.value.items.size > 1) {
        navigation.pop()
      }

      val systemUi = rememberSystemUiController()
      val focus    = LocalFocusManager.current

      AnimatedNavHost(
        navigation = navigation,
        animation  = ::screenTransitions,
      ) { destination: Screen ->
        LaunchedEffect(destination) {
          focus.clearFocus()
          systemUi.setStatusBarColor(color = Color.Transparent, darkIcons = false)
        }

        Box(
          modifier = Modifier.fillMaxSize(),
          propagateMinConstraints = true
        ) {
          navigation.NavigationRouter(destination, this@NavigationActivity)

          if(this@AnimatedNavHost.transition.isRunning)
            Box(Modifier.fillMaxSize().pointerInput(Unit) {}) // Block interaction during animation
        }
      }
    }
  }
}

/**
 * Screen transition animations
 */

private fun screenTransitions(type: NavAction, from: Screen, to: Screen) =
  if (from is Bottom && to is Bottom) {
    ContentTransform(EnterTransition.None, ExitTransition.None)
  } else {
    when (type) {
      NavAction.Idle, NavAction.Replace, NavAction.Navigate ->
        scaleIn(
          animationSpec = tween(120, easing = FastOutLinearInEasing),
          initialScale = 0.7f
        ) + fadeIn() with ExitTransition.None

      NavAction.Pop ->
        scaleIn(
          animationSpec = tween(220),
          initialScale = 1.3f
        ) + fadeIn() with ExitTransition.None
    }
  }