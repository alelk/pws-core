package io.github.alelk.pws.features.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.runtime.remember
import io.github.alelk.pws.features.booklibrary.BookLibraryFirstLaunchState
import org.koin.compose.getKoin
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.features.components.BookCard
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.components.OnTabReselected
import io.github.alelk.pws.features.components.StateCrossfade
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.books_error_title
import io.github.alelk.pws.features.resources.books_loading
import io.github.alelk.pws.features.resources.home_load_error_message
import io.github.alelk.pws.features.resources.home_songbooks
import io.github.alelk.pws.features.resources.book_library_open
import io.github.alelk.pws.features.resources.books_browse_library_action
import io.github.alelk.pws.features.resources.books_browse_library_subtitle
import io.github.alelk.pws.features.resources.books_browse_library_title
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

class BooksScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<BooksScreenModel>()
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val bookLibraryScreen = rememberScreen(SharedScreens.BookLibrary)
    val koin = getKoin()
    val firstLaunchState = remember { runCatching { koin.get<BookLibraryFirstLaunchState>() }.getOrNull() }

    LaunchedEffect(Unit) {
      if (firstLaunchState?.shouldShow() == true) {
        firstLaunchState.markShown()
        navigator.push(bookLibraryScreen)
      }
    }

    BooksContent(
      state = state,
      onRetry = viewModel::retry,
      onOpenBookLibrary = { navigator.push(bookLibraryScreen) },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksContent(state: BooksUiState, onRetry: () -> Unit = {}, onOpenBookLibrary: () -> Unit = {}) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val navigator = LocalNavigator.currentOrThrow
  val gridState = rememberLazyGridState()
  val scope = rememberCoroutineScope()

  OnTabReselected(NavDestination.Books) {
    scope.launch { gridState.animateScrollToItem(0) }
    scrollBehavior.state.heightOffset = 0f
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      BooksTopBar(
        scrollBehavior = scrollBehavior,
        onOpenSettings = { navigator.push(ScreenRegistry.get(SharedScreens.Settings)) },
        onOpenBookLibrary = onOpenBookLibrary,
      )
    }
  ) { innerPadding ->
    StateCrossfade(state, modifier = Modifier.padding(innerPadding)) { current ->
      when (current) {
        BooksUiState.Loading -> {
          LoadingContent(message = stringResource(Res.string.books_loading))
        }
        is BooksUiState.Content -> {
          BooksGrid(books = current.books, gridState = gridState, onOpenBookLibrary = onOpenBookLibrary)
        }
        BooksUiState.Error -> {
          ErrorContent(
            title = stringResource(Res.string.books_error_title),
            message = stringResource(Res.string.home_load_error_message),
            onRetry = onRetry,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksTopBar(
  scrollBehavior: TopAppBarScrollBehavior,
  onOpenSettings: () -> Unit,
  onOpenBookLibrary: () -> Unit = {},
) {
  LargeTopAppBar(
    title = {
      Text(
        text = stringResource(Res.string.home_songbooks),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.semantics { heading() }
      )
    },
    actions = {
      IconButton(onClick = onOpenBookLibrary) {
        Icon(
          imageVector = Icons.Filled.LibraryBooks,
          contentDescription = stringResource(Res.string.book_library_open)
        )
      }
      IconButton(onClick = onOpenSettings) {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = stringResource(Res.string.settings_open)
        )
      }
    },
    scrollBehavior = scrollBehavior,
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
    )
  )
}

@Composable
private fun BooksGrid(
  books: List<BookSummary>,
  modifier: Modifier = Modifier,
  gridState: androidx.compose.foundation.lazy.grid.LazyGridState = rememberLazyGridState(),
  onOpenBookLibrary: () -> Unit = {},
) {
  val navigator = LocalNavigator.currentOrThrow
  val haptic = LocalHapticFeedback.current

  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    state = gridState,
    modifier = modifier.fillMaxSize().testTag("books-grid"),
    contentPadding = PaddingValues(
      horizontal = MaterialTheme.spacing.screenHorizontal,
      vertical = MaterialTheme.spacing.md
    ),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
  ) {
    itemsIndexed(
      items = books,
      key = { _, book -> book.id.toString() }
    ) { index, book ->
      val bookSongsScreen = rememberScreen(SharedScreens.bookSongs(book.id))
      BookCard(
        displayName = book.displayName.value,
        songCount = book.countSongs,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          navigator.push(bookSongsScreen)
        },
        testTag = "book-card-$index"
      )
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
      BrowseLibraryBanner(onClick = onOpenBookLibrary)
    }

    // Bottom padding for navigation bar
    item(span = { GridItemSpan(maxLineSpan) }) {
      Spacer(Modifier.height(80.dp))
    }
  }
}

@Composable
private fun BrowseLibraryBanner(onClick: () -> Unit) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
      Icon(
        imageVector = Icons.Filled.LibraryBooks,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSecondaryContainer,
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(Res.string.books_browse_library_title),
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
          text = stringResource(Res.string.books_browse_library_subtitle),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
      }
      FilledTonalButton(onClick = onClick) {
        Text(
          text = stringResource(Res.string.books_browse_library_action),
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}
