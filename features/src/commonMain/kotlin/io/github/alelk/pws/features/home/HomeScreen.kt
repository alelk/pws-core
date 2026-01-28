package io.github.alelk.pws.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.features.book.songs.BookSongsScreen
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.NumberInputModal
import io.github.alelk.pws.features.components.SearchBarWithSuggestions
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

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    when (state) {
      HomeUiState.Loading -> {
        LoadingContent(message = "Загрузка...")
      }

      is HomeUiState.Content -> {
        // Main scrollable content with search bar inside
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 140.dp),
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.screenHorizontal,
            vertical = MaterialTheme.spacing.md
          ),
          horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
        ) {
          // Header with title
          item(span = { GridItemSpan(maxLineSpan) }) {
            HomeHeader()
          }

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

          // Quick action buttons
          item(span = { GridItemSpan(maxLineSpan) }) {
            QuickActionButtons(
              onNumberSearchClick = { showNumberInput = true },
              onTextSearchClick = {
                if (searchQuery.isNotBlank()) {
                  val screen = ScreenRegistry.get(SharedScreens.SearchResults(searchQuery))
                  onClearSearch()
                  navigator.push(screen)
                  }
                }
              )
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

            // Books grid
            items(
              items = state.books,
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
      onConfirm = { bookId, songNumber ->
        showNumberInput = false
        // Navigate to book songs screen - the user will find the song by number
        // TODO: Implement direct navigation to song by number when song lookup is available
        navigator.push(BookSongsScreen(bookId))
      }
    )
  }
}

@Composable
private fun HomeHeader() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = MaterialTheme.spacing.lg)
  ) {
    Text(
      text = "🎵 Псаломщик",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold
      ),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(MaterialTheme.spacing.xs))
    Text(
      text = "Найди любимую песню",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}


@Composable
private fun QuickActionButtons(
  onNumberSearchClick: () -> Unit,
  onTextSearchClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(top = MaterialTheme.spacing.md),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
  ) {
    QuickActionButton(
      icon = Icons.Default.Dialpad,
      title = "По номеру",
      subtitle = "123",
      onClick = onNumberSearchClick,
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier.weight(1f)
    )
    QuickActionButton(
      icon = Icons.Default.TextFields,
      title = "По тексту",
      subtitle = "ABC",
      onClick = onTextSearchClick,
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier
) {
  ElevatedCard(
    modifier = modifier
      .clip(MaterialTheme.shapes.large)
      .clickable(onClick = onClick),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.elevatedCardColors(
      containerColor = containerColor
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(MaterialTheme.spacing.md),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = contentColor
      )
      Spacer(Modifier.width(MaterialTheme.spacing.sm))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          color = contentColor,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = contentColor.copy(alpha = 0.7f)
        )
      }
    }
  }
}

/**
 * Generates a color from book name.
 */
private fun bookNameToColor(name: String): Color {
  val hash = name.hashCode()
  val hue = (hash and 0xFF) / 255f * 360f
  val saturation = 0.45f + (((hash shr 8) and 0xFF) / 255f) * 0.15f
  val lightness = 0.4f + (((hash shr 16) and 0xFF) / 255f) * 0.1f
  return Color.hsl(hue, saturation, lightness)
}

/**
 * Extracts initials from book name.
 */
private fun getInitials(name: String): String {
  val words = name.split(" ", "-").filter { it.isNotBlank() }
  return when {
    words.isEmpty() -> "?"
    words.size == 1 -> words[0].take(2).uppercase()
    else -> words.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
  }
}

@Composable
private fun HomeBookCard(
  book: BookSummary,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val baseColor = remember(book.displayName.value) { bookNameToColor(book.displayName.value) }
  val initials = remember(book.displayName.value) { getInitials(book.displayName.value) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
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
