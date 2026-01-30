package io.github.alelk.pws.features.favorites

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

class FavoritesScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<FavoritesScreenModel>()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    FavoritesContent(
      state = state,
      snackbarHostState = snackbarHostState,
      onRemove = { viewModel.onEvent(FavoritesEvent.RemoveFromFavorites(it)) }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
  state: FavoritesUiState,
  snackbarHostState: SnackbarHostState,
  onRemove: (FavoriteSongUi) -> Unit
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val navigator = LocalNavigator.currentOrThrow

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        navigationIcon = {
          if (navigator.canPop) {
            IconButton(onClick = { navigator.pop() }) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
              )
            }
          }
        },
        title = {
          Text(
            text = "Избранное",
            style = MaterialTheme.typography.headlineMedium
          )
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
    when (state) {
      FavoritesUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = "Загрузка избранного..."
        )
      }

      FavoritesUiState.Empty -> {
        EmptyContent(
          modifier = Modifier.padding(innerPadding),
          icon = Icons.Outlined.FavoriteBorder,
          title = "Нет избранных песен",
          subtitle = "Добавляйте песни в избранное, чтобы быстро находить их"
        )
      }

      is FavoritesUiState.Content -> {
        FavoritesList(
          songs = state.songs,
          modifier = Modifier.padding(innerPadding),
          onRemove = onRemove
        )
      }

      is FavoritesUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = "Ошибка",
          message = state.message
        )
      }
    }
  }
}

@Composable
private fun FavoritesList(
  songs: List<FavoriteSongUi>,
  modifier: Modifier = Modifier,
  onRemove: (FavoriteSongUi) -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow

  LazyColumn(
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
          val songScreen = rememberScreen(SharedScreens.Song(song.subject.songNumberId))
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
          val songScreen = rememberScreen(SharedScreens.SongById(song.subject.songId))
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
