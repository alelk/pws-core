package io.github.alelk.pws.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.donation_prompt_action_donate
import io.github.alelk.pws.features.resources.donation_prompt_action_later
import io.github.alelk.pws.features.resources.donation_prompt_message
import io.github.alelk.pws.features.resources.donation_prompt_title
import org.jetbrains.compose.resources.stringResource

/**
 * Inline card shown at the bottom of the song content asking for a donation.
 *
 * Non-intrusive — part of the scrollable song content, not a modal overlay.
 *
 * @param onDonate Called when the user taps "Support on Boosty".
 * @param onDismiss Called when the user taps the close (×) button. Suppresses for N views.
 */
@Composable
fun DonationPromptCard(
  onDonate: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ),
    shape = MaterialTheme.shapes.large,
  ) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
      // Title row with close button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = stringResource(Res.string.donation_prompt_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(Res.string.donation_prompt_action_later),
            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
          )
        }
      }

      // Message
      Text(
        text = stringResource(Res.string.donation_prompt_message),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
        modifier = Modifier.padding(end = 12.dp),
      )

      Spacer(Modifier.width(8.dp))

      // Donate button
      FilledTonalButton(
        onClick = onDonate,
        modifier = Modifier.padding(top = 8.dp, end = 12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
      ) {
        Text(text = stringResource(Res.string.donation_prompt_action_donate))
      }
    }
  }
}

