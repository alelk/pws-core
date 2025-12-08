package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alelk.pws.features.theme.spacing

/**
 * Generates a stable color from a string (e.g., book name).
 */
private fun stringToColor(str: String): Color {
  val hash = str.hashCode()
  val hue = (hash and 0xFF) / 255f * 360f
  val saturation = 0.4f + (((hash shr 8) and 0xFF) / 255f) * 0.2f
  val lightness = 0.35f + (((hash shr 16) and 0xFF) / 255f) * 0.15f
  return Color.hsl(hue, saturation, lightness)
}

/**
 * Extracts initials from a display name.
 */
private fun getInitials(name: String): String {
  val words = name.split(" ", "-").filter { it.isNotBlank() }
  return when {
    words.isEmpty() -> "?"
    words.size == 1 -> words[0].take(2).uppercase()
    else -> words.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
  }
}

/**
 * Modern book card with gradient background and initials.
 * Used in a grid or list layout on the Books screen.
 */
@Composable
fun BookCard(
  displayName: String,
  songCount: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val baseColor = remember(displayName) { stringToColor(displayName) }
  val initials = remember(displayName) { getInitials(displayName) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column {
      // Gradient header with initials
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.6f)
          .background(
            Brush.linearGradient(
              colors = listOf(
                baseColor,
                baseColor.copy(alpha = 0.7f)
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
          ),
          color = Color.White.copy(alpha = 0.9f)
        )
      }

      // Text content
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(MaterialTheme.spacing.md)
      ) {
        Text(
          text = displayName,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
          text = pluralizeSongs(songCount),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Compact horizontal book card for list layouts.
 */
@Composable
fun BookListItem(
  displayName: String,
  songCount: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val baseColor = remember(displayName) { stringToColor(displayName) }
  val initials = remember(displayName) { getInitials(displayName) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(MaterialTheme.spacing.md),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with initials
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(baseColor, baseColor.copy(alpha = 0.7f))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
          ),
          color = Color.White
        )
      }

      Spacer(Modifier.width(MaterialTheme.spacing.md))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = displayName,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = pluralizeSongs(songCount),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Russian pluralization for songs count.
 */
private fun pluralizeSongs(count: Int): String {
  val lastTwo = count % 100
  val lastOne = count % 10
  val word = when {
    lastTwo in 11..19 -> "песен"
    lastOne == 1 -> "песня"
    lastOne in 2..4 -> "песни"
    else -> "песен"
  }
  return "$count $word"
}

