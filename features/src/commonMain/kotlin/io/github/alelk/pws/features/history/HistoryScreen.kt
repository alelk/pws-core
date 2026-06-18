package io.github.alelk.pws.features.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.components.EmptyContent
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.components.OnTabReselected
import io.github.alelk.pws.features.components.StateCrossfade
import io.github.alelk.pws.features.components.SwipeableSongItem
import io.github.alelk.pws.features.components.confirm
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_back
import io.github.alelk.pws.features.resources.common_error_title
import io.github.alelk.pws.features.resources.common_recently
import io.github.alelk.pws.features.resources.history_clear
import io.github.alelk.pws.features.resources.history_clear_dialog_cancel
import io.github.alelk.pws.features.resources.history_clear_dialog_confirm
import io.github.alelk.pws.features.resources.history_clear_dialog_message
import io.github.alelk.pws.features.resources.history_clear_dialog_title
import io.github.alelk.pws.features.resources.history_empty_subtitle
import io.github.alelk.pws.features.resources.history_empty_title
import io.github.alelk.pws.features.resources.history_group_earlier
import io.github.alelk.pws.features.resources.history_group_this_week
import io.github.alelk.pws.features.resources.history_group_today
import io.github.alelk.pws.features.resources.history_group_yesterday
import io.github.alelk.pws.features.resources.history_loading
import io.github.alelk.pws.features.resources.nav_history
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.resources.time_days_ago
import io.github.alelk.pws.features.resources.time_hours_ago
import io.github.alelk.pws.features.resources.time_just_now
import io.github.alelk.pws.features.resources.time_minutes_ago
import io.github.alelk.pws.features.resources.time_yesterday
import io.github.alelk.pws.features.components.testTagsAsResourceId
import io.github.alelk.pws.features.theme.spacing
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

class HistoryScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<HistoryScreenModel>()
    val state by viewModel.state.collectAsState()
    val showClearDialog by viewModel.showClearDialog.collectAsState()

    HistoryContent(
      state = state,
      showClearDialog = showClearDialog,
      onClearAll = { viewModel.onEvent(HistoryEvent.ClearAll) },
      onConfirmClear = { viewModel.onEvent(HistoryEvent.ConfirmClearAll) },
      onDismissClear = { viewModel.onEvent(HistoryEvent.DismissClearDialog) },
      onRemoveItem = { viewModel.onEvent(HistoryEvent.RemoveItem(it)) },
      onRetry = viewModel::retry,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
  state: HistoryUiState,
  showClearDialog: Boolean,
  onClearAll: () -> Unit,
  onConfirmClear: () -> Unit,
  onDismissClear: () -> Unit,
  onRemoveItem: (HistoryItemUi) -> Unit,
  onRetry: () -> Unit = {},
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val navigator = LocalNavigator.currentOrThrow
  val haptic = LocalHapticFeedback.current
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  // Reselect tab — scroll to top + expand large top bar (iOS-like).
  OnTabReselected(NavDestination.History) {
    scope.launch { listState.animateScrollToItem(0) }
    scrollBehavior.state.heightOffset = 0f
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        navigationIcon = {
          // Show back only when there's something to pop.
          // This covers the "Home -> History" flow (push).
          if (navigator.canPop) {
            IconButton(onClick = { navigator.pop() }) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.common_back)
              )
            }
          }
        },
        title = {
          Text(
            text = stringResource(Res.string.nav_history),
            style = MaterialTheme.typography.headlineMedium
          )
        },
        actions = {
          IconButton(
              onClick = { navigator.push(ScreenRegistry.get(SharedScreens.Settings)) },
              modifier = Modifier.testTag("action:open-settings")
            ) {
              Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(Res.string.settings_open)
              )
            }
            if (state is HistoryUiState.Content && state.canClearAll) {
              IconButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onClearAll()
                },
                modifier = Modifier.testTag("action:clear-history")
              ) {
              Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = stringResource(Res.string.history_clear)
              )
            }
          }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
      )
    }
  ) { innerPadding ->
    StateCrossfade(state, modifier = Modifier.padding(innerPadding)) { current ->
      when (current) {
        HistoryUiState.Loading -> {
          LoadingContent(message = stringResource(Res.string.history_loading))
        }

        HistoryUiState.Empty -> {
          EmptyContent(
            icon = Icons.Outlined.History,
            title = stringResource(Res.string.history_empty_title),
            subtitle = stringResource(Res.string.history_empty_subtitle)
          )
        }

        is HistoryUiState.Content -> {
          HistoryList(
            items = current.items,
            listState = listState,
            onRemove = onRemoveItem
          )
        }

        is HistoryUiState.Error -> {
          ErrorContent(
            title = stringResource(Res.string.common_error_title),
            message = io.github.alelk.pws.features.app.rememberResolved(current.message),
            onRetry = onRetry,
          )
        }
      }
    }
  }

  // Clear confirmation dialog — destructive haptic on confirm.
  if (showClearDialog) {
    ClearHistoryDialog(
      onConfirm = {
        haptic.confirm()
        onConfirmClear()
      },
      onDismiss = onDismissClear
    )
  }
}

