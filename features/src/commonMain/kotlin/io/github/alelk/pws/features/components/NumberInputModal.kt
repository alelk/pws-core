package io.github.alelk.pws.features.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_clear
import io.github.alelk.pws.features.resources.number_dropdown_idle
import io.github.alelk.pws.features.resources.number_dropdown_no_results
import io.github.alelk.pws.features.resources.number_hint_example
import io.github.alelk.pws.features.resources.number_hint_found_count
import io.github.alelk.pws.features.resources.number_hint_not_found
import io.github.alelk.pws.features.resources.number_hint_searching
import io.github.alelk.pws.features.resources.number_input_placeholder
import io.github.alelk.pws.features.resources.number_search_title
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

/**
 * Modal bottom sheet for entering a song number with live suggestions.
 *
 * Backed by [AppModalBottomSheet] for system-correct dismissal, a11y dialog role,
 * and drag-to-dismiss out of the box.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberInputModal(
  numberQuery: String,
  suggestions: List<SearchSuggestion>,
  isSearching: Boolean,
  onNumberChange: (String) -> Unit,
  onDismiss: () -> Unit,
  onSuggestionClick: (SearchSuggestion) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val focusRequester = remember { FocusRequester() }
  val normalizedQuery = numberQuery.filter { it.isDigit() }.take(4)

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  AppModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(MaterialTheme.spacing.lg)
        .padding(WindowInsets.navigationBars.asPaddingValues()),
    ) {
      Text(
        text = stringResource(Res.string.number_search_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Spacer(Modifier.height(MaterialTheme.spacing.md))

      NumberInputField(
        value = normalizedQuery,
        isSearching = isSearching,
        focusRequester = focusRequester,
        onValueChange = onNumberChange,
        onClear = { onNumberChange("") },
        onDone = { suggestions.firstOrNull()?.let(onSuggestionClick) },
      )

      Spacer(Modifier.height(MaterialTheme.spacing.sm))

      SuggestionsHint(
        query = normalizedQuery,
        suggestionsCount = suggestions.size,
        isSearching = isSearching,
      )

      Spacer(Modifier.height(MaterialTheme.spacing.sm))

      SuggestionsDropdown(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 180.dp, max = 300.dp),
        query = normalizedQuery,
        suggestions = suggestions,
        isSearching = isSearching,
        onSuggestionClick = onSuggestionClick,
      )

      Spacer(Modifier.height(MaterialTheme.spacing.md))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberInputField(
  value: String,
  isSearching: Boolean,
  focusRequester: FocusRequester,
  onValueChange: (String) -> Unit,
  onClear: () -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  OutlinedTextField(
    value = value,
    onValueChange = { input ->
      onValueChange(input.filter { it.isDigit() }.take(4))
    },
    modifier = modifier
      .fillMaxWidth()
      .focusRequester(focusRequester)
      .testTag("field:number-input"),
    singleLine = true,
    textStyle = MaterialTheme.typography.headlineSmall,
    placeholder = { Text(stringResource(Res.string.number_input_placeholder)) },
    leadingIcon = {
      Icon(
        imageVector = Icons.Default.Dialpad,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    trailingIcon = {
      when {
        isSearching -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        value.isNotEmpty() -> IconButton(onClick = onClear) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = stringResource(Res.string.common_clear),
          )
        }
      }
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(onDone = { onDone() }),
    shape = RoundedCornerShape(16.dp),
    colors = TextFieldDefaults.colors(
      focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
    ),
  )
}

@Composable
private fun SuggestionsHint(
  query: String,
  suggestionsCount: Int,
  isSearching: Boolean,
  modifier: Modifier = Modifier,
) {
  val hint = when {
    query.isBlank() -> stringResource(Res.string.number_hint_example)
    isSearching -> stringResource(Res.string.number_hint_searching)
    suggestionsCount > 0 -> stringResource(Res.string.number_hint_found_count, suggestionsCount)
    else -> stringResource(Res.string.number_hint_not_found)
  }

  Text(
    text = hint,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = modifier,
  )
}

@Composable
private fun SuggestionsDropdown(
  query: String,
  suggestions: List<SearchSuggestion>,
  isSearching: Boolean,
  onSuggestionClick: (SearchSuggestion) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
  ) {
    when {
      query.isBlank() -> EmptyDropdownState(text = stringResource(Res.string.number_dropdown_idle))
      isSearching -> Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
      }

      suggestions.isEmpty() -> EmptyDropdownState(text = stringResource(Res.string.number_dropdown_no_results))
      else -> {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          items(suggestions, key = { it.songId.value }) { suggestion ->
            val index = suggestions.indexOf(suggestion)
            NumberSuggestionItem(
              suggestion = suggestion,
              onClick = { onSuggestionClick(suggestion) },
              modifier = Modifier.testTag("number-suggestion-$index"),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyDropdownState(
  text: String,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(180.dp)
      .padding(MaterialTheme.spacing.lg),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun NumberSuggestionItem(
  suggestion: SearchSuggestion,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
      })
      .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val primaryRef = suggestion.bookReferences.firstOrNull()

    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.primaryContainer,
    ) {
      Text(
        text = primaryRef?.songNumber?.toString() ?: "-",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      )
    }

    Spacer(Modifier.width(MaterialTheme.spacing.md))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = suggestion.songName,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      if (suggestion.bookReferences.isNotEmpty()) {
        Text(
          text = suggestion.bookReferences
            .take(3)
            .joinToString("  ") { it.displayShortName },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
