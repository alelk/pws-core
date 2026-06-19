package io.github.alelk.pws.data.repository.room.installed_book

import io.github.alelk.pws.database.installed_book.InstalledBookDao
import io.github.alelk.pws.database.installed_book.InstalledBookEntity
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.booklibrary.repository.InstalledBookRepository
import io.github.alelk.pws.domain.core.ids.BookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InstalledBookRepositoryImpl(private val dao: InstalledBookDao) : InstalledBookRepository {

    override fun observeAll(): Flow<List<InstalledBook>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getByBookId(bookId: BookId): InstalledBook? =
        dao.getByBookId(bookId)?.toDomain()

    override suspend fun upsert(book: InstalledBook) =
        dao.upsert(book.toEntity())

    override suspend fun upsertAll(books: List<InstalledBook>) =
        dao.upsertAll(books.map { it.toEntity() })

    override suspend fun delete(bookId: BookId) =
        dao.deleteByBookId(bookId)

    override suspend fun existsBySource(source: BookInstallSource): Boolean =
        dao.existsBySource(source)
}

private fun InstalledBookEntity.toDomain() = InstalledBook(
    bookId = bookId,
    source = source,
    installedAt = installedAt,
    bundleVersion = bundleVersion,
)

private fun InstalledBook.toEntity() = InstalledBookEntity(
    bookId = bookId,
    bundleVersion = bundleVersion,
    installedAt = installedAt,
    source = source,
)
