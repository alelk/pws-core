package io.github.alelk.pws.features.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_clear
import io.github.alelk.pws.features.resources.search_find_song_placeholder
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource


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
  placeholder: String? = null
) {
  val resolvedPlaceholder = placeholder ?: stringResource(Res.string.search_find_song_placeholder)
  val focusRequester = remember { FocusRequester() }
  val expanded = showSuggestions && (suggestions.isNotEmpty() || isLoading)

  // Auto-focus when requested
  LaunchedEffect(autoFocus) {
    if (autoFocus) {
      focusRequester.requestFocus()
    }
  }

  // Column to anchor DropdownMenu properly
  Column(modifier = modifier.fillMaxWidth()) {
    // Search field
    SearchInputField(
      query = query,
      onQueryChange = onQueryChange,
      onSearch = onSearch,
      isLoading = isLoading,
      focusRequester = focusRequester,
      placeholder = resolvedPlaceholder
    )

    // DropdownMenu for suggestions - renders as overlay
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { /* Dismiss logic should be handled by parent */ },
      properties = PopupProperties(focusable = false), // Allow typing while menu is open
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .heightIn(max = 350.dp)
    ) {
      if (isLoading && suggestions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.lg)
            .testTagsAsResourceId(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
      } else {
        Box(modifier = Modifier.testTagsAsResourceId()) {
          Column {
            suggestions.forEachIndexed { index, suggestion ->
              SuggestionDropdownItem(
                suggestion = suggestion,
                onClick = { onSuggestionClick(suggestion) },
                modifier = Modifier.testTag("home-suggestion-$index")
              )
              if (index < suggestions.lastIndex) {
                HorizontalDivider(
                  modifier = Modifier.padding(start = 56.dp),
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
              }
            }
          }
        }
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
      .focusRequester(focusRequester)
      .testTag("field:home-search"),
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
              contentDescription = stringResource(Res.string.common_clear),
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
private fun SuggestionDropdownItem(
  suggestion: SearchSuggestion,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  DropdownMenuItem(
    modifier = modifier,
    text = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
      ) {
        // Song number badge
        suggestion.primarySongNumber?.let { number ->
          Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(min = 36.dp).padding(end = 12.dp)
          )
        } ?: run {
          Icon(
            imageVector = Icons.Outlined.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(end = 12.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = suggestion.songName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
          )

          suggestion.bookReferences.firstOrNull()?.let { ref ->
            Text(
              text = ref.displayShortName,
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
    },
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onClick()
    }
  )
}
