package io.github.alelk.pws.domain.booklibrary.usecase

import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface InstallBookUseCase {
    operator fun invoke(entry: BookCatalogEntry): Flow<DownloadState>
}
