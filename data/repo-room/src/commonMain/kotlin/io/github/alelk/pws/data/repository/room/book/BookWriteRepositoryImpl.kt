package io.github.alelk.pws.data.repository.room.book

import arrow.core.Either
import io.github.alelk.pws.database.book.BookDao
import io.github.alelk.pws.database.book.BookEntity
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId

class BookWriteRepositoryImpl(private val bookDao: BookDao) : BookWriteRepository {

  override suspend fun create(bookCommand: CreateBookCommand): Either<CreateError, BookId> =
    runCatching {
      bookDao.insert(
        BookEntity(
          id = bookCommand.id,
          version = bookCommand.version,
          locales = bookCommand.locales,
          name = bookCommand.name.value,
          displayShortName = bookCommand.displayShortName.value,
          displayName = bookCommand.displayName.value,
          releaseDate = bookCommand.releaseDate,
          authors = bookCommand.authors.takeIf { it.isNotEmpty() },
          creators = bookCommand.creators.takeIf { it.isNotEmpty() },
          reviewers = bookCommand.reviewers.takeIf { it.isNotEmpty() },
          editors = bookCommand.editors.takeIf { it.isNotEmpty() },
          description = bookCommand.description,
          preface = bookCommand.preface,
        )
      )
      Either.Right(bookCommand.id)
    }.getOrElse { Either.Left(CreateError.UnknownError(it)) }

  override suspend fun update(bookCommand: UpdateBookCommand): Either<UpdateError, BookId> =
    runCatching {
      val existing = bookDao.getById(bookCommand.id)
        ?: return Either.Left(UpdateError.NotFound)
      val updated = existing.copy(
        version = bookCommand.version ?: existing.version,
        locales = bookCommand.locales ?: existing.locales,
        name = bookCommand.name?.value ?: existing.name,
        displayShortName = bookCommand.displayShortName?.value ?: existing.displayShortName,
        displayName = bookCommand.displayName?.value ?: existing.displayName,
        releaseDate = when (val rd = bookCommand.releaseDate) {
          OptionalField.Unchanged -> existing.releaseDate
          OptionalField.Clear -> null
          is OptionalField.Set -> rd.value
        },
        description = when (val d = bookCommand.description) {
          OptionalField.Unchanged -> existing.description
          OptionalField.Clear -> null
          is OptionalField.Set -> d.value
        },
        preface = when (val p = bookCommand.preface) {
          OptionalField.Unchanged -> existing.preface
          OptionalField.Clear -> null
          is OptionalField.Set -> p.value
        },
      )
      bookDao.update(updated)
      Either.Right(bookCommand.id)
    }.getOrElse { Either.Left(UpdateError.UnknownError(it)) }

  override suspend fun delete(bookId: BookId): Either<DeleteError, BookId> =
    runCatching {
      val existing = bookDao.getById(bookId)
        ?: return Either.Left(DeleteError.NotFound)
      bookDao.delete(existing)
      Either.Right(bookId)
    }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }
}

