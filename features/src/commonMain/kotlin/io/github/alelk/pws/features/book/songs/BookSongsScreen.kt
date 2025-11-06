package io.github.alelk.pws.features.book.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
import org.koin.core.parameter.parametersOf

class BookSongsScreen(val bookId: BookId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<BookSongsScreenModel>(parameters = { parametersOf(bookId) })
    val state by viewModel.state.collectAsState()
    BookSongs(bookId = bookId, state = state)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSongs(
  bookId: BookId,
  state: BookSongsUiState
) {
  val navigator = LocalNavigator.currentOrThrow

  Scaffold(topBar = {
    TopAppBar(
      title = { Text("Песни сборника") },
      navigationIcon = {
        IconButton(onClick = { navigator.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") }
      }
    )
  }) { innerPadding ->
    when (state) {
      BookSongsUiState.Loading -> {
        Box(
          Modifier.padding(innerPadding).fillMaxSize()
        ) { CircularProgressIndicator(Modifier.align(androidx.compose.ui.Alignment.Center)) }
      }

      is BookSongsUiState.Content -> {
        LazyColumn(Modifier.padding(innerPadding).fillMaxSize()) {
          val sortedSongs = state.book.songs.toList().sortedBy { (number, _) -> number }
          items(items = sortedSongs) { (number, song) ->
            val songScreen = rememberScreen(SharedScreens.Song(SongNumberId(bookId, song.id)))
            SongRow(number = number, song = song, onClick = { navigator.push(songScreen) })
          }
        }
      }

      BookSongsUiState.Error -> {
        Box(
          Modifier.padding(innerPadding).fillMaxSize()
        ) { Text("Ошибка", Modifier.align(androidx.compose.ui.Alignment.Center)) }
      }
    }
  }
}

@Composable
private fun SongRow(number: Int, song: SongSummary, onClick: () -> Unit) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .clickable(onClick = onClick),
    shape = MaterialTheme.shapes.small,
    tonalElevation = if (song.edited) 2.dp else 0.dp
  ) {
    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(number.toString(), style = MaterialTheme.typography.labelLarge)
      Column(Modifier.weight(1f)) {
        Text(song.name.value, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (song.edited) Text("Редактирована", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

