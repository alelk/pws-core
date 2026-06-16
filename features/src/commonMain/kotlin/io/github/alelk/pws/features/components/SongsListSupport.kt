package io.github.alelk.pws.features.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Standard divider used inside song lists: indented to clear the number badge,
 * faint outline-variant tint. Use between rows.
 */
@Composable
fun SongsListDivider(modifier: Modifier = Modifier) {
  HorizontalDivider(
    modifier = modifier.padding(start = 72.dp),
    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
  )
}

/**
 * Adds a bottom spacer item to a song list that leaves room under the
 * bottom navigation bar. Drop this as the final `item { }` in a [LazyListScope]
 * to keep the last row scrollable past the nav bar.
 */
fun LazyListScope.bottomNavSpacer() {
  item { Spacer(Modifier.height(80.dp)) }
}
