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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.features.components.AppTopBar
import io.github.alelk.pws.features.components.EmptyContent
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.HighlightedText
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.SearchEmptyContent
import io.github.alelk.pws.features.components.SearchField
import io.github.alelk.pws.features.components.StateCrossfade
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.search_error_title
import io.github.alelk.pws.features.resources.search_idle_subtitle
import io.github.alelk.pws.features.resources.search_idle_title
import io.github.alelk.pws.features.resources.search_loading
import io.github.alelk.pws.features.resources.search_placeholder
import io.github.alelk.pws.features.resources.search_scope_all
import io.github.alelk.pws.features.resources.search_scope_in_books
import io.github.alelk.pws.features.resources.search_scope_standalone
import io.github.alelk.pws.features.resources.search_title
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

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
  override val key: String = "search-results/$initialQuery"

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

/** Локальный UI-фильтр результатов поиска. Не лезет в use case — пост-фильтрация. */
enum class SearchScope { ALL, IN_BOOKS, STANDALONE }

private fun SearchScope.matches(s: SearchSuggestion): Boolean = when (this) {
  SearchScope.ALL -> true
  SearchScope.IN_BOOKS -> s.bookReferences.isNotEmpty()
  SearchScope.STANDALONE -> s.bookReferences.isEmpty()
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
  var scope by remember { mutableStateOf(SearchScope.ALL) }

  // Auto-focus search field when screen opens
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  Scaffold(
    topBar = {
      AppTopBar(
        title = stringResource(Res.string.search_title),
        canNavigateBack = navigator.canPop,
        onNavigateBack = { navigator.pop() },
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
        }
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
          .padding(bottom = MaterialTheme.spacing.sm)
          .focusRequester(focusRequester),
        placeholder = stringResource(Res.string.search_placeholder)
      )

      // Scope chips — iOS-style filter row под поиском.
      SearchScopeChips(
        scope = scope,
        onScopeChange = { scope = it },
        modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm)
      )

      // Content based on state — spring crossfade between Loading/Empty/Results/Error
      StateCrossfade(state) { current ->
        when (current) {
          SearchUiState.Idle -> {
            SearchIdleContent()
          }

          SearchUiState.Loading -> {
            LoadingContent(message = stringResource(Res.string.search_loading))
          }

          is SearchUiState.Suggestions -> {
            val filtered = current.items.filter { scope.matches(it) }
            if (filtered.isEmpty()) {
              SearchEmptyContent(query = query)
            } else {
              SearchSuggestionsList(suggestions = filtered)
            }
          }

          is SearchUiState.Results -> {
            val filtered = current.items.filter { scope.matches(it) }
            if (filtered.isEmpty() && !current.isLoading) {
              SearchEmptyContent(query = query)
            } else {
              SearchResultsList(
                results = filtered,
                isLoading = current.isLoading
              )
            }
          }

          is SearchUiState.Error -> {
            ErrorContent(
              title = stringResource(Res.string.search_error_title),
              message = io.github.alelk.pws.features.app.rememberResolved(current.message),
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScopeChips(
  scope: SearchScope,
  onScopeChange: (SearchScope) -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val items = listOf(
    SearchScope.ALL to stringResource(Res.string.search_scope_all),
    SearchScope.IN_BOOKS to stringResource(Res.string.search_scope_in_books),
    SearchScope.STANDALONE to stringResource(Res.string.search_scope_standalone),
  )
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.screenHorizontal),
    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.sm),
  ) {
    items(items, key = { it.first }) { (s, label) ->
      FilterChip(
        selected = scope == s,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onScopeChange(s)
        },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
      )
    }
  }
}

@Composable
private fun SearchIdleContent() {
  EmptyContent(
    icon = Icons.Outlined.Search,
    title = stringResource(Res.string.search_idle_title),
    subtitle = stringResource(Res.string.search_idle_subtitle)
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
        rememberScreen(SharedScreens.song(SongNumberId(ref.bookId, suggestion.songId)))
      } ?: rememberScreen(SharedScreens.songById(suggestion.songId))

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
  val booksByNumber = suggestion.booksByNumber
  val haptic = LocalHapticFeedback.current
  val itemCd = suggestion.primarySongNumber?.let { "song-row-$it" } ?: "song-row-unknown"

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(itemCd)
      .clickable(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
      }),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = MaterialTheme.spacing.listItemHorizontal,
          vertical = MaterialTheme.spacing.listItemVertical
        ),
      verticalAlignment = Alignment.Top
    ) {
      // Song number badge (from first book reference)
      suggestion.primarySongNumber?.let { number ->
        Text(
          text = number.toString(),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.widthIn(min = 40.dp).padding(end = MaterialTheme.spacing.md)
        )
      } ?: run {
        // Fallback icon when no book reference
        Icon(
          imageVector = Icons.Outlined.MusicNote,
          contentDescription = null,
          modifier = Modifier.size(24.dp),
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(MaterialTheme.spacing.md))
      }

      Column(modifier = Modifier.weight(1f)) {
        // Song name
        Text(
          text = suggestion.songName,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Books grouped by number
        if (booksByNumber.isNotEmpty()) {
          if (booksByNumber.size == 1) {
            // All books have the same number - show them together
            Text(
              text = suggestion.booksDisplayTextForNumber(booksByNumber.first().first),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            // Different numbers in different books - show primary book only
            suggestion.bookReferences.firstOrNull()?.let { ref ->
              Text(
                text = ref.displayShortName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Snippet with highlighting
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
        rememberScreen(SharedScreens.song(SongNumberId(ref.bookId, result.songId)))
      } ?: rememberScreen(SharedScreens.songById(result.songId))

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
