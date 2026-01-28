package io.github.alelk.pws.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.domain.history.model.SongHistorySummary
import io.github.alelk.pws.features.theme.spacing

@Composable
fun RecentSongCard(
  song: SongHistorySummary,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .width(140.dp)
      .clickable(onClick = onClick),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier.padding(MaterialTheme.spacing.md)
    ) {
      // Song Number Badge
      Box(
        modifier = Modifier
          .widthIn(min = 32.dp)
          .height(32.dp)
          .clip(MaterialTheme.shapes.small)
          .background(MaterialTheme.colorScheme.primaryContainer)
          .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = song.songNumber.toString(),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }

      Spacer(Modifier.height(MaterialTheme.spacing.sm))

      // Song Title
      Text(
        text = song.songName,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        minLines = 2
      )

      Spacer(Modifier.height(MaterialTheme.spacing.xs))

      // Book Name (Short) - using bookDisplayName from SongHistorySummary
      Text(
        text = song.bookDisplayName,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
