package io.github.alelk.pws.features.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alelk.pws.features.theme.spacing

/**
 * Full-screen loading indicator with fade animation.
 */
@Composable
fun LoadingContent(
  modifier: Modifier = Modifier,
  message: String? = null
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(48.dp),
        strokeWidth = 3.dp
      )
      if (message != null) {
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Text(
          text = message,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Empty state placeholder with icon and message.
 */
@Composable
fun EmptyContent(
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Outlined.FolderOff,
  title: String,
  subtitle: String? = null,
  action: (@Composable () -> Unit)? = null
) {
  Box(
    modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.xl),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(64.dp).alpha(0.6f),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(Modifier.height(MaterialTheme.spacing.lg))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
      if (subtitle != null) {
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
      if (action != null) {
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        action()
      }
    }
  }
}

/**
 * Search empty state.
 */
@Composable
fun SearchEmptyContent(
  modifier: Modifier = Modifier,
  query: String
) {
  EmptyContent(
    modifier = modifier,
    icon = Icons.Outlined.SearchOff,
    title = "Ничего не найдено",
    subtitle = "По запросу \"$query\" ничего не найдено. Попробуйте изменить поисковый запрос."
  )
}

/**
 * Error state with retry button.
 */
@Composable
fun ErrorContent(
  modifier: Modifier = Modifier,
  title: String = "Произошла ошибка",
  message: String? = null,
  onRetry: (() -> Unit)? = null
) {
  Box(
    modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.xl),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.ErrorOutline,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.error
      )
      Spacer(Modifier.height(MaterialTheme.spacing.lg))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
      if (message != null) {
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
          text = message,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
      if (onRetry != null) {
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Button(onClick = onRetry) {
          Text("Повторить")
        }
      }
    }
  }
}

/**
 * Animated content visibility wrapper.
 */
@Composable
fun AnimatedContent(
  visible: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  AnimatedVisibility(
    visible = visible,
    modifier = modifier,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically()
  ) {
    content()
  }
}