@OptIn(ExperimentalTime::class, ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
  items: List<HistoryItemUi>,
  modifier: Modifier = Modifier,
  listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
  onRemove: (HistoryItemUi) -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow
  val now = remember { Clock.System.now() }
  val groups = remember(items, now) { groupByDate(items, now) }

  LazyColumn(
    state = listState,
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    groups.forEach { group ->
      stickyHeader(key = "header-${group.key}") {
        HistoryGroupHeader(title = stringResource(group.titleRes))
      }
      items(
        items = group.items,
        key = { it.id }
      ) { item ->
        when (item) {
          is HistoryItemUi.BookedSong -> {
            val songScreen = rememberScreen(SharedScreens.song(item.subject.songNumberId))
            SwipeableSongItem(
              number = item.songNumber,
              title = item.songName,
              subtitle = "${item.bookDisplayName} • ${formatRelativeTime(item.viewedAt, now)}",
              onClick = { navigator.push(songScreen) },
              onDelete = { onRemove(item) }
            )
          }
          is HistoryItemUi.StandaloneSong -> {
            val songScreen = rememberScreen(SharedScreens.songById(item.subject.songId))
            SwipeableSongItem(
              number = null,
              title = item.songName,
              subtitle = formatRelativeTime(item.viewedAt, now),
              onClick = { navigator.push(songScreen) },
              onDelete = { onRemove(item) }
            )
          }
        }

        if (item != group.items.last()) {
          HorizontalDivider(
            modifier = Modifier.padding(start = 72.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          )
        }
      }
    }
  }
}

@Composable
private fun HistoryGroupHeader(title: String) {
  androidx.compose.material3.Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
  }
}

@OptIn(ExperimentalTime::class)
private data class HistoryGroup(
  val key: String,
  val titleRes: org.jetbrains.compose.resources.StringResource,
  val items: List<HistoryItemUi>,
)

/**
 * Группируем по «Сегодня / Вчера / На этой неделе / Ранее».
 * iOS-style — границы по календарным дням, не по 24-часовому окну.
 */
@OptIn(ExperimentalTime::class)
private fun groupByDate(items: List<HistoryItemUi>, now: Instant): List<HistoryGroup> {
  if (items.isEmpty()) return emptyList()
  val today = mutableListOf<HistoryItemUi>()
  val yesterday = mutableListOf<HistoryItemUi>()
  val thisWeek = mutableListOf<HistoryItemUi>()
  val earlier = mutableListOf<HistoryItemUi>()
  for (item in items) {
    val days = (now - item.viewedAt).inWholeDays
    when {
      days < 1L -> today += item
      days < 2L -> yesterday += item
      days < 7L -> thisWeek += item
      else -> earlier += item
    }
  }
  return buildList {
    if (today.isNotEmpty()) add(HistoryGroup("today", Res.string.history_group_today, today))
    if (yesterday.isNotEmpty()) add(HistoryGroup("yesterday", Res.string.history_group_yesterday, yesterday))
    if (thisWeek.isNotEmpty()) add(HistoryGroup("this_week", Res.string.history_group_this_week, thisWeek))
    if (earlier.isNotEmpty()) add(HistoryGroup("earlier", Res.string.history_group_earlier, earlier))
  }
}

@Composable
private fun ClearHistoryDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  io.github.alelk.pws.features.components.AppConfirmDialog(
    title = stringResource(Res.string.history_clear_dialog_title),
    message = stringResource(Res.string.history_clear_dialog_message),
    confirmLabel = stringResource(Res.string.history_clear_dialog_confirm),
    dismissLabel = stringResource(Res.string.history_clear_dialog_cancel),
    icon = Icons.Outlined.DeleteSweep,
    confirmButtonTestTag = "action:confirm-clear-history",
    onConfirm = onConfirm,
    onDismiss = onDismiss,
  )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun formatRelativeTime(viewedAt: Instant, now: Instant): String {
  val diff = now - viewedAt
  val minutes = diff.inWholeMinutes
  val hours = diff.inWholeHours
  val days = diff.inWholeDays
  return when {
    minutes < 1 -> stringResource(Res.string.time_just_now)
    minutes < 60 -> stringResource(Res.string.time_minutes_ago, minutes.toInt())
    hours < 24 -> stringResource(Res.string.time_hours_ago, hours.toInt())
    days < 2 -> stringResource(Res.string.time_yesterday)
    else -> stringResource(Res.string.time_days_ago, days.toInt())
  }
}
