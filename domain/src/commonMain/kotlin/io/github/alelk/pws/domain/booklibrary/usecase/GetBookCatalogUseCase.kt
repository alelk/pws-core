package io.github.alelk.pws.domain.booklibrary.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.repository.BookCatalogRepository
import io.github.alelk.pws.domain.core.error.ReadError

class GetBookCatalogUseCase(
    private val catalogRepository: BookCatalogRepository,
) {
    suspend operator fun invoke(): Either<ReadError, List<BookCatalogEntry>> =
        catalogRepository.getAvailableBooks()
}
