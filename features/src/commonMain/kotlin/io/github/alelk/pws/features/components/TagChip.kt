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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.features.theme.spacing

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
  Surface(
    modifier = modifier.clickable(onClick = onClick),
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
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
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
  FilterChip(
    modifier = modifier,
    selected = selected,
    onClick = onClick,
    label = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
        )
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
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(Modifier.width(MaterialTheme.spacing.sm))
      Text(
        text = name,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      IconButton(
        onClick = onRemove,
        modifier = Modifier.size(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Удалить",
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
  Surface(
    modifier = modifier.clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .padding(MaterialTheme.spacing.lg)
        .padding(end = MaterialTheme.spacing.sm),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Color indicator
      Box(
        modifier = Modifier
          .size(12.dp)
          .clip(CircleShape)
          .background(color)
      )

      Spacer(Modifier.width(MaterialTheme.spacing.md))

      // Name and count
      Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )

      Text(
        text = pluralizeSongsShort(songCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (!isPredefined) {
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        IconButton(onClick = onEditClick) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Редактировать",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

private fun pluralizeSongsShort(count: Int): String {
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

