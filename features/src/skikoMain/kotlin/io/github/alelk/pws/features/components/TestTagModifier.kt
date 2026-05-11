package io.github.alelk.pws.features.components

import androidx.compose.ui.Modifier

/** No-op on non-Android platforms. */
actual fun Modifier.testTagsAsResourceId(): Modifier = this
