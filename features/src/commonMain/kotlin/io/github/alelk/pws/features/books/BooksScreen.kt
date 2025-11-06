package io.github.alelk.pws.features.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.book.model.BookSummary

class BooksScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<BooksScreenModel>()
    val state by viewModel.state.collectAsState()
    Books(state = state)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Books(state: BooksUiState) {
  val navigator = LocalNavigator.currentOrThrow
  Scaffold(topBar = { TopAppBar(title = { Text("Сборники песен") }) }) { innerPadding ->
    when (state) {
      BooksUiState.Loading -> Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
      is BooksUiState.Content -> BooksList(state.books, navigator, modifier = Modifier.padding(innerPadding))
      BooksUiState.Error -> Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Ошибка") }
    }
  }
}

@Composable
private fun BooksList(books: List<BookSummary>, navigator: cafe.adriel.voyager.navigator.Navigator, modifier: Modifier = Modifier) {
  LazyColumn(modifier.fillMaxSize()) {
    items(books, key = { it.id.toString() }) { book ->
      val bookSongsScreen = rememberScreen(SharedScreens.BookSongs(book.id))
      BookRow(book = book, onClick = { navigator.push(bookSongsScreen) })
    }
    item { Spacer(Modifier.height(32.dp)) }
  }
}

@Composable
private fun BookRow(book: BookSummary, onClick: () -> Unit) {
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .clickable(onClick = onClick)
  ) {
    Column(Modifier.padding(16.dp)) {
      Text(book.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(Modifier.height(4.dp))
      AssistChip(onClick = {}, label = { Text("${book.countSongs} песен") })
    }
  }
}
