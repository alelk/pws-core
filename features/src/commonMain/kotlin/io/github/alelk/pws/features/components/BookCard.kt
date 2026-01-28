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
 * Generates a stable color from a string (e.g., book name) using a predefined palette.
 * Colors are chosen to be content-friendly (not too bright, not too dark).
 */
fun generateBookColor(str: String): Color {
  val palette = listOf(
    Color(0xFFE57373), // Red 300
    Color(0xFFF06292), // Pink 300
    Color(0xFFBA68C8), // Purple 300
    Color(0xFF9575CD), // Deep Purple 300
    Color(0xFF7986CB), // Indigo 300
    Color(0xFF64B5F6), // Blue 300
    Color(0xFF4FC3F7), // Light Blue 300
    Color(0xFF4DB6AC), // Teal 300
    Color(0xFF81C784), // Green 300
    Color(0xFFAED581), // Light Green 300
    Color(0xFFFF8A65), // Deep Orange 300
    Color(0xFFA1887F), // Brown 300
    Color(0xFF90A4AE)  // Blue Grey 300
  )
  val index = kotlin.math.abs(str.hashCode()) % palette.size
  return palette[index]
}

/**
 * Extracts initials from a display name.
 */
fun getInitials(name: String): String {
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
  val baseColor = remember(displayName) { generateBookColor(displayName) }
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
  val baseColor = remember(displayName) { generateBookColor(displayName) }
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

