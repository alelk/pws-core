package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_delete
import io.github.alelk.pws.features.resources.song_item_add_to_favorites
import io.github.alelk.pws.features.resources.song_item_edited
import io.github.alelk.pws.features.resources.song_item_in_favorites
import io.github.alelk.pws.features.resources.song_item_remove_from_favorites
import io.github.alelk.pws.features.resources.song_number_a11y
import io.github.alelk.pws.features.theme.NumberBadgeTextStyle
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

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
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("song-row-$number")
      .clickableWithScale(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
      }),
    color = MaterialTheme.colorScheme.surfaceContainerLow
  ) {
    Column {
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
              modifier = Modifier.semantics(mergeDescendants = true) { },
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
                text = stringResource(Res.string.song_item_edited),
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
            contentDescription = stringResource(Res.string.song_item_in_favorites),
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
          )
        }
      }

      HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )
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
  val songNumberA11y = stringResource(Res.string.song_number_a11y, number)
  Box(
    modifier = modifier
      .size(40.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.primaryContainer)
      .semantics { contentDescription = songNumberA11y },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = number.toString(),
      style = NumberBadgeTextStyle,
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
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("song-row-$number")
      .clickableWithScale(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
      }),
    color = MaterialTheme.colorScheme.surfaceContainerLow
  ) {
    Column {
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
          IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFavoriteToggle()
          }) {
            Icon(
              imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = if (isFavorite) {
                stringResource(Res.string.song_item_remove_from_favorites)
              } else {
                stringResource(Res.string.song_item_add_to_favorites)
              },
              tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Delete button — destructive haptic, не lightTap.
        if (onDelete != null) {
          IconButton(onClick = {
            haptic.confirm()
            onDelete()
          }) {
            Icon(
              imageVector = Icons.Outlined.Delete,
              contentDescription = stringResource(Res.string.common_delete),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )
    }
  }
}
