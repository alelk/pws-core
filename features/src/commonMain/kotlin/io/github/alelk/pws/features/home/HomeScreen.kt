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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import io.github.alelk.pws.features.book.songs.BookSongsScreen
import io.github.alelk.pws.features.components.AppLargeTopBar
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.NumberInputModal
import io.github.alelk.pws.features.components.SearchBarWithSuggestions
import io.github.alelk.pws.features.components.clickableWithScale
import io.github.alelk.pws.features.components.generateBookColor
import io.github.alelk.pws.features.components.getInitials
import io.github.alelk.pws.features.components.shimmerEffect
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.spacing

/**
 * Home Screen - Main entry point with search focus.
 */
class HomeScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<HomeScreenModel>()
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    HomeContent(
      state = state,
      searchQuery = searchQuery,
      suggestions = suggestions,
      isSearching = isSearching,
      onSearchQueryChange = viewModel::onSearchQueryChange,
      onClearSearch = viewModel::onClearSearch
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
  onClearSearch: () -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow
  var showNumberInput by remember { mutableStateOf(false) }
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      AppLargeTopBar(
        title = "Псаломщик",
        canNavigateBack = false,
        scrollBehavior = scrollBehavior
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
                    SharedScreens.Song(io.github.alelk.pws.domain.core.ids.SongNumberId(ref.bookId, suggestion.songId))
                  )
                } ?: ScreenRegistry.get(
                  SharedScreens.SongById(suggestion.songId)
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
              onNumberSearchClick = { showNumberInput = true },
              onTextSearchClick = {
                // Focus search bar? Or navigate to search screen?
                // For now let's make it more useful - navigate to text search directly (which is conceptually 'Search')
                navigator.push(ScreenRegistry.get(SharedScreens.Search))
              },
              onHistoryClick = {
                navigator.push(ScreenRegistry.get(SharedScreens.History))
              }
            )
          }

          // Second row of quick actions - Favorites and Tags
          item(span = { GridItemSpan(maxLineSpan) }) {
            QuickActionsRowSecondary(
              onFavoritesClick = {
                // Navigate to favorites screen
                navigator.push(ScreenRegistry.get(SharedScreens.Favorites))
              },
              onTagsClick = {
                // Navigate to tags screen
                navigator.push(ScreenRegistry.get(SharedScreens.Tags))
              }
            )
          }

          // Recently viewed songs section
          if (state.recentSongs.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
              Spacer(Modifier.height(MaterialTheme.spacing.md))
              Text(
                text = "Недавно открытые",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
              )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
              LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
              ) {
                items(
                  items = state.recentSongs,
                  key = { it.id }
                ) { song ->
                  val songScreen = when (val subject = song.subject) {
                    is HistorySubject.BookedSong -> rememberScreen(SharedScreens.Song(subject.songNumberId))
                    is HistorySubject.StandaloneSong -> rememberScreen(SharedScreens.SongById(subject.songId))
                  }
                  RecentSongCard(
                    song = song,
                    onClick = { navigator.push(songScreen) }
                  )
                }
              }
            }
          }

          // Section header for books
          item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
              text = "Сборники песен",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground
            )
          }

          // Books grid - limit to max 6 featured books
          items(
            items = state.books.take(6),
            key = { it.id.toString() }
          ) { book ->
            val bookSongsScreen = rememberScreen(SharedScreens.BookSongs(book.id))
            HomeBookCard(
              book = book,
              onClick = { navigator.push(bookSongsScreen) }
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
          title = "Не удалось загрузить",
          message = "Проверьте подключение и попробуйте снова"
        )
      }
    }
  }

  // Number input modal
  if (showNumberInput && state is HomeUiState.Content) {
    NumberInputModal(
      books = state.books,
      onDismiss = { showNumberInput = false },
      onConfirm = { bookId, _ ->
        showNumberInput = false
        navigator.push(BookSongsScreen(bookId))
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
      label = "По номеру",
      onClick = onNumberSearchClick,
      modifier = Modifier.weight(1f)
    )
    QuickActionChip(
      icon = Icons.Default.TextFields,
      label = "По тексту",
      onClick = onTextSearchClick,
      modifier = Modifier.weight(1f)
    )
    QuickActionChip(
      icon = Icons.Default.History,
      label = "История",
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
      label = "Избранное",
      onClick = onFavoritesClick,
      modifier = Modifier.weight(1f)
    )
    QuickActionChip(
      icon = Icons.Outlined.Tag,
      label = "Теги",
      onClick = onTagsClick,
      modifier = Modifier.weight(1f)
    )
    // Балансируем сетку, чтобы визуально была ровная 3-колоночная линия как в первом ряду
    Spacer(Modifier.weight(1f))
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
    modifier = modifier.clickableWithScale(onClick = onClick),
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
private fun HomeBookCard(
  book: BookSummary,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val baseColor = remember(book.displayName.value) { generateBookColor(book.displayName.value) }
  val initials = remember(book.displayName.value) { getInitials(book.displayName.value) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithScale(onClick = onClick),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column {
      // Gradient header with initials
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.4f)
          .background(
            Brush.linearGradient(
              colors = listOf(
                baseColor,
                baseColor.copy(alpha = 0.7f)
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
          ),
          color = Color.White.copy(alpha = 0.95f)
        )
      }

      // Book info
      Column(
        modifier = Modifier.padding(MaterialTheme.spacing.md)
      ) {
        Text(
          text = book.displayName.value,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
          text = "${book.countSongs} песен",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
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
