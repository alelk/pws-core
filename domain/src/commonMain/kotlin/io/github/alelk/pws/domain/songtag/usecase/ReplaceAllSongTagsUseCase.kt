package io.github.alelk.pws.domain.songtag.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.getOrElse
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ReplaceAllError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.model.ReplaceAllSuccess
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository

/**
 * Use case: Replace all existing tag associations for a song with the provided set.
 *  - Any missing tags are removed
 *  - Any new tags are inserted.
 *
 * Applied atomically.
 * @param ID The type of TagId this use case works with
 */
class ReplaceAllSongTagsUseCase<ID : TagId>(
  private val readRepository: SongTagReadRepository<ID>,
  private val writeRepository: SongTagWriteRepository<ID>,
  private val txRunner: TransactionRunner
) {
  private class RollbackException(val error: ReplaceAllError) : Exception()

  suspend operator fun invoke(songId: SongId, tagIds: Set<ID>): Either<ReplaceAllError, ReplaceAllSuccess<SongTagAssociation<ID>>> =
    try {
      txRunner.inRwTransaction {
        val existingTagIds = readRepository.getTagIdsBySongId(songId)
        val existingTagIdsMap = existingTagIds.associateBy { it.identifier }
        val newTagIdsMap = tagIds.associateBy { it.identifier }

        val tagIdsToDelete = existingTagIdsMap.keys - newTagIdsMap.keys
        val tagIdsToCreate = newTagIdsMap.keys - existingTagIdsMap.keys
        val unchangedTagIds = existingTagIdsMap.keys intersect newTagIdsMap.keys

        val created = mutableListOf<SongTagAssociation<ID>>()
        for (tagIdStr in tagIdsToCreate) {
          val tagId = newTagIdsMap.getValue(tagIdStr)
          val res = writeRepository.create(songId, tagId).getOrElse { err ->
            val replaceAllError = when (err) {
              is CreateError.AlreadyExists -> ReplaceAllError.UnknownError(message = "illegal state: creating song-tag already exists: $songId $tagId")
              is CreateError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is CreateError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
            throw RollbackException(replaceAllError)
          }
          created.add(res)
        }

        val deleted = mutableListOf<SongTagAssociation<ID>>()
        for (tagIdStr in tagIdsToDelete) {
          val tagId = existingTagIdsMap.getValue(tagIdStr)
          val res = writeRepository.delete(songId, tagId).getOrElse { err ->
            val replaceAllError = when (err) {
              is DeleteError.NotFound -> ReplaceAllError.UnknownError(message = "illegal state: deleting song-tag not found: $songId $tagId")
              is DeleteError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is DeleteError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
            throw RollbackException(replaceAllError)
          }
          deleted.add(res)
        }

        val unchanged = unchangedTagIds.map { SongTagAssociation(songId, existingTagIdsMap.getValue(it)) }

        Either.Right(
          ReplaceAllSuccess(
            created = created,
            updated = emptyList(),
            unchanged = unchanged,
            deleted = deleted
          )
        )
      }
    } catch (e: RollbackException) {
      Either.Left(e.error)
    } catch (e: Exception) {
      Either.Left(ReplaceAllError.UnknownError(e, e.message ?: "Unknown error"))
    }
}
