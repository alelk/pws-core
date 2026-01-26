package io.github.alelk.pws.features.book.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.SongListItem
import io.github.alelk.pws.features.theme.spacing
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.koin.core.parameter.parametersOf

class BookSongsScreen(val bookId: BookId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<BookSongsScreenModel>(parameters = { parametersOf(bookId) })
    val state by viewModel.state.collectAsState()
    BookSongsContent(bookId = bookId, state = state)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSongsContent(
  bookId: BookId,
  state: BookSongsUiState,
  onNumberInputClick: () -> Unit = {}
) {
  val navigator = LocalNavigator.currentOrThrow
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          Text(
            text = when (state) {
              is BookSongsUiState.Content -> state.book.book.displayName.value
              else -> "Песни"
            },
            style = MaterialTheme.typography.headlineSmall
          )
        },
        navigationIcon = {
          IconButton(onClick = { navigator.pop() }) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Назад"
            )
          }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
      )
    },
    floatingActionButton = {
      if (state is BookSongsUiState.Content) {
        FloatingActionButton(
          onClick = onNumberInputClick,
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
          Icon(
            imageVector = Icons.Default.Dialpad,
            contentDescription = "Перейти по номеру"
          )
        }
      }
    }
  ) { innerPadding ->
    when (state) {
      BookSongsUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = "Загрузка песен..."
        )
      }

      is BookSongsUiState.Content -> {
        SongsList(
          bookId = bookId,
          songs = state.book.songs,
          modifier = Modifier.padding(innerPadding)
        )
      }

      BookSongsUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = "Не удалось загрузить песни",
          message = "Проверьте подключение и попробуйте снова"
        )
      }
    }
  }
}

@Composable
private fun SongsList(
  bookId: BookId,
  songs: Map<Int, SongSummary>,
  modifier: Modifier = Modifier
) {
  val navigator = LocalNavigator.currentOrThrow
  val sortedSongs = remember(songs) { songs.toList().sortedBy { (number, _) -> number } }

  // Client-side incremental rendering for large lists
  val listState = rememberLazyListState()
  val visibleCountState = rememberSaveable(songs.hashCode().toString()) {
    mutableStateOf(minOf(100, sortedSongs.size))
  }
  var visibleCount by visibleCountState

  // Reset visible count on data change
  LaunchedEffect(sortedSongs.size) {
    visibleCount = minOf(100, sortedSongs.size)
  }

  // Load more as user scrolls
  LaunchedEffect(listState, sortedSongs.size, visibleCount) {
    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
      .filter { it != null }
      .map { it!! }
      .distinctUntilChanged()
      .collect { lastVisibleIndex ->
        val threshold = visibleCount - 20
        if (lastVisibleIndex >= threshold && visibleCount < sortedSongs.size) {
          visibleCount = minOf(visibleCount + 50, sortedSongs.size)
        }
      }
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    state = listState,
    contentPadding = PaddingValues(vertical = MaterialTheme.spacing.sm)
  ) {
    val slice = sortedSongs.take(visibleCount)

    items(
      items = slice,
      key = { (number, _) -> number }
    ) { (number, song) ->
      val songScreen = rememberScreen(SharedScreens.Song(SongNumberId(bookId, song.id)))

      SongListItem(
        number = number,
        title = song.name.value,
        onClick = { navigator.push(songScreen) },
        isEdited = song.edited
      )

      if (number != slice.last().first) {
        HorizontalDivider(
          modifier = Modifier.padding(start = 72.dp),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
      }
    }

    // Loading indicator for more items
    if (visibleCount < sortedSongs.size) {
      item(key = "loading_more") {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.lg)
        ) {
          LoadingContent()
        }
      }
    }

    // Bottom padding
    item {
      Spacer(Modifier.height(80.dp))
    }
  }
}

