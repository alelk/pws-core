package io.github.alelk.pws.domain.songtag.usecase

import arrow.core.Either
import arrow.core.raise.either
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
  suspend operator fun invoke(songId: SongId, tagIds: Set<ID>): Either<ReplaceAllError, ReplaceAllSuccess<SongTagAssociation<ID>>> =
    txRunner.inRwTransaction {
      either {
        val existingTagIds = readRepository.getTagIdsBySongId(songId)
        val existingTagIdsMap = existingTagIds.associateBy { it.identifier }
        val newTagIdsMap = tagIds.associateBy { it.identifier }

        val tagIdsToDelete = existingTagIdsMap.keys - newTagIdsMap.keys
        val tagIdsToCreate = newTagIdsMap.keys - existingTagIdsMap.keys
        val unchangedTagIds = existingTagIdsMap.keys intersect newTagIdsMap.keys

        val created = mutableListOf<SongTagAssociation<ID>>()
        for (tagIdStr in tagIdsToCreate) {
          val tagId = newTagIdsMap.getValue(tagIdStr)
          val createdAssociation = writeRepository.create(songId, tagId).mapLeft { err ->
            when (err) {
              is CreateError.AlreadyExists -> ReplaceAllError.UnknownError(message = "illegal state: creating song-tag already exists: $songId $tagId")
              is CreateError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is CreateError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
          created.add(createdAssociation)
        }

        val deleted = mutableListOf<SongTagAssociation<ID>>()
        for (tagIdStr in tagIdsToDelete) {
          val tagId = existingTagIdsMap.getValue(tagIdStr)
          val deletedAssociation = writeRepository.delete(songId, tagId).mapLeft { err ->
            when (err) {
              is DeleteError.NotFound -> ReplaceAllError.UnknownError(message = "illegal state: deleting song-tag not found: $songId $tagId")
              is DeleteError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is DeleteError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
          deleted.add(deletedAssociation)
        }

        val unchanged = unchangedTagIds.map { key -> SongTagAssociation(songId, existingTagIdsMap.getValue(key)) }

        ReplaceAllSuccess(
          created = created,
          updated = emptyList(),
          unchanged = unchanged,
          deleted = deleted
        )
      }
    }
}
