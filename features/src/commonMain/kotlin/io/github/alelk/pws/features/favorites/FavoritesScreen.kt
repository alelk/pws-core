package io.github.alelk.pws.features.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_back
import io.github.alelk.pws.features.resources.common_error_title
import io.github.alelk.pws.features.resources.favorites_empty_subtitle
import io.github.alelk.pws.features.resources.favorites_empty_title
import io.github.alelk.pws.features.resources.favorites_loading
import io.github.alelk.pws.features.resources.favorites_sort
import io.github.alelk.pws.features.resources.favorites_sort_added_date
import io.github.alelk.pws.features.resources.favorites_sort_direction
import io.github.alelk.pws.features.resources.favorites_sort_song_name
import io.github.alelk.pws.features.resources.favorites_sort_song_number
import io.github.alelk.pws.features.resources.favorites_title
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.song.detail.LocalFavoritesDisplaySettings
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

class FavoritesScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<FavoritesScreenModel>()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val displaySettings = LocalFavoritesDisplaySettings.current

    LaunchedEffect(displaySettings) {
      displaySettings?.let { viewModel.setDisplaySettings(it) }
    }

    FavoritesContent(
      state = state,
      snackbarHostState = snackbarHostState,
      onRemove = { viewModel.onEvent(FavoritesEvent.RemoveFromFavorites(it)) },
      onSortModeChange = { viewModel.onEvent(FavoritesEvent.ChangeSortMode(it)) },
      onSortDirectionToggle = { viewModel.onEvent(FavoritesEvent.ToggleSortDirection) },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
  state: FavoritesUiState,
  snackbarHostState: SnackbarHostState,
  onRemove: (FavoriteSongUi) -> Unit,
  onSortModeChange: (FavoriteSortMode) -> Unit,
  onSortDirectionToggle: () -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val navigator = LocalNavigator.currentOrThrow
  val haptic = LocalHapticFeedback.current
  var showSortDialog by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  OnTabReselected(NavDestination.Favorites) {
    scope.launch { listState.animateScrollToItem(0) }
    scrollBehavior.state.heightOffset = 0f
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        navigationIcon = {
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
            text = stringResource(Res.string.favorites_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
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
          if (state is FavoritesUiState.Content) {
            IconButton(onClick = { 
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              showSortDialog = true 
            }) {
              Icon(
                imageVector = Icons.Filled.Sort,
                contentDescription = stringResource(Res.string.favorites_sort)
              )
            }
            IconButton(onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSortDirectionToggle()
            }) {
              Icon(
                imageVector = if (state.ascending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = stringResource(Res.string.favorites_sort_direction)
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
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    StateCrossfade(state, modifier = Modifier.padding(innerPadding)) { current ->
      when (current) {
        FavoritesUiState.Loading -> {
          LoadingContent(message = stringResource(Res.string.favorites_loading))
        }

        FavoritesUiState.Empty -> {
          EmptyContent(
            icon = Icons.Outlined.FavoriteBorder,
            title = stringResource(Res.string.favorites_empty_title),
            subtitle = stringResource(Res.string.favorites_empty_subtitle)
          )
        }

        is FavoritesUiState.Content -> {
          FavoritesList(
            songs = current.songs,
            listState = listState,
            onRemove = onRemove
          )
        }

        is FavoritesUiState.Error -> {
          ErrorContent(
            title = stringResource(Res.string.common_error_title),
            message = io.github.alelk.pws.features.app.rememberResolved(current.message),
          )
        }
      }
    }
  }

  if (showSortDialog && state is FavoritesUiState.Content) {
    AlertDialog(
      onDismissRequest = { showSortDialog = false },
      title = { Text(stringResource(Res.string.favorites_sort)) },
      text = {
        androidx.compose.foundation.layout.Column {
          SortOptionRow(
            label = stringResource(Res.string.favorites_sort_added_date),
            selected = state.sortMode == FavoriteSortMode.ADDED_DATE,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSortModeChange(FavoriteSortMode.ADDED_DATE)
              showSortDialog = false
            }
          )
          SortOptionRow(
            label = stringResource(Res.string.favorites_sort_song_number),
            selected = state.sortMode == FavoriteSortMode.SONG_NUMBER,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSortModeChange(FavoriteSortMode.SONG_NUMBER)
              showSortDialog = false
            }
          )
          SortOptionRow(
            label = stringResource(Res.string.favorites_sort_song_name),
            selected = state.sortMode == FavoriteSortMode.SONG_NAME,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSortModeChange(FavoriteSortMode.SONG_NAME)
              showSortDialog = false
            }
          )
        }
      },
      confirmButton = {},
      dismissButton = {}
    )
  }
}

@Composable
private fun SortOptionRow(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f).padding(vertical = 12.dp),
    )
    RadioButton(selected = selected, onClick = onClick)
  }
}

@Composable
private fun FavoritesList(
  songs: List<FavoriteSongUi>,
  modifier: Modifier = Modifier,
  listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
  onRemove: (FavoriteSongUi) -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow

  LazyColumn(
    state = listState,
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = MaterialTheme.spacing.sm)
  ) {
    items(
      items = songs,
      key = { song ->
        when (song) {
          is FavoriteSongUi.BookedSong -> "booked-${song.subject.songNumberId}"
          is FavoriteSongUi.StandaloneSong -> "standalone-${song.subject.songId}"
        }
      }
    ) { song ->
      when (song) {
        is FavoriteSongUi.BookedSong -> {
          val songScreen = rememberScreen(SharedScreens.song(song.subject.songNumberId))
          SwipeableSongItem(
            number = song.songNumber,
            title = song.songName,
            subtitle = song.bookDisplayName,
            onClick = { navigator.push(songScreen) },
            onFavoriteToggle = { onRemove(song) },
            isFavorite = true
          )
        }
        is FavoriteSongUi.StandaloneSong -> {
          val songScreen = rememberScreen(SharedScreens.songById(song.subject.songId))
          SwipeableSongItem(
            number = null,
            title = song.songName,
            subtitle = null,
            onClick = { navigator.push(songScreen) },
            onFavoriteToggle = { onRemove(song) },
            isFavorite = true
          )
        }
      }

      if (song != songs.last()) {
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
