package io.github.alelk.pws.features.booklibrary

import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.DownloadState
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.core.ids.BookId

data class BookLibraryItem(
    val entry: BookCatalogEntry,
    val installed: InstalledBook?,
    val downloadState: DownloadState = DownloadState.Idle,
) {
    val bookId: BookId get() = entry.bookId
    val isBuiltIn: Boolean get() = installed?.source == BookInstallSource.ASSET
    val isInstalled: Boolean get() = installed != null
    val hasUpdate: Boolean
        get() = installed != null && !isBuiltIn && entry.bundleVersion > installed.bundleVersion
}

sealed class BookLibraryUiState {
    data object Loading : BookLibraryUiState()
    data class Content(
        val items: List<BookLibraryItem>,
        /** Book to highlight as "Recommended" on the empty library (locale match); null otherwise. */
        val recommendedBookId: BookId? = null,
    ) : BookLibraryUiState()
    data class Error(val message: String) : BookLibraryUiState()
}
