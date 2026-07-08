package io.github.alelk.pws.features.booklibrary

import androidx.compose.runtime.staticCompositionLocalOf

data class BookLibraryExternalActions(
    val onImportFromFile: () -> Unit,
)

val LocalBookLibraryExternalActions = staticCompositionLocalOf<BookLibraryExternalActions?> { null }
