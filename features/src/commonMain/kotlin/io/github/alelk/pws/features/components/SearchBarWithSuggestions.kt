package io.github.alelk.pws.features.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.spacing


/**
 * Search bar with dropdown suggestions overlay.
 *
 * @param query Current search query
 * @param onQueryChange Called when query text changes
 * @param onSearch Called when user presses Enter/Search
 * @param suggestions List of suggestions to display
 * @param onSuggestionClick Called when user clicks a suggestion
 * @param isLoading Whether suggestions are being loaded
 * @param showSuggestions Whether to show the suggestions dropdown
 * @param autoFocus Whether to auto-focus the search field
 */
@Composable
fun SearchBarWithSuggestions(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  suggestions: List<SearchSuggestion>,
  onSuggestionClick: (SearchSuggestion) -> Unit,
  isLoading: Boolean = false,
  showSuggestions: Boolean = false,
  autoFocus: Boolean = false,
  modifier: Modifier = Modifier,
  placeholder: String = "Найти песню..."
) {
  val focusRequester = remember { FocusRequester() }

  // Auto-focus when requested
  LaunchedEffect(autoFocus) {
    if (autoFocus) {
      focusRequester.requestFocus()
    }
  }

  Box(modifier = modifier.zIndex(10f)) {
    Column {
      // Search field
      SearchInputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        isLoading = isLoading,
        focusRequester = focusRequester,
        placeholder = placeholder
      )

      // Suggestions dropdown
      AnimatedVisibility(
        visible = showSuggestions && (suggestions.isNotEmpty() || isLoading),
        enter = fadeIn() + slideInVertically { -it / 4 },
        exit = fadeOut() + slideOutVertically { -it / 4 }
      ) {
        SuggestionsDropdown(
          suggestions = suggestions,
          onSuggestionClick = onSuggestionClick,
          isLoading = isLoading
        )
      }
    }
  }
}

@Composable
private fun SearchInputField(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  isLoading: Boolean,
  focusRequester: FocusRequester,
  placeholder: String
) {
  TextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = Modifier
      .fillMaxWidth()
      .focusRequester(focusRequester),
    placeholder = {
      Text(
        text = placeholder,
        style = MaterialTheme.typography.bodyLarge
      )
    },
    leadingIcon = {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    trailingIcon = {
      when {
        isLoading -> {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
          )
        }
        query.isNotEmpty() -> {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Очистить",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    shape = RoundedCornerShape(28.dp),
    colors = TextFieldDefaults.colors(
      focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent
    )
  )
}

@Composable
private fun SuggestionsDropdown(
  suggestions: List<SearchSuggestion>,
  onSuggestionClick: (SearchSuggestion) -> Unit,
  isLoading: Boolean
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp)
      .shadow(
        elevation = 12.dp,
        shape = MaterialTheme.shapes.medium,
        clip = false
      ),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 350.dp)
    ) {
      items(
        items = suggestions,
        key = { it.songId.value }
      ) { suggestion ->
        SuggestionItemRow(
          suggestion = suggestion,
          onClick = { onSuggestionClick(suggestion) }
        )
        if (suggestion != suggestions.last()) {
          HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
          )
        }
      }

      if (isLoading && suggestions.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(MaterialTheme.spacing.lg),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun SuggestionItemRow(
  suggestion: SearchSuggestion,
  onClick: () -> Unit
) {
  val booksByNumber = suggestion.booksByNumber

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    color = Color.Transparent
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Song number badge (from first book reference)
      suggestion.primarySongNumber?.let { number ->
        Text(
          text = number.toString(),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.widthIn(min = 36.dp).padding(end = 12.dp)
        )
      } ?: run {
        // Fallback icon when no book reference
        Icon(
          imageVector = Icons.Outlined.MusicNote,
          contentDescription = null,
          modifier = Modifier.size(24.dp).padding(end = 12.dp),
          tint = MaterialTheme.colorScheme.primary
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        // Song name
        Text(
          text = suggestion.songName,
          style = MaterialTheme.typography.bodyMedium,
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
            // (other numbers will be shown as separate suggestions if needed)
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
