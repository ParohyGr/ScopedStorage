package shared

import com.parohy.scopedstorage.navigation.Screen

fun NavController.navigate(destination: Screen) =
  setNewBackstack(
    items  = backstack.value.items + BackstackItem(destination),
    action = NavAction.Navigate)

fun NavController.navigate(destinations: List<Screen>) =
  setNewBackstack(
    items  = backstack.value.items + destinations.map { BackstackItem(it) },
    action = NavAction.Navigate)

fun NavController.pop(): Unit? =
  // toto je napisane tak debilne lebo compose compiler (1.3.1) vybuchuje ak je navratovy typ Unit
  backstack.value.items.takeIf { it.isNotEmpty() }?.let {
    setNewBackstack(
      items  = backstack.value.items.dropLast(1),
      action = NavAction.Pop)
  }

fun NavController.replaceLast(newDestination: Screen) {
  if (backstack.value.items.isNotEmpty())
    setNewBackstack(
      items  = backstack.value.items.dropLast(1) + BackstackItem(newDestination),
      action = NavAction.Replace)
}

inline fun NavController.popUpTo(orElse: () -> Unit = {}, predicate: (Screen) -> Boolean) {
  val entryIndex = backstack.value.items.indexOfLast { predicate(it.destination) }
  if (entryIndex >= 0)
    setNewBackstack(
      items  = backstack.value.items.subList(0, entryIndex + 1),
      action = NavAction.Pop)
  else
    orElse()
}

fun NavController.switchTab(destination: Screen) {
  val entryIndex = backstack.value.items.indexOfLast { it.destination == destination }
  if (entryIndex >= 0) {
    setNewBackstack(
      action = NavAction.Navigate,
      items  = if (entryIndex == backstack.value.items.lastIndex) { backstack.value.items }
      else backstack.value.items.toMutableList().also { it.add(it.removeAt(entryIndex)) })
  } else
    navigate(destination)
}

fun NavController.replaceAll(screens: List<Screen>) =
  setNewBackstack(
    items  = screens.map { BackstackItem(it) },
    action = NavAction.Replace)

//inline fun <reified T: Screen> NavController.returnTo(drop: Int = 1, modify: T.() -> T): Boolean =
//  if(backstack.value.items.getOrNull(backstack.value.items.size - drop - 1)?.destination is T) {
//    val popnuty  = backstack.value.items.dropLast(drop)
//    val posledna = popnuty.last()
//    val nova     = (posledna.destination as T).modify()
//    setNewBackstack(
//      action = NavAction.Pop,
//      items  = popnuty.dropLast(1) + BackstackItem(nova, posledna.id))
//    true
//  } else
//    false
//
//fun NavController.moveToTop(predicate: (Screen) -> Boolean): Boolean {
//  val entryIndex = backstack.value.items.indexOfFirst { predicate(it.destination) }
//  return if (entryIndex >= 0) {
//    setNewBackstack(
//      items = if (entryIndex == backstack.value.items.lastIndex) {
//        backstack.value.items
//      } else {
//        backstack.value.items.toMutableList().also {
//          val entry = it.removeAt(entryIndex)
//          it.add(entry)
//        }
//      },
//      action = NavAction.Navigate)
//    true
//  } else {
//    false
//  }
//}