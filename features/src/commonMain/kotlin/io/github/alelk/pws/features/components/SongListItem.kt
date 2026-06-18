package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import io.github.alelk.pws.features.resources.song_detail_action_share
import io.github.alelk.pws.features.resources.song_item_add_to_favorites
import io.github.alelk.pws.features.resources.song_item_edited
import io.github.alelk.pws.features.resources.song_item_in_favorites
import io.github.alelk.pws.features.resources.song_item_remove_from_favorites
import io.github.alelk.pws.features.resources.song_number_a11y
import io.github.alelk.pws.features.song.detail.LocalSongDetailExternalActions
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
  showChevron: Boolean = true,
  onFavoriteToggle: (() -> Unit)? = null,
) {
  val haptic = LocalHapticFeedback.current
  val externalActions = LocalSongDetailExternalActions.current
  var showContextMenu by remember { mutableStateOf(false) }

  if (showContextMenu) {
    @OptIn(ExperimentalMaterial3Api::class)
    val sheetState = rememberModalBottomSheetState()
    @OptIn(ExperimentalMaterial3Api::class)
    AppModalBottomSheet(
      onDismissRequest = { showContextMenu = false },
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
      SongListContextMenu(
        number = number,
        title = title,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        onShare = externalActions?.let { actions ->
          { actions.shareText("$number. $title") }
        },
        onDismiss = { showContextMenu = false }
      )
    }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("song-row-$number")
      .clickableWithScale(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onClick()
        },
        onLongClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          showContextMenu = true
        },
      ),
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

@Composable
private fun SongListContextMenu(
  number: Int,
  title: String,
  isFavorite: Boolean,
  onFavoriteToggle: (() -> Unit)?,
  onShare: (() -> Unit)?,
  onDismiss: () -> Unit,
) {
  val spacing = MaterialTheme.spacing
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = spacing.md)
      .padding(WindowInsets.navigationBars.asPaddingValues())
  ) {
    Text(
      text = "$number. $title",
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

    if (onFavoriteToggle != null) {
      Surface(
        onClick = { onFavoriteToggle(); onDismiss() },
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = stringResource(if (isFavorite) Res.string.song_item_remove_from_favorites else Res.string.song_item_add_to_favorites),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    if (onShare != null) {
      Surface(
        onClick = { onShare(); onDismiss() },
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = stringResource(Res.string.song_detail_action_share),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    Spacer(Modifier.height(spacing.md))
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
