package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.ReplaceAllResourcesResult
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
 */
class ReplaceAllSongTagsUseCase(
  private val readRepository: SongTagReadRepository,
  private val writeRepository: SongTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId, tagIds: Set<TagId>): ReplaceAllResourcesResult<SongTagAssociation> =
    txRunner.inRwTransaction {
      val existingTagIds = readRepository.getTagIdsBySongId(songId)

      val unchangedTagIds = existingTagIds intersect tagIds
      val tagIdsToDelete = existingTagIds - tagIds
      val tagIdsToCreate = tagIds - existingTagIds

      // Create new associations
      for (tagId in tagIdsToCreate) {
        val association = SongTagAssociation(songId, tagId)
        when (val result = writeRepository.create(songId, tagId)) {
          is CreateResourceResult.Success<*> -> continue
          is CreateResourceResult.AlreadyExists<*> -> error("illegal state: creating song-tag already exists: $songId $tagId")
          is CreateResourceResult.ValidationError<*> -> return@inRwTransaction ReplaceAllResourcesResult.ValidationError(association, result.message)
          is CreateResourceResult.UnknownError<*> -> return@inRwTransaction ReplaceAllResourcesResult.UnknownError(association, result.exception, result.message)
        }
      }

      // Delete removed associations
      for (tagId in tagIdsToDelete) {
        val association = SongTagAssociation(songId, tagId)
        when (val result = writeRepository.delete(songId, tagId)) {
          is DeleteResourceResult.Success<*> -> continue
          is DeleteResourceResult.NotFound<*> -> error("illegal state: deleting song-tag not found: $songId $tagId")
          is DeleteResourceResult.ValidationError<*> -> return@inRwTransaction ReplaceAllResourcesResult.ValidationError(association, result.message)
          is DeleteResourceResult.UnknownError<*> -> return@inRwTransaction ReplaceAllResourcesResult.UnknownError(association, result.exception, result.message)
        }
      }

      ReplaceAllResourcesResult.Success(
        created = tagIdsToCreate.map { SongTagAssociation(songId, it) },
        updated = emptyList(), // Song-tag has no updatable fields
        unchanged = unchangedTagIds.map { SongTagAssociation(songId, it) },
        deleted = tagIdsToDelete.map { SongTagAssociation(songId, it) }
      )
    }
}

