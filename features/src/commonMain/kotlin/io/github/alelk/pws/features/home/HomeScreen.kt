package io.github.alelk.pws.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.features.components.AppLargeTopBar
import io.github.alelk.pws.features.components.BookCard
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.NumberInputModal
import io.github.alelk.pws.features.components.SearchBarWithSuggestions
import io.github.alelk.pws.features.components.clickableWithScaleAndClip
import io.github.alelk.pws.features.components.shimmerEffect
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.app_name
import io.github.alelk.pws.features.resources.book_songs_count
import io.github.alelk.pws.features.resources.home_load_error_message
import io.github.alelk.pws.features.resources.home_load_error_title
import io.github.alelk.pws.features.resources.home_quick_favorites
import io.github.alelk.pws.features.resources.home_quick_history
import io.github.alelk.pws.features.resources.home_quick_number
import io.github.alelk.pws.features.resources.home_quick_tags
import io.github.alelk.pws.features.resources.home_quick_text
import io.github.alelk.pws.features.resources.home_recently_opened
import io.github.alelk.pws.features.resources.home_songbooks
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.spacing
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
  var showNumberInput by remember { mutableStateOf(false) }
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
              imageVector = Icons.Filled.Settings,
              contentDescription = stringResource(Res.string.settings_open)
            )
          }
        }
      )
    }
  ) { innerPadding ->
    when (state) {
      HomeUiState.Loading -> {
        HomeContentSkeleton(
          modifier = Modifier.padding(innerPadding)
        )
      }

      is HomeUiState.Content -> {
        // Main scrollable content with search bar inside
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 140.dp),
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.screenHorizontal,
            vertical = MaterialTheme.spacing.md
          ),
          horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
        ) {
          // Search bar - scrolls with content
          item(span = { GridItemSpan(maxLineSpan) }) {
            SearchBarWithSuggestions(
              query = searchQuery,
              onQueryChange = onSearchQueryChange,
              onSearch = {
                if (searchQuery.isNotBlank()) {
                  val screen = ScreenRegistry.get(SharedScreens.SearchResults(searchQuery))
                  onClearSearch()
                  navigator.push(screen)
                }
              },
              suggestions = suggestions,
              onSuggestionClick = { suggestion ->
                onClearSearch()
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
              isLoading = isSearching,
              showSuggestions = searchQuery.isNotBlank()
            )
          }

          // Quick action chips - scrollable row
          item(span = { GridItemSpan(maxLineSpan) }) {
            QuickActionsRow(
              onNumberSearchClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showNumberInput = true 
              },
              onTextSearchClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navigator.push(ScreenRegistry.get(SharedScreens.Search))
              },
              onHistoryClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navigator.push(ScreenRegistry.get(SharedScreens.History))
              }
            )
          }

          // Second row of quick actions - Favorites and Tags
          item(span = { GridItemSpan(maxLineSpan) }) {
            QuickActionsRowSecondary(
              onFavoritesClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navigator.push(ScreenRegistry.get(SharedScreens.Favorites))
              },
              onTagsClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navigator.push(ScreenRegistry.get(SharedScreens.Tags))
              }
            )
          }

          // Recently viewed songs section
          if (state.recentSongs.isNotEmpty()) {
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

          item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(horizontal = 0.dp),
              horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
            ) {
              itemsIndexed(
                items = state.recentSongs,
                key = { _, song -> song.id }
              ) { index, song ->
                val songScreen = when (val subject = song.subject) {
                  is HistorySubject.BookedSong -> rememberScreen(SharedScreens.song(subject.songNumberId))
                  is HistorySubject.StandaloneSong -> rememberScreen(SharedScreens.songById(subject.songId))
                }
                RecentSongCard(
                  song = song,
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    navigator.push(songScreen)
                  },
                  modifier = Modifier.testTag("recent-song-card-$index")
                )
              }
            }
          }
          }

          // Section header for books
          item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
              text = stringResource(Res.string.home_songbooks),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.semantics { heading() }
            )
          }

          // Books grid - limit to max 6 featured books
          items(
            items = state.books.take(6),
            key = { it.id.toString() }
          ) { book ->
            val bookSongsScreen = rememberScreen(SharedScreens.bookSongs(book.id))
            BookCard(
              displayName = book.displayName.value,
              songCount = book.countSongs,
              aspectRatio = 1.4f,
              initialsStyle = MaterialTheme.typography.headlineMedium,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navigator.push(bookSongsScreen)
              }
            )
          }

          // Bottom spacer
          item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(32.dp))
          }
        }
      }

      HomeUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = stringResource(Res.string.home_load_error_title),
          message = stringResource(Res.string.home_load_error_message)
        )
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
private fun QuickActionsRow(
  onNumberSearchClick: () -> Unit,
  onTextSearchClick: () -> Unit,
  onHistoryClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = MaterialTheme.spacing.md),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
  ) {
    QuickActionChip(
      icon = Icons.Default.Dialpad,
      label = stringResource(Res.string.home_quick_number),
      onClick = onNumberSearchClick,
      modifier = Modifier.weight(1f).testTag("action:number-search")
    )
    QuickActionChip(
      icon = Icons.Default.TextFields,
      label = stringResource(Res.string.home_quick_text),
      onClick = onTextSearchClick,
      modifier = Modifier.weight(1f).testTag("action:text-search")
    )
    QuickActionChip(
      icon = Icons.Default.History,
      label = stringResource(Res.string.home_quick_history),
      onClick = onHistoryClick,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun QuickActionsRowSecondary(
  onFavoritesClick: () -> Unit,
  onTagsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(top = MaterialTheme.spacing.sm),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
  ) {
    QuickActionChip(
      icon = Icons.Outlined.FavoriteBorder,
      label = stringResource(Res.string.home_quick_favorites),
      onClick = onFavoritesClick,
      modifier = Modifier.weight(1f)
    )
    QuickActionChip(
      icon = Icons.Outlined.Tag,
      label = stringResource(Res.string.home_quick_tags),
      onClick = onTagsClick,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun QuickActionChip(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.clickableWithScaleAndClip(shape = MaterialTheme.shapes.medium, onClick = onClick),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    tonalElevation = 2.dp
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSecondaryContainer
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
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

    // Quick actions placeholder
    item(span = { GridItemSpan(maxLineSpan) }) {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .shimmerEffect(MaterialTheme.shapes.medium)
        )
        Box(
          modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .shimmerEffect(MaterialTheme.shapes.medium)
        )
      }
    }

    // Second row quick actions placeholder
    item(span = { GridItemSpan(maxLineSpan) }) {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .shimmerEffect(MaterialTheme.shapes.medium)
        )
        Box(
          modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .shimmerEffect(MaterialTheme.shapes.medium)
        )
      }
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

    // Books placeholders
    items(6) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
          .shimmerEffect(MaterialTheme.shapes.medium)
      )
    }
  }
}
