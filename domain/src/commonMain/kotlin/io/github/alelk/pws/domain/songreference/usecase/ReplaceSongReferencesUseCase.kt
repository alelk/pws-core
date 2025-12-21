package io.github.alelk.pws.domain.songreference.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.ReplaceAllResourcesResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository

/**
 * Use case: Replace all existing references for a song with the provided set.
 *  - Any missing references are removed
 *  - Any new references are inserted
 *  - Existing references are updated if needed
 *
 * Applied atomically.
 */
class ReplaceSongReferencesUseCase(
  private val readRepository: SongReferenceReadRepository,
  private val writeRepository: SongReferenceWriteRepository,
  private val txRunner: TransactionRunner
) {

  suspend operator fun invoke(songId: SongId, references: Collection<SongReference>): ReplaceAllResourcesResult<SongReference> =
    txRunner.inRwTransaction {
      val existingRefs = readRepository.getReferencesForSong(songId).toSet()
      val targetRefSongIds = references.map { it.refSongId }.toSet()

      val unchangedRefs = references intersect existingRefs
      val refsToDelete = existingRefs.filterNot { it.refSongId in targetRefSongIds }
      val refsToCreateOrUpdate = references.filterNot { it in unchangedRefs }

      val created = mutableListOf<SongReference>()
      val updated = mutableListOf<SongReference>()

      for (ref in refsToCreateOrUpdate) {
        val createCommand = CreateSongReferenceCommand(
          songId = ref.songId,
          refSongId = ref.refSongId,
          reason = ref.reason,
          volume = ref.volume,
          priority = ref.priority
        )
        when (val result = writeRepository.create(createCommand)) {
          is CreateResourceResult.Success<*> -> created.add(ref)
          is CreateResourceResult.AlreadyExists<*> -> {
            val updateCommand = UpdateSongReferenceCommand(
              songId = ref.songId,
              refSongId = ref.refSongId,
              reason = ref.reason,
              volume = ref.volume,
              priority = ref.priority
            )
            when (val updateResult = writeRepository.update(updateCommand)) {
              is UpdateResourceResult.Success<*> -> updated.add(ref)
              is UpdateResourceResult.NotFound<*> -> error("illegal state: updating song reference not found: $songId -> ${ref.refSongId}")
              is UpdateResourceResult.ValidationError<*> -> return@inRwTransaction ReplaceAllResourcesResult.ValidationError(ref, updateResult.message)
              is UpdateResourceResult.UnknownError<*> -> return@inRwTransaction ReplaceAllResourcesResult.UnknownError(ref, updateResult.exception, updateResult.message)
            }
          }
          is CreateResourceResult.ValidationError<*> -> return@inRwTransaction ReplaceAllResourcesResult.ValidationError(ref, result.message)
          is CreateResourceResult.UnknownError<*> -> return@inRwTransaction ReplaceAllResourcesResult.UnknownError(ref, result.exception, result.message)
        }
      }

      for (ref in refsToDelete) {
        when (val result = writeRepository.delete(ref.songId, ref.refSongId)) {
          is DeleteResourceResult.Success<*> -> continue
          is DeleteResourceResult.NotFound<*> -> error("illegal state: deleting song reference not found: $songId -> ${ref.refSongId}")
          is DeleteResourceResult.ValidationError<*> -> return@inRwTransaction ReplaceAllResourcesResult.ValidationError(ref, result.message)
          is DeleteResourceResult.UnknownError<*> -> return@inRwTransaction ReplaceAllResourcesResult.UnknownError(ref, result.exception, result.message)
        }
      }

      ReplaceAllResourcesResult.Success(
        created = created,
        updated = updated,
        unchanged = unchangedRefs.toList(),
        deleted = refsToDelete
      )
    }
}
