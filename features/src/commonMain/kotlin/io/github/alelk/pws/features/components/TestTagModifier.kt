package io.github.alelk.pws.features.components

import androidx.compose.ui.Modifier

/**
 * Marks this subtree so that Compose testTags are exposed as resource-id in the
 * Android accessibility / UI-Automator tree (needed by Maestro `id:` selectors).
 *
 * On non-Android platforms this is a no-op, because the concept of resource-id
 * does not exist there.
 *
 * Use this on the root of any composable that renders in a separate window
 * (Dialog, ModalBottomSheet) where the flag set in MainActivity is not inherited.
 */
expect fun Modifier.testTagsAsResourceId(): Modifier

