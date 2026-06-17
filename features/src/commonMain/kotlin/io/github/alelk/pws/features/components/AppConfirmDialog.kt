package io.github.alelk.pws.features.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.tags_cancel
import org.jetbrains.compose.resources.stringResource

/**
 * Uniform confirmation dialog used across destructive actions
 * (delete, clear, discard, etc.). Centralises styling so every confirmation
 * looks and behaves the same.
 */
@Composable
fun AppConfirmDialog(
  title: String,
  message: String,
  confirmLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  icon: ImageVector? = null,
  isDestructive: Boolean = true,
  dismissLabel: String = stringResource(Res.string.tags_cancel),
  confirmButtonTestTag: String? = null,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = icon?.let {
      {
        Icon(
          imageVector = it,
          contentDescription = null,
          tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
      }
    },
    title = { Text(title) },
    text = { Text(message) },
    confirmButton = {
      val buttonModifier = if (confirmButtonTestTag != null) {
        Modifier.testTagsAsResourceId().testTag(confirmButtonTestTag)
      } else Modifier
      TextButton(onClick = onConfirm, modifier = buttonModifier) {
        Text(
          text = confirmLabel,
          color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(dismissLabel)
      }
    },
  )
}
