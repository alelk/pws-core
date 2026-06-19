package io.github.alelk.pws.domain.booklibrary.repository

import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.core.ids.BookId
import kotlinx.coroutines.flow.Flow

interface InstalledBookRepository {
    fun observeAll(): Flow<List<InstalledBook>>
    suspend fun getByBookId(bookId: BookId): InstalledBook?
    suspend fun upsert(book: InstalledBook)
    suspend fun upsertAll(books: List<InstalledBook>)
    suspend fun delete(bookId: BookId)
    suspend fun existsBySource(source: BookInstallSource): Boolean
}
