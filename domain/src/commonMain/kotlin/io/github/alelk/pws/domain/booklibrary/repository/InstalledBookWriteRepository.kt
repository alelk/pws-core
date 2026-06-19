package io.github.alelk.pws.domain.booklibrary.repository

import arrow.core.Either
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.core.ids.BookId

interface InstalledBookWriteRepository {
    suspend fun upsert(book: InstalledBook): Either<UpsertError, InstalledBook>
    suspend fun upsertAll(books: List<InstalledBook>): Either<UpsertError, List<InstalledBook>>
    suspend fun delete(bookId: BookId): Either<DeleteError, Unit>
}
