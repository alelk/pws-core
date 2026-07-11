package io.github.alelk.pws.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.features.components.AppLargeTopBar
import io.github.alelk.pws.features.components.BookCard
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.HomeSearchField
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.components.NumberInputModal
import io.github.alelk.pws.features.components.OnTabReselected
import io.github.alelk.pws.features.components.SearchSuggestionRow
import io.github.alelk.pws.features.components.SearchSuggestionsLoadingRow
import io.github.alelk.pws.features.components.StateCrossfade
import io.github.alelk.pws.features.components.SwipeableSongItem
import io.github.alelk.pws.features.components.shimmerEffect
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.app_name
import io.github.alelk.pws.features.resources.home_empty_action
import io.github.alelk.pws.features.resources.home_empty_message
import io.github.alelk.pws.features.resources.home_load_error_message
import io.github.alelk.pws.features.resources.home_load_error_title
import io.github.alelk.pws.features.resources.home_recently_opened
import io.github.alelk.pws.features.resources.home_songbooks
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Home Screen - Main entry point with search focus.
 */
class HomeScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<HomeScreenModel>()
    val state by viewModel.state.collectAsState()
    val content = state as? HomeUiState.Content

    HomeContent(
      state = state,
      searchQuery = content?.searchQuery.orEmpty(),
      suggestions = content?.searchSuggestions.orEmpty(),
      isSearching = content?.isSearching == true,
      onSearchQueryChange = { viewModel.onEvent(HomeEvent.SearchQueryChanged(it)) },
      onClearSearch = { viewModel.onEvent(HomeEvent.SearchCleared) },
      numberQuery = content?.numberQuery.orEmpty(),
      numberSuggestions = content?.numberSuggestions.orEmpty(),
      isNumberSearching = content?.isNumberSearching == true,
      onNumberQueryChange = { viewModel.onEvent(HomeEvent.NumberQueryChanged(it)) },
      onClearNumberSearch = { viewModel.onEvent(HomeEvent.NumberCleared) },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
  state: HomeUiState,
  searchQuery: String,
  suggestions: List<SearchSuggestion>,
  isSearching: Boolean,
  onSearchQueryChange: (String) -> Unit,
  onClearSearch: () -> Unit,
  numberQuery: String = "",
  numberSuggestions: List<SearchSuggestion> = emptyList(),
  isNumberSearching: Boolean = false,
  onNumberQueryChange: (String) -> Unit = {},
  onClearNumberSearch: () -> Unit = {}
) {
  val navigator = LocalNavigator.currentOrThrow
  val haptic = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  var showNumberInput by remember { mutableStateOf(false) }
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val gridState = rememberLazyGridState()
  val scope = rememberCoroutineScope()

  OnTabReselected(NavDestination.Home) {
    scope.launch { gridState.animateScrollToItem(0) }
    scrollBehavior.state.heightOffset = 0f
  }

  Scaffold(
    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      AppLargeTopBar(
        title = stringResource(Res.string.app_name),
        canNavigateBack = false,
        scrollBehavior = scrollBehavior,
        actions = {
          IconButton(
              onClick = { navigator.push(ScreenRegistry.get(SharedScreens.Settings)) },
              modifier = Modifier.testTag("action:open-settings")
            ) {
            Icon(
              imageVector = Lucide.Settings,
              contentDescription = stringResource(Res.string.settings_open)
            )
          }
        }
      )
    }
  ) { innerPadding ->
    StateCrossfade(state, modifier = Modifier.padding(innerPadding)) { current ->
    when (current) {
      HomeUiState.Loading -> {
        HomeContentSkeleton()
      }

      is HomeUiState.Content -> {
        val isSearchMode = searchQuery.isNotBlank()
        // Main scrollable content with search bar inside
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 140.dp),
          state = gridState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.screenHorizontal,
            vertical = MaterialTheme.spacing.md
          ),
          horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
        ) {
          // Search hero — the primary action on Home
          item(span = { GridItemSpan(maxLineSpan) }) {
            HomeSearchField(
              query = searchQuery,
              onQueryChange = onSearchQueryChange,
              // Live search: results are already under the field, Enter only hides the keyboard.
              onSearch = { keyboardController?.hide() },
              isLoading = isSearching,
              onNumberModeClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showNumberInput = true
              }
            )
          }

          // Live results take over the surface while a query is active
          // (single-surface search, same as the Search tab — no popup).
          if (isSearchMode) {
            if (isSearching && suggestions.isEmpty()) {
              item(span = { GridItemSpan(maxLineSpan) }) {
                SearchSuggestionsLoadingRow()
              }
            }
            itemsIndexed(
              items = suggestions,
              key = { _, s -> s.songId.value },
              span = { _, _ -> GridItemSpan(maxLineSpan) }
            ) { index, suggestion ->
              SearchSuggestionRow(
                suggestion = suggestion,
                onClick = {
                  onClearSearch()
                  keyboardController?.hide()
                  // Navigate to song in book context if available
                  val screen = suggestion.bookReferences.firstOrNull()?.let { ref ->
                    ScreenRegistry.get(
                      SharedScreens.song(io.github.alelk.pws.domain.core.ids.SongNumberId(ref.bookId, suggestion.songId))
                    )
                  } ?: ScreenRegistry.get(
                    SharedScreens.songById(suggestion.songId)
                  )
                  navigator.push(screen)
                },
                modifier = Modifier.testTag("home-suggestion-$index")
              )
            }
          }

          // Section header for books — the shelf comes right after the search
          if (!isSearchMode) item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
              text = stringResource(Res.string.home_songbooks),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.semantics { heading() }
            )
          }

          // Empty state: no books installed yet
          if (!isSearchMode && current.books.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
            val bookLibraryScreen = rememberScreen(SharedScreens.BookLibrary)
            Column(
              modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.lg),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
              Text(
                text = stringResource(Res.string.home_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Button(onClick = { navigator.push(bookLibraryScreen) }) {
                Text(stringResource(Res.string.home_empty_action))
              }
            }
          }

          // Books grid - limit to max 6 featured books
          if (!isSearchMode) items(
            items = current.books.take(6),
            key = { it.id.toString() }
          ) { book ->
            val bookSongsScreen = rememberScreen(SharedScreens.bookSongs(book.id))
            BookCard(
              displayName = book.displayName.value,
              songCount = book.countSongs,
              colorKey = book.id.toString(),
              aspectRatio = 1.4f,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navigator.push(bookSongsScreen)
              }
            )
          }

          // Recently opened — compact rows below the shelf (quick "continue" path)
          if (!isSearchMode && current.recentSongs.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
              Spacer(Modifier.height(MaterialTheme.spacing.md))
              Text(
                text = stringResource(Res.string.home_recently_opened),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                  .testTag("home-section-recently-viewed")
                  .semantics { heading() }
              )
            }

            itemsIndexed(
              items = current.recentSongs.take(5),
              key = { _, song -> song.id },
              span = { _, _ -> GridItemSpan(maxLineSpan) }
            ) { index, song ->
              val songScreen = when (val subject = song.subject) {
                is HistorySubject.BookedSong -> rememberScreen(SharedScreens.song(subject.songNumberId))
                is HistorySubject.StandaloneSong -> rememberScreen(SharedScreens.songById(subject.songId))
              }
              SwipeableSongItem(
                number = song.songNumber,
                title = song.songName,
                subtitle = song.bookDisplayName,
                onClick = { navigator.push(songScreen) },
                modifier = Modifier.testTag("recent-song-card-$index")
              )
            }
          }

          // Bottom spacer
          item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(32.dp))
          }
        }
      }

      HomeUiState.Error -> {
        ErrorContent(
          title = stringResource(Res.string.home_load_error_title),
          message = stringResource(Res.string.home_load_error_message)
        )
      }
    }
    }
  }

  // Number input modal
  if (showNumberInput) {
    NumberInputModal(
      numberQuery = numberQuery,
      suggestions = numberSuggestions,
      isSearching = isNumberSearching,
      onNumberChange = onNumberQueryChange,
      onDismiss = {
        showNumberInput = false
        onClearNumberSearch()
      },
      onSuggestionClick = { suggestion ->
        showNumberInput = false
        onClearNumberSearch()
        val screen = suggestion.bookReferences.firstOrNull()?.let { ref ->
          ScreenRegistry.get(
            SharedScreens.song(io.github.alelk.pws.domain.core.ids.SongNumberId(ref.bookId, suggestion.songId))
          )
        } ?: ScreenRegistry.get(SharedScreens.songById(suggestion.songId))
        navigator.push(screen)
      }
    )
  }
}

@Composable
private fun HomeContentSkeleton(modifier: Modifier = Modifier) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 140.dp),
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(
      horizontal = MaterialTheme.spacing.screenHorizontal,
      vertical = MaterialTheme.spacing.md
    ),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
  ) {

    // Search bar placeholder
    item(span = { GridItemSpan(maxLineSpan) }) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .shimmerEffect(RoundedCornerShape(28.dp))
      )
    }

    // Section title
    item(span = { GridItemSpan(maxLineSpan) }) {
      Spacer(Modifier.height(MaterialTheme.spacing.sm))
      Box(
        modifier = Modifier
          .width(150.dp)
          .height(24.dp)
          .shimmerEffect(MaterialTheme.shapes.small)
      )
    }

    // Books placeholders — same aspect ratio as the real cards (no jump after load)
    items(6) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.4f)
          .shimmerEffect(MaterialTheme.shapes.medium)
      )
    }
  }
}
