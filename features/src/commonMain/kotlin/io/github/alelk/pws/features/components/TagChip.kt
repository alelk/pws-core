package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.book_songs_count
import io.github.alelk.pws.features.resources.common_delete
import io.github.alelk.pws.features.resources.tag_chip_edit
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

/**
 * Color indicator that also conveys shape info for colorblind accessibility.
 * Shape is deterministically derived from the color value so the same tag
 * always gets the same shape.
 */
@Composable
fun TagColorIndicator(color: Color, size: Dp = 8.dp, modifier: Modifier = Modifier) {
  val pattern = (color.hashCode() and 0x7FFFFFFF) % 4
  when (pattern) {
    0 -> Box(modifier = modifier.size(size).clip(CircleShape).background(color))
    1 -> Icon(
      imageVector = Icons.Default.Star,
      contentDescription = null,
      tint = color,
      modifier = modifier.size(size + 2.dp)
    )
    2 -> Box(modifier = modifier.size(size).clip(RoundedCornerShape(2.dp)).background(color))
    else -> Box(
      modifier = modifier
        .size(size)
        .rotate(45f)
        .clip(RoundedCornerShape(1.dp))
        .background(color)
    )
  }
}

/**
 * Tag chip for display in song view and other places.
 */
@Composable
fun TagChip(
  name: String,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = modifier.clickable(onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onClick()
    }),
    shape = MaterialTheme.shapes.small,
    color = color.copy(alpha = 0.15f)
  ) {
    Row(
      modifier = Modifier.padding(
        horizontal = MaterialTheme.spacing.md,
        vertical = MaterialTheme.spacing.sm
      ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TagColorIndicator(color = color)
      Spacer(Modifier.width(MaterialTheme.spacing.sm))
      Text(
        text = name,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

/**
 * Selectable tag chip for editing screens.
 */
@Composable
fun SelectableTagChip(
  name: String,
  color: Color,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  FilterChip(
    modifier = modifier,
    selected = selected,
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onClick()
    },
    label = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TagColorIndicator(color = color)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Text(name)
      }
    },
    leadingIcon = if (selected) {
      {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(FilterChipDefaults.IconSize)
        )
      }
    } else null
  )
}

/**
 * Removable tag chip with delete button.
 */
@Composable
fun RemovableTagChip(
  name: String,
  color: Color,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.small,
    color = color.copy(alpha = 0.15f)
  ) {
    Row(
      modifier = Modifier.padding(
        start = MaterialTheme.spacing.md,
        end = MaterialTheme.spacing.xs,
        top = MaterialTheme.spacing.xs,
        bottom = MaterialTheme.spacing.xs
      ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TagColorIndicator(color = color)
      Spacer(Modifier.width(MaterialTheme.spacing.sm))
      Text(
        text = name,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      IconButton(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onRemove()
        },
        modifier = Modifier.size(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = stringResource(Res.string.common_delete),
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Flow row of tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsRow(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
  ) {
    content()
  }
}

/**
 * Tag list item for tags management screen.
 */
@Composable
fun TagListItem(
  name: String,
  color: Color,
  songCount: Int,
  onClick: () -> Unit,
  onEditClick: () -> Unit,
  modifier: Modifier = Modifier,
  isPredefined: Boolean = false
) {
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = modifier.clickable(onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onClick()
    }),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .padding(MaterialTheme.spacing.lg)
        .padding(end = MaterialTheme.spacing.sm),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Color indicator with shape pattern for colorblind accessibility
      TagColorIndicator(color = color, size = 12.dp)

      Spacer(Modifier.width(MaterialTheme.spacing.md))

      // Name and count
      Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )

      Text(
        text = stringResource(Res.string.book_songs_count, songCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (!isPredefined) {
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        IconButton(onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onEditClick()
        }) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(Res.string.tag_chip_edit),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
