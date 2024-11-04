package com.parohy.scopedstorage.common.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
inline fun CenteredRow(
  modifier   : Modifier = Modifier,
  horizontal : Arrangement.Horizontal = Arrangement.Start,
  content    : @Composable RowScope.() -> Unit) = Row(modifier, horizontal, Alignment.CenterVertically, content)

@Composable
inline fun CenteredColumn(
  modifier : Modifier = Modifier,
  vertical : Arrangement.Vertical = Arrangement.Top,
  content  : @Composable ColumnScope.() -> Unit) = Column(modifier, vertical, Alignment.CenterHorizontally, content)

@Composable
inline fun CenteredBox(
  modifier : Modifier = Modifier,
  content  : @Composable BoxScope.() -> Unit) = Box(modifier, Alignment.Center, content = content)

@Composable
fun <T> mutable(init: T): MutableState<T> = remember { mutableStateOf(init) }
@Composable
fun <T> mutable(key: Any?, init: T): MutableState<T> = remember(key) { mutableStateOf(init) }

@Composable
fun <T> saveable(init: T): MutableState<T> = rememberSaveable { mutableStateOf(init) }