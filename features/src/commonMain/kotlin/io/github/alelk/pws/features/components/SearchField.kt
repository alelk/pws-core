package io.github.alelk.pws.features.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_clear
import io.github.alelk.pws.features.resources.search_mode_numbers
import io.github.alelk.pws.features.resources.search_mode_words
import io.github.alelk.pws.features.resources.search_placeholder
import io.github.alelk.pws.features.theme.NumberBadgeTextStyle
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

/**
 * Modern search field with clear button.
 *
 * When [onNumberModeToggle] is set, the field shows a mono «123 / abc» pill that
 * switches the keyboard between words and digits (дизайн-система, кадр 7).
 */
@Composable
fun SearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  enabled: Boolean = true,
  numberMode: Boolean = false,
  onNumberModeToggle: (() -> Unit)? = null
) {
  val resolvedPlaceholder = placeholder ?: stringResource(Res.string.search_placeholder)

  TextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.fillMaxWidth().testTag("field:search"),
    enabled = enabled,
    placeholder = {
      Text(
        text = resolvedPlaceholder,
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
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onNumberModeToggle != null) {
          SearchModeBadge(numberMode = numberMode, onClick = onNumberModeToggle)
        }
        AnimatedVisibility(
          visible = query.isNotEmpty(),
          enter = fadeIn(),
          exit = fadeOut()
        ) {
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
    keyboardOptions = KeyboardOptions(
      keyboardType = if (numberMode) KeyboardType.Number else KeyboardType.Text,
      imeAction = ImeAction.Search
    ),
    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    shape = MaterialTheme.shapes.extraLarge,
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

/** «123 / abc» keyboard-mode switch: the label shows the mode it switches TO. */
@Composable
private fun SearchModeBadge(
  numberMode: Boolean,
  onClick: () -> Unit,
) {
  val description = stringResource(
    if (numberMode) Res.string.search_mode_words else Res.string.search_mode_numbers
  )
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHighest,
    modifier = Modifier
      .padding(end = MaterialTheme.spacing.xs)
      .testTag("action:search-mode-toggle")
      .semantics { contentDescription = description }
  ) {
    Text(
      text = if (numberMode) "abc" else "123",
      style = NumberBadgeTextStyle,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

/**
 * Outlined search field variant.
 */
@Composable
fun OutlinedSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  enabled: Boolean = true
) {
  val resolvedPlaceholder = placeholder ?: stringResource(Res.string.search_placeholder)

  OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
    placeholder = {
      Text(
        text = resolvedPlaceholder,
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
      AnimatedVisibility(
        visible = query.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        IconButton(onClick = { onQueryChange("") }) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = stringResource(Res.string.common_clear),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    shape = MaterialTheme.shapes.medium,
    colors = OutlinedTextFieldDefaults.colors()
  )
}

