package io.github.alelk.pws.features.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import io.github.alelk.pws.features.components.SwipeableSongItem
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
      onRemoveItem = { viewModel.onEvent(HistoryEvent.RemoveItem(it)) }
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
  onRemoveItem: (HistoryItemUi) -> Unit
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val navigator = LocalNavigator.currentOrThrow
  val haptic = LocalHapticFeedback.current

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
    when (state) {
      HistoryUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = stringResource(Res.string.history_loading)
        )
      }

      HistoryUiState.Empty -> {
        EmptyContent(
          modifier = Modifier.padding(innerPadding),
          icon = Icons.Outlined.History,
          title = stringResource(Res.string.history_empty_title),
          subtitle = stringResource(Res.string.history_empty_subtitle)
        )
      }

      is HistoryUiState.Content -> {
        HistoryList(
          items = state.items,
          modifier = Modifier.padding(innerPadding),
          onRemove = onRemoveItem
        )
      }

      is HistoryUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = stringResource(Res.string.common_error_title),
          message = io.github.alelk.pws.features.app.rememberResolved(state.message),
        )
      }
    }
  }

  // Clear confirmation dialog
  if (showClearDialog) {
    ClearHistoryDialog(
      onConfirm = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onConfirmClear()
      },
      onDismiss = onDismissClear
    )
  }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun HistoryList(
  items: List<HistoryItemUi>,
  modifier: Modifier = Modifier,
  onRemove: (HistoryItemUi) -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow
  val now = remember { Clock.System.now() }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = MaterialTheme.spacing.sm)
  ) {
    items(
      items = items,
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

      if (item != items.last()) {
        HorizontalDivider(
          modifier = Modifier.padding(start = 72.dp),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
      }
    }

    item {
      Spacer(Modifier.height(80.dp))
    }
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
