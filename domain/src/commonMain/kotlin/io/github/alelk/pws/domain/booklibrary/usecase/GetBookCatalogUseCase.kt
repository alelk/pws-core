package io.github.alelk.pws.domain.booklibrary.usecase

import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.booklibrary.repository.BookCatalogRepository
import io.github.alelk.pws.domain.booklibrary.repository.InstalledBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class BookLibraryItem(
    val entry: BookCatalogEntry,
    val installed: InstalledBook?,
) {
    val isBuiltIn: Boolean get() = installed?.source == BookInstallSource.ASSET
    val isInstalled: Boolean get() = installed != null
}

class GetBookCatalogUseCase(
    private val catalogRepository: BookCatalogRepository,
    private val installedBookRepository: InstalledBookRepository,
) {
    suspend operator fun invoke(): Pair<List<BookCatalogEntry>, Flow<List<InstalledBook>>> =
        catalogRepository.getAvailableBooks() to installedBookRepository.observeAll()

    fun observeInstalled(): Flow<List<InstalledBook>> = installedBookRepository.observeAll()

    suspend fun getCatalog(): List<BookCatalogEntry> = catalogRepository.getAvailableBooks()
}
