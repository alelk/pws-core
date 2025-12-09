package io.github.alelk.pws.features.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import io.github.alelk.pws.features.theme.spacing

/**
 * Modern search field with clear button.
 */
@Composable
fun SearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Поиск...",
  enabled: Boolean = true
) {
  TextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
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
      AnimatedVisibility(
        visible = query.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        IconButton(onClick = { onQueryChange("") }) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Очистить",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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

/**
 * Outlined search field variant.
 */
@Composable
fun OutlinedSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Поиск...",
  enabled: Boolean = true
) {
  OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
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
      AnimatedVisibility(
        visible = query.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        IconButton(onClick = { onQueryChange("") }) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Очистить",
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

