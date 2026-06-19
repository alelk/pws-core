package io.github.alelk.pws.data.repository.room.installed_book

import arrow.core.Either
import io.github.alelk.pws.database.installed_book.InstalledBookDao
import io.github.alelk.pws.database.installed_book.InstalledBookEntity
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.booklibrary.repository.InstalledBookRepository
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.core.ids.BookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InstalledBookRepositoryImpl(private val dao: InstalledBookDao) : InstalledBookRepository {

    override fun observeAll(): Flow<List<InstalledBook>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getByBookId(bookId: BookId): InstalledBook? =
        dao.getByBookId(bookId)?.toDomain()

    override suspend fun existsBySource(source: BookInstallSource): Boolean =
        dao.existsBySource(source)

    override suspend fun upsert(book: InstalledBook): Either<UpsertError, InstalledBook> =
        runCatching { dao.upsert(book.toEntity()); book }
            .fold(
                onSuccess = { Either.Right(it) },
                onFailure = { Either.Left(UpsertError.UnknownError(it)) },
            )

    override suspend fun upsertAll(books: List<InstalledBook>): Either<UpsertError, List<InstalledBook>> =
        runCatching { dao.upsertAll(books.map { it.toEntity() }); books }
            .fold(
                onSuccess = { Either.Right(it) },
                onFailure = { Either.Left(UpsertError.UnknownError(it)) },
            )

    override suspend fun delete(bookId: BookId): Either<DeleteError, Unit> =
        runCatching { dao.deleteByBookId(bookId) }
            .fold(
                onSuccess = { Either.Right(Unit) },
                onFailure = { Either.Left(DeleteError.UnknownError(it)) },
            )
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
