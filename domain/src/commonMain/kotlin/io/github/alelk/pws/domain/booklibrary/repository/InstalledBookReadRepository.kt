package io.github.alelk.pws.domain.booklibrary.repository

import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.core.ids.BookId

interface InstalledBookReadRepository {
    suspend fun getByBookId(bookId: BookId): InstalledBook?
    suspend fun existsBySource(source: BookInstallSource): Boolean
}
