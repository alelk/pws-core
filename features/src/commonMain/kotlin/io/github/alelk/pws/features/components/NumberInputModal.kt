package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import io.github.alelk.pws.features.search.SearchSuggestion
import io.github.alelk.pws.features.theme.spacing

/**
 * Modal for entering song number with custom numpad and live suggestions.
 *
 * @param numberQuery Current number input string
 * @param suggestions List of song suggestions for the current number
 * @param isSearching Whether search is in progress
 * @param onNumberChange Called when user presses a digit or backspace
 * @param onDismiss Called when modal should be dismissed
 * @param onSuggestionClick Called when user taps a suggestion
 */
@Composable
fun NumberInputModal(
  numberQuery: String,
  suggestions: List<SearchSuggestion>,
  isSearching: Boolean,
  onNumberChange: (String) -> Unit,
  onDismiss: () -> Unit,
  onSuggestionClick: (SearchSuggestion) -> Unit,
  modifier: Modifier = Modifier
) {
  val focusRequester = FocusRequester()
  val normalizedQuery = numberQuery.filter { it.isDigit() }.take(4)

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  // Full screen overlay
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.5f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Bottom sheet content
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = false) { },
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(MaterialTheme.spacing.lg),
        horizontalAlignment = Alignment.Start
      ) {
        // Handle bar
        Box(
          modifier = Modifier
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        // Header with close button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Поиск по номеру",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Закрыть",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(Modifier.height(MaterialTheme.spacing.md))

        NumberInputField(
          value = normalizedQuery,
          isSearching = isSearching,
          focusRequester = focusRequester,
          onValueChange = onNumberChange,
          onClear = { onNumberChange("") },
          onDone = {
            suggestions.firstOrNull()?.let(onSuggestionClick)
          }
        )

        Spacer(Modifier.height(MaterialTheme.spacing.sm))

        SuggestionsHint(
          query = normalizedQuery,
          suggestionsCount = suggestions.size,
          isSearching = isSearching
        )

        Spacer(Modifier.height(MaterialTheme.spacing.sm))

        SuggestionsDropdown(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 300.dp),
          query = normalizedQuery,
          suggestions = suggestions,
          isSearching = isSearching,
          onSuggestionClick = onSuggestionClick
        )

        Spacer(Modifier.height(MaterialTheme.spacing.md))
      }
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
  modifier: Modifier = Modifier
) {
  OutlinedTextField(
    value = value,
    onValueChange = { input ->
      onValueChange(input.filter { it.isDigit() }.take(4))
    },
    modifier = modifier
      .fillMaxWidth()
      .focusRequester(focusRequester),
    singleLine = true,
    textStyle = MaterialTheme.typography.headlineSmall,
    placeholder = { Text("Введите номер") },
    leadingIcon = {
      Icon(
        imageVector = Icons.Default.Dialpad,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    trailingIcon = {
      when {
        isSearching -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        value.isNotEmpty() -> IconButton(onClick = onClear) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Очистить"
          )
        }
      }
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
      onDone = { onDone() }
    ),
    shape = RoundedCornerShape(16.dp),
    colors = TextFieldDefaults.colors(
      focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent
    )
  )
}

@Composable
private fun SuggestionsHint(
  query: String,
  suggestionsCount: Int,
  isSearching: Boolean,
  modifier: Modifier = Modifier
) {
  val hint = when {
    query.isBlank() -> "Введите номер песни, например 120"
    isSearching -> "Ищем песни..."
    suggestionsCount > 0 -> "Найдено: $suggestionsCount"
    else -> "Ничего не найдено"
  }

  Text(
    text = hint,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = modifier
  )
}

@Composable
private fun SuggestionsDropdown(
  query: String,
  suggestions: List<SearchSuggestion>,
  isSearching: Boolean,
  onSuggestionClick: (SearchSuggestion) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    when {
      query.isBlank() -> {
        EmptyDropdownState(text = "Подсказки появятся во время ввода")
      }
      isSearching -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        }
      }
      suggestions.isEmpty() -> {
        EmptyDropdownState(text = "Песни с таким номером не найдены")
      }
      else -> {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          items(suggestions, key = { it.songId.value }) { suggestion ->
            NumberSuggestionItem(
              suggestion = suggestion,
              onClick = { onSuggestionClick(suggestion) }
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
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(180.dp)
      .padding(MaterialTheme.spacing.lg),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun NumberSuggestionItem(
  suggestion: SearchSuggestion,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val primaryRef = suggestion.bookReferences.firstOrNull()

    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.primaryContainer
    ) {
      Text(
        text = primaryRef?.songNumber?.toString() ?: "-",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
      )
    }

    Spacer(Modifier.width(MaterialTheme.spacing.md))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = suggestion.songName,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      if (suggestion.bookReferences.isNotEmpty()) {
        Text(
          text = suggestion.bookReferences
            .take(3)
            .joinToString("  ") { it.displayShortName },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
