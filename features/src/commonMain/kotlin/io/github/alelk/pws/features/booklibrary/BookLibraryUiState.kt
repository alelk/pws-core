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
}

sealed class BookLibraryUiState {
    data object Loading : BookLibraryUiState()
    data class Content(val items: List<BookLibraryItem>) : BookLibraryUiState()
    data class Error(val message: String) : BookLibraryUiState()
}
