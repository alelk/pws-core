package io.github.alelk.pws.domain.songnumber.usecase

import arrow.core.Either
import arrow.core.raise.either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ReplaceAllError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.model.ReplaceAllSuccess
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository

/**
 * Use case: Replace all existing associations for a book with the provided set.
 */
class ReplaceAllBookSongNumbersUseCase(
  val readRepository: SongNumberReadRepository,
  val writeRepository: SongNumberWriteRepository,
  val txRunner: TransactionRunner
) {
  suspend operator fun invoke(bookId: BookId, assignments: Collection<SongNumberLink>): Either<ReplaceAllError, ReplaceAllSuccess<SongNumberLink>> =
    txRunner.inRwTransaction {
      either {
        val existingLinks = readRepository.getAllByBookId(bookId).toSet()
        val targetLinkSongIds = assignments.map { it.songId }.toSet()
        val existingLinkSongIds = existingLinks.map { it.songId }.toSet()

        val unchangedLinks = assignments intersect existingLinks
        val linksToDelete = existingLinks.filterNot { it.songId in targetLinkSongIds }
        val linksToCreate = assignments.filterNot { it.songId in existingLinkSongIds }
        val linksToUpdate = (assignments - linksToCreate.toSet() - unchangedLinks.toSet())

        for (link in linksToUpdate) {
          writeRepository.update(bookId, link).mapLeft { err ->
            when (err) {
              is UpdateError.NotFound -> error("illegal state: updating song number not found: $bookId $link")
              is UpdateError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is UpdateError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
        }
        for (link in linksToCreate) {
          writeRepository.create(bookId, link).mapLeft { err ->
            when (err) {
              is CreateError.AlreadyExists -> error("illegal state: creating song number already exists: $bookId $link")
              is CreateError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is CreateError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
        }
        for (link in linksToDelete) {
          writeRepository.delete(bookId, link.songId).mapLeft { err ->
            when (err) {
              is DeleteError.NotFound -> error("illegal state: deleting song number not found: $bookId $link")
              is DeleteError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is DeleteError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
        }

        ReplaceAllSuccess(
          created = linksToCreate.toList(),
          updated = linksToUpdate.toList(),
          unchanged = unchangedLinks.toList(),
          deleted = linksToDelete
        )
      }
    }
}