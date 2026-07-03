package io.github.alelk.pws.features.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_clear
import io.github.alelk.pws.features.resources.home_quick_number
import io.github.alelk.pws.features.resources.search_find_song_placeholder
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.NumberBadgeTextStyle
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

/**
 * Hero search field for the Home screen.
 *
 * Live results render inline in the Home list (single surface, like the Search
 * tab) — this component is only the field itself.
 *
 * @param onNumberModeClick When set, an empty field shows a "123" badge switching to number input
 */
@Composable
fun HomeSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  isLoading: Boolean = false,
  autoFocus: Boolean = false,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  onNumberModeClick: (() -> Unit)? = null
) {
  val resolvedPlaceholder = placeholder ?: stringResource(Res.string.search_find_song_placeholder)
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(autoFocus) {
    if (autoFocus) {
      focusRequester.requestFocus()
    }
  }

  TextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier
      .fillMaxWidth()
      .focusRequester(focusRequester)
      .testTag("field:home-search"),
    placeholder = {
      Text(
        text = resolvedPlaceholder,
        style = MaterialTheme.typography.bodyLarge
      )
    },
    leadingIcon = {
      Icon(
        imageVector = Lucide.Search,
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
              imageVector = Lucide.X,
              contentDescription = stringResource(Res.string.common_clear),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        onNumberModeClick != null -> {
          NumberModeBadge(onClick = onNumberModeClick)
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

/**
 * "123" pill inside the search field — switches to the number-pad input
 * (design system: the "123 / abc" pill toggles digits/words).
 */
@Composable
private fun NumberModeBadge(onClick: () -> Unit) {
  val haptic = LocalHapticFeedback.current
  val label = stringResource(Res.string.home_quick_number)
  Surface(
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onClick()
    },
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHighest,
    modifier = Modifier
      .padding(end = MaterialTheme.spacing.xs)
      .testTag("action:number-search")
      .semantics { contentDescription = label }
  ) {
    Text(
      text = "123",
      style = NumberBadgeTextStyle,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

/**
 * Live search suggestion row rendered inline in the Home list —
 * same row language as the Search tab: number, title, book, snippet.
 */
@Composable
fun SearchSuggestionRow(
  suggestion: SearchSuggestion,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
      }),
    color = Color.Transparent
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = MaterialTheme.spacing.xs,
          vertical = MaterialTheme.spacing.listItemVertical
        ),
      verticalAlignment = Alignment.Top
    ) {
      suggestion.primarySongNumber?.let { number ->
        Text(
          text = number.toString(),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.widthIn(min = 36.dp).padding(end = MaterialTheme.spacing.md)
        )
      } ?: run {
        Icon(
          imageVector = Lucide.Music,
          contentDescription = null,
          modifier = Modifier.size(24.dp).padding(end = MaterialTheme.spacing.md),
          tint = MaterialTheme.colorScheme.primary
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = suggestion.songName,
          style = MaterialTheme.typography.bodyLarge,
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
  }
}

/** Centered spinner row shown while live suggestions are loading. */
@Composable
fun SearchSuggestionsLoadingRow(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(MaterialTheme.spacing.lg),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(modifier = Modifier.size(24.dp))
  }
}
