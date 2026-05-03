package io.github.alelk.pws.features.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Wrapper around [ModalBottomSheet].
 *
 * Sets testTagsAsResourceId on the content root so that Compose testTags inside
 * the sheet are accessible via resource-id (needed by Maestro `id:` selectors).
 * ModalBottomSheet renders in a separate window and does not inherit the flag
 * set in MainActivity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: SheetState,
  containerColor: Color,
  content: @Composable () -> Unit
) {
  val stableContent = remember(content) { content }

  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    containerColor = containerColor
  ) {
    Box(modifier = Modifier.testTagsAsResourceId()) {
      stableContent()
    }
  }
}
