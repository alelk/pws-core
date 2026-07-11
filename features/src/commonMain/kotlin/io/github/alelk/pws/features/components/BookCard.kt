package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.book_card_a11y
import io.github.alelk.pws.features.resources.book_songs_count
import io.github.alelk.pws.features.theme.CategoryTints
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

/** Warm white readable on every category tint (cover title, avatar initials). */
private val OnTintColor = Color(0xFFF6F1E7)

/** Translucent "spine" strip on the left edge of a cover. */
private val SpineColor = Color(0x2BFFFFFF)

/**
 * Stable muted category tint from a key (book id).
 * Replaces the old random bright-gradient palette: same key → same warm tint.
 */
fun bookTint(key: String): Color =
  CategoryTints[kotlin.math.abs(key.hashCode()) % CategoryTints.size]

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
 * Book cover card: warm paper base + flat category-tinted header with a spine strip,
 * serif title on the tint, song count on the paper footer.
 * Used in a grid or list layout on the Books and Home screens.
 */
@Composable
fun BookCard(
  displayName: String,
  songCount: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  colorKey: String = displayName,
  aspectRatio: Float = 1.6f,
  testTag: String = "book-card-$displayName",
) {
  val tint = remember(colorKey) { bookTint(colorKey) }
  val a11yDescription = stringResource(Res.string.book_card_a11y, displayName, songCount)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
      .semantics { contentDescription = a11yDescription }
      .clickableWithScaleAndClip(shape = MaterialTheme.shapes.large, onClick = onClick),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column {
      // Tinted cover header with a spine strip and serif title
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(aspectRatio)
          .background(tint)
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .width(5.dp)
            .background(SpineColor)
        )
        Text(
          text = displayName,
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.W500,
            fontSize = 19.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp
          ),
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          color = OnTintColor,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = MaterialTheme.spacing.md, top = MaterialTheme.spacing.md, end = MaterialTheme.spacing.md)
        )
      }

      // Paper footer with the song count
      Text(
        text = stringResource(Res.string.book_songs_count, songCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm)
      )
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
  modifier: Modifier = Modifier,
  colorKey: String = displayName,
) {
  val tint = remember(colorKey) { bookTint(colorKey) }
  val initials = remember(displayName) { getInitials(displayName) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithScaleAndClip(shape = MaterialTheme.shapes.medium, onClick = onClick),
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
      // Mini-cover with initials
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(tint),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.W600
          ),
          color = OnTintColor
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
          text = stringResource(Res.string.book_songs_count, songCount),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
