package io.github.alelk.pws.domain.booklibrary.repository

import arrow.core.Either
import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.core.error.ReadError

interface BookCatalogRepository {
    suspend fun getAvailableBooks(): Either<ReadError, List<BookCatalogEntry>>
}
