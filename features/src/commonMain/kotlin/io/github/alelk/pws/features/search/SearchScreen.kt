package io.github.alelk.pws.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.features.components.EmptyContent
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.HighlightedText
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.SearchEmptyContent
import io.github.alelk.pws.features.components.SearchField
import io.github.alelk.pws.features.theme.spacing

/**
 * Search screen without initial query.
 */
class SearchScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SearchScreenModel>()
    val state by viewModel.state.collectAsState()
    val query by viewModel.query.collectAsState()

    SearchContent(
      query = query,
      state = state,
      onQueryChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
      onSearch = { viewModel.onEvent(SearchEvent.SearchSubmitted) }
    )
  }
}

/**
 * Search screen with initial query - navigated from HomeScreen.
 * Immediately starts search with the provided query.
 */
class SearchResultsScreen(private val initialQuery: String) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SearchScreenModel>()
    val state by viewModel.state.collectAsState()
    val query by viewModel.query.collectAsState()

    // Set initial query on first composition
    LaunchedEffect(Unit) {
      viewModel.onEvent(SearchEvent.QueryChanged(initialQuery))
      viewModel.onEvent(SearchEvent.SearchSubmitted)
    }

    SearchContent(
      query = query,
      state = state,
      onQueryChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
      onSearch = { viewModel.onEvent(SearchEvent.SearchSubmitted) }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
  query: String,
  state: SearchUiState,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow
  val focusRequester = remember { FocusRequester() }

  // Auto-focus search field when screen opens
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = { navigator.pop() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Назад"
            )
          }
        },
        title = {
          Text(
            text = "Поиск",
            style = MaterialTheme.typography.headlineSmall
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Search field - uses query directly for responsive input
      SearchField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
          .padding(bottom = MaterialTheme.spacing.md)
          .focusRequester(focusRequester),
        placeholder = "Поиск песен..."
      )

      // Content based on state
      when (state) {
        SearchUiState.Idle -> {
          SearchIdleContent()
        }

        SearchUiState.Loading -> {
          LoadingContent(message = "Поиск...")
        }

        is SearchUiState.Suggestions -> {
          if (state.items.isEmpty()) {
            SearchEmptyContent(query = query)
          } else {
            SearchSuggestionsList(suggestions = state.items)
          }
        }

        is SearchUiState.Results -> {
          if (state.items.isEmpty() && !state.isLoading) {
            SearchEmptyContent(query = query)
          } else {
            SearchResultsList(
              results = state.items,
              isLoading = state.isLoading
            )
          }
        }

        is SearchUiState.Error -> {
          ErrorContent(
            title = "Ошибка поиска",
            message = state.message
          )
        }
      }
    }
  }
}

@Composable
private fun SearchIdleContent() {
  EmptyContent(
    icon = Icons.Outlined.Search,
    title = "Начните поиск",
    subtitle = "Введите название песни, номер или текст"
  )
}

@Composable
private fun SearchSuggestionsList(
  suggestions: List<SearchSuggestion>
) {
  val navigator = LocalNavigator.currentOrThrow

  LazyColumn(
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    items(
      items = suggestions,
      key = { it.songId.value }
    ) { suggestion ->
      // Navigate to song in book context if available, otherwise by id
      val songScreen = suggestion.bookReferences.firstOrNull()?.let { ref ->
        rememberScreen(SharedScreens.Song(SongNumberId(ref.bookId, suggestion.songId)))
      } ?: rememberScreen(SharedScreens.SongById(suggestion.songId))

      SearchSuggestionItem(
        suggestion = suggestion,
        onClick = { navigator.push(songScreen) }
      )
      HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )
    }
  }
}

@Composable
private fun SearchSuggestionItem(
  suggestion: SearchSuggestion,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = MaterialTheme.spacing.listItemHorizontal,
          vertical = MaterialTheme.spacing.listItemVertical
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Outlined.MusicNote,
        contentDescription = null,
        modifier = Modifier.size(MaterialTheme.spacing.iconMd),
        tint = MaterialTheme.colorScheme.primary
      )

      Spacer(Modifier.width(MaterialTheme.spacing.md))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = suggestion.songName,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (suggestion.bookReferences.isNotEmpty()) {
          Text(
            text = suggestion.booksDisplayText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        suggestion.snippet?.let { snippet ->
          HighlightedText(
            text = snippet,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
private fun SearchResultsList(
  results: List<SearchSuggestion>,
  isLoading: Boolean
) {
  val navigator = LocalNavigator.currentOrThrow

  LazyColumn(
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    items(
      items = results,
      key = { it.songId.value }
    ) { result ->
      // Navigate to song in book context if available, otherwise by id
      val songScreen = result.bookReferences.firstOrNull()?.let { ref ->
        rememberScreen(SharedScreens.Song(SongNumberId(ref.bookId, result.songId)))
      } ?: rememberScreen(SharedScreens.SongById(result.songId))

      SearchSuggestionItem(
        suggestion = result,
        onClick = { navigator.push(songScreen) }
      )
      HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )
    }

    if (isLoading) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.lg),
          contentAlignment = Alignment.Center
        ) {
          LoadingContent()
        }
      }
    }
  }
}

