package io.github.alelk.pws.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.donation_prompt_action_donate
import io.github.alelk.pws.features.resources.donation_prompt_action_later
import io.github.alelk.pws.features.resources.donation_prompt_message
import io.github.alelk.pws.features.resources.donation_prompt_title
import org.jetbrains.compose.resources.stringResource

/**
 * Modal dialog asking the user to support the app via Boosty.
 *
 * @param onDonate Called when the user taps "Support on Boosty".
 * @param onDismiss Called when the user taps "Later".
 */
@Composable
fun DonationPromptDialog(
  onDonate: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
    },
    title = {
      Text(text = stringResource(Res.string.donation_prompt_title))
    },
    text = {
      Text(text = stringResource(Res.string.donation_prompt_message))
    },
    confirmButton = {
      FilledTonalButton(
        onClick = onDonate,
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
      ) {
        Text(text = stringResource(Res.string.donation_prompt_action_donate))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(Res.string.donation_prompt_action_later))
      }
    },
  )
}

