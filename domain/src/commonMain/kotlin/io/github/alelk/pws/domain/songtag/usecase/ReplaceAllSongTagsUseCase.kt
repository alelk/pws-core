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
        val unchangedTagIds = existingTagIds intersect tagIds
        val tagIdsToDelete = existingTagIds - tagIds
        val tagIdsToCreate = tagIds - existingTagIds

        for (tagId in tagIdsToCreate) {
          writeRepository.create(songId, tagId).mapLeft { err ->
            when (err) {
              is CreateError.AlreadyExists -> error("illegal state: creating song-tag already exists: $songId $tagId")
              is CreateError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is CreateError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
        }
        for (tagId in tagIdsToDelete) {
          writeRepository.delete(songId, tagId).mapLeft { err ->
            when (err) {
              is DeleteError.NotFound -> error("illegal state: deleting song-tag not found: $songId $tagId")
              is DeleteError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is DeleteError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
        }

        ReplaceAllSuccess(
          created = tagIdsToCreate.map { SongTagAssociation(songId, it) },
          updated = emptyList(),
          unchanged = unchangedTagIds.map { SongTagAssociation(songId, it) },
          deleted = tagIdsToDelete.map { SongTagAssociation(songId, it) }
        )
      }
    }
}
