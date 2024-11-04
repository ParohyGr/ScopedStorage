@file:OptIn(ExperimentalAnimationApi::class)

package shared

import android.os.ParcelUuid
import android.os.Parcelable
import androidx.compose.animation.*
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.parohy.scopedstorage.navigation.Screen
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

val LocalScreenId = compositionLocalOf { ParcelUuid(UUID.randomUUID()) }

enum class NavAction { Idle, Navigate, Replace, Pop  }

@Immutable
@Parcelize
data class Backstack(val items: List<BackstackItem>, val action: NavAction = NavAction.Idle): Parcelable

@Immutable
@Parcelize
data class BackstackItem(val destination: Screen, val id: ParcelUuid = ParcelUuid(UUID.randomUUID())): Parcelable

@Stable
@Parcelize
class NavController(val backstack: @RawValue MutableState<Backstack>) : Parcelable {
  var stateHolder: SaveableStateHolder? = null
  var onBackstackChange: ((backstack: Backstack) -> Unit)? = null
}

val NavController.items get() = backstack.value.items

fun NavController.setNewBackstack(items: List<BackstackItem>, action: NavAction) {
  val before = backstack.value.items.map { it.id }

  backstack.value = Backstack(items, action)
  onBackstackChange?.invoke(backstack.value)

  val after = backstack.value.items.map { it.id }

  before.filter { it !in after }.forEach { deletedId: ParcelUuid ->
    stateHolder?.removeState(deletedId)
//    removeUiInboxes(deletedId)
  }
}


@Composable
fun AnimatedNavHost(
  navigation : NavController,
  animation  : (type: NavAction, from: Screen, to: Screen) -> ContentTransform,
  router     : @Composable AnimatedVisibilityScope.(Screen) -> Unit
) {
  val backstack     = navigation.backstack.value
  val saveableState = rememberSaveableStateHolder()
  val transition    = updateTransition(backstack.items.lastOrNull(), "AnimatedNavHost")
  if(navigation.stateHolder == null) navigation.stateHolder = saveableState

  transition.AnimatedContent(
    contentKey     = { it?.id },
    transitionSpec = {
      if(initialState != null && targetState != null)
        animation(backstack.action, initialState!!.destination, targetState!!.destination)
      else
        EnterTransition.None with ExitTransition.None
    }
  ) { item: BackstackItem? ->
    if (item != null) {
      saveableState.SaveableStateProvider(item.id) { // ukladanie stavu
        CompositionLocalProvider(LocalScreenId provides item.id) { // kvoli nasim eventom
          router(item.destination)
        }
      }
    } else // empty placeholder
      Box(Modifier.fillMaxSize().background(Color.Red)) {}
  }
}