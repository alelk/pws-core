package io.github.alelk.pws.features.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.features.components.BookCard
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.books_error_title
import io.github.alelk.pws.features.resources.books_loading
import io.github.alelk.pws.features.resources.home_load_error_message
import io.github.alelk.pws.features.resources.home_songbooks
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

class BooksScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<BooksScreenModel>()
    val state by viewModel.state.collectAsState()
    BooksContent(state = state)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksContent(state: BooksUiState) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val navigator = LocalNavigator.currentOrThrow

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      BooksTopBar(
        scrollBehavior = scrollBehavior,
        onOpenSettings = { navigator.push(ScreenRegistry.get(SharedScreens.Settings)) }
      )
    }
  ) { innerPadding ->
    when (state) {
      BooksUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = stringResource(Res.string.books_loading)
        )
      }
      is BooksUiState.Content -> {
        BooksGrid(
          books = state.books,
          modifier = Modifier.padding(innerPadding)
        )
      }
      BooksUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = stringResource(Res.string.books_error_title),
          message = stringResource(Res.string.home_load_error_message)
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksTopBar(
  scrollBehavior: TopAppBarScrollBehavior,
  onOpenSettings: () -> Unit,
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
  modifier: Modifier = Modifier
) {
  val navigator = LocalNavigator.currentOrThrow
  val haptic = LocalHapticFeedback.current

  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    modifier = modifier.fillMaxSize().testTag("books-grid"),
    contentPadding = PaddingValues(
      horizontal = MaterialTheme.spacing.screenHorizontal,
      vertical = MaterialTheme.spacing.md
    ),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
  ) {
    items(
      items = books,
      key = { it.id.toString() }
    ) { book ->
      val bookSongsScreen = rememberScreen(SharedScreens.bookSongs(book.id))
      BookCard(
        displayName = book.displayName.value,
        songCount = book.countSongs,
        onClick = { 
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          navigator.push(bookSongsScreen) 
        }
      )
    }

    // Bottom padding for navigation bar
    item(span = { GridItemSpan(maxLineSpan) }) {
      Spacer(Modifier.height(80.dp))
    }
  }
}
