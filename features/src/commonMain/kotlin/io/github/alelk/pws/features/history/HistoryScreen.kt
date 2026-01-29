package io.github.alelk.pws.features.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.components.EmptyContent
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.SwipeableSongItem
import io.github.alelk.pws.features.theme.spacing
import kotlin.time.ExperimentalTime

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
      onDismissClear = { viewModel.onEvent(HistoryEvent.DismissClearDialog) }
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
  onDismissClear: () -> Unit
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          Text(
            text = "История",
            style = MaterialTheme.typography.headlineMedium
          )
        },
        actions = {
          if (state is HistoryUiState.Content && state.canClearAll) {
            IconButton(onClick = onClearAll) {
              Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = "Очистить историю"
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
          message = "Загрузка истории..."
        )
      }

      HistoryUiState.Empty -> {
        EmptyContent(
          modifier = Modifier.padding(innerPadding),
          icon = Icons.Outlined.History,
          title = "История пуста",
          subtitle = "Здесь будут отображаться просмотренные песни"
        )
      }

      is HistoryUiState.Content -> {
        HistoryList(
          items = state.items,
          modifier = Modifier.padding(innerPadding)
        )
      }

      is HistoryUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = "Ошибка",
          message = state.message
        )
      }
    }
  }

  // Clear confirmation dialog
  if (showClearDialog) {
    ClearHistoryDialog(
      onConfirm = onConfirmClear,
      onDismiss = onDismissClear
    )
  }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun HistoryList(
  items: List<HistoryItemUi>,
  modifier: Modifier = Modifier
) {
  val navigator = LocalNavigator.currentOrThrow

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
          val songScreen = rememberScreen(SharedScreens.Song(item.subject.songNumberId))
          SwipeableSongItem(
            number = item.songNumber,
            title = item.songName,
            subtitle = "${item.bookDisplayName} • ${formatTime(item.viewedAt.toEpochMilliseconds())}",
            onClick = { navigator.push(songScreen) }
          )
        }
        is HistoryItemUi.StandaloneSong -> {
          val songScreen = rememberScreen(SharedScreens.SongById(item.subject.songId))
          SwipeableSongItem(
            number = null,
            title = item.songName,
            subtitle = formatTime(item.viewedAt.toEpochMilliseconds()),
            onClick = { navigator.push(songScreen) }
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
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Outlined.DeleteSweep,
        contentDescription = null
      )
    },
    title = {
      Text("Очистить историю?")
    },
    text = {
      Text("Все записи истории будут удалены. Это действие нельзя отменить.")
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text("Очистить")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Отмена")
      }
    }
  )
}

private fun formatTime(timestamp: Long): String {
  // Simple relative time formatting
  // In production, use kotlinx-datetime for proper KMP time handling
  return "недавно"
}

