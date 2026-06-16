package io.github.alelk.pws.features.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_back
import org.jetbrains.compose.resources.stringResource

/**
 * iOS-like large title top bar (good for Home / primary destinations).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLargeTopBar(
  title: String,
  canNavigateBack: Boolean,
  onNavigateBack: (() -> Unit)? = null,
  scrollBehavior: TopAppBarScrollBehavior? = null,
  actions: @Composable RowScope.() -> Unit = {},
) {
  LargeTopAppBar(
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.semantics { heading() }
      )
    },
    navigationIcon = {
      if (canNavigateBack && onNavigateBack != null) {
        IconButton(onClick = onNavigateBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
        }
      }
    },
    actions = actions,
    scrollBehavior = scrollBehavior,
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
    )
  )
}
