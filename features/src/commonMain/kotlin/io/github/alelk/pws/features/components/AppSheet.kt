package io.github.alelk.pws.features.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Wrapper around [ModalBottomSheet] to isolate Material3 API drift between versions.
 *
 * Rationale: Compose Material3 has changed bottom-sheet state APIs multiple times.
 * We keep a minimal API here that works across versions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: SheetState,
  containerColor: Color,
  content: @Composable () -> Unit
) {
  // Keep a stable reference to content lambda to reduce recompositions in some versions.
  val stableContent = remember(content) { content }

  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    containerColor = containerColor
  ) {
    stableContent()
  }
}
