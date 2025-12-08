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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
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
import io.github.alelk.pws.features.theme.spacing

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

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      BooksTopBar(scrollBehavior = scrollBehavior)
    }
  ) { innerPadding ->
    when (state) {
      BooksUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = "Загрузка сборников..."
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
          title = "Не удалось загрузить сборники",
          message = "Проверьте подключение и попробуйте снова"
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksTopBar(
  scrollBehavior: TopAppBarScrollBehavior
) {
  LargeTopAppBar(
    title = {
      Text(
        text = "Сборники песен",
        style = MaterialTheme.typography.headlineMedium
      )
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

  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    modifier = modifier.fillMaxSize(),
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
      val bookSongsScreen = rememberScreen(SharedScreens.BookSongs(book.id))
      BookCard(
        displayName = book.displayName.value,
        songCount = book.countSongs,
        onClick = { navigator.push(bookSongsScreen) }
      )
    }

    // Bottom padding for navigation bar
    item(span = { GridItemSpan(maxLineSpan) }) {
      Spacer(Modifier.height(80.dp))
    }
  }
}
