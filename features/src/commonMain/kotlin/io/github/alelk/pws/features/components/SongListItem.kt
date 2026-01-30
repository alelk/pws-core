package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.features.theme.spacing

/**
 * Song list item with number badge, title, and optional indicators.
 */
@Composable
fun SongListItem(
  number: Int,
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEdited: Boolean = false,
  isFavorite: Boolean = false,
  showChevron: Boolean = true
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = MaterialTheme.spacing.listItemHorizontal,
          vertical = MaterialTheme.spacing.listItemVertical
        ),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
    ) {
      // Number badge
      NumberBadge(number = number)

      // Title and indicators
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (isEdited) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              modifier = Modifier.size(12.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Редактирована",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // Favorite indicator
      if (isFavorite) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "В избранном",
          modifier = Modifier.size(MaterialTheme.spacing.iconSm),
          tint = MaterialTheme.colorScheme.tertiary
        )
      }

      // Chevron
      if (showChevron) {
        Icon(
          imageVector = Icons.Outlined.ChevronRight,
          contentDescription = null,
          modifier = Modifier.size(MaterialTheme.spacing.iconMd),
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
      }
    }
  }
}

/**
 * Compact number badge for song number.
 */
@Composable
fun NumberBadge(
  number: Int,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(40.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.primaryContainer),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = number.toString(),
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.SemiBold
      ),
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
  }
}

/**
 * Song row with swipe actions support (for favorites/history).
 */
@Composable
fun SwipeableSongItem(
  number: Int?,
  title: String,
  onClick: () -> Unit,
  onFavoriteToggle: (() -> Unit)? = null,
  onDelete: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  isFavorite: Boolean = false,
  subtitle: String? = null
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = MaterialTheme.spacing.listItemHorizontal,
          vertical = MaterialTheme.spacing.listItemVertical
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Optional number badge
      if (number != null) {
        NumberBadge(number = number)
        Spacer(Modifier.width(MaterialTheme.spacing.md))
      }

      // Content
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Favorite toggle
      if (onFavoriteToggle != null) {
        IconButton(onClick = onFavoriteToggle) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Delete button
      if (onDelete != null) {
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Удалить",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

