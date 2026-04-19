package io.github.alelk.pws.domain.songreference.usecase

import arrow.core.Either
import arrow.core.raise.either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ReplaceAllError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.model.ReplaceAllSuccess
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository

/**
 * Use case: Replace all existing references for a song with the provided set.
 */
class ReplaceSongReferencesUseCase(
  private val readRepository: SongReferenceReadRepository,
  private val writeRepository: SongReferenceWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId, references: Collection<SongReference>): Either<ReplaceAllError, ReplaceAllSuccess<SongReference>> =
    txRunner.inRwTransaction {
      either {
        val existingRefs = readRepository.getReferencesForSong(songId).toSet()
        val targetRefSongIds = references.map { it.refSongId }.toSet()

        val unchangedRefs = references intersect existingRefs
        val refsToDelete = existingRefs.filterNot { it.refSongId in targetRefSongIds }
        val refsToCreateOrUpdate = references.filterNot { it in unchangedRefs }

        val created = mutableListOf<SongReference>()
        val updated = mutableListOf<SongReference>()

        for (ref in refsToCreateOrUpdate) {
          val createCommand = CreateSongReferenceCommand(
            songId = ref.songId, refSongId = ref.refSongId,
            reason = ref.reason, volume = ref.volume, priority = ref.priority
          )
          val createResult = writeRepository.create(createCommand)
          when {
            createResult.isRight() -> created.add(ref)
            createResult.leftOrNull() is CreateError.AlreadyExists -> {
              val updateCommand = UpdateSongReferenceCommand(
                songId = ref.songId, refSongId = ref.refSongId,
                reason = ref.reason, volume = ref.volume, priority = ref.priority
              )
              writeRepository.update(updateCommand).mapLeft { err ->
                when (err) {
                  is UpdateError.NotFound -> error("illegal state: updating song reference not found: $songId -> ${ref.refSongId}")
                  is UpdateError.ValidationError -> ReplaceAllError.ValidationError(err.message)
                  is UpdateError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
                }
              }.bind()
              updated.add(ref)
            }
            else -> createResult.mapLeft { err ->
              when (err) {
                is CreateError.AlreadyExists -> error("unreachable")
                is CreateError.ValidationError -> ReplaceAllError.ValidationError(err.message)
                is CreateError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
              }
            }.bind()
          }
        }

        for (ref in refsToDelete) {
          writeRepository.delete(ref.songId, ref.refSongId).mapLeft { err ->
            when (err) {
              is DeleteError.NotFound -> error("illegal state: deleting song reference not found: $songId -> ${ref.refSongId}")
              is DeleteError.ValidationError -> ReplaceAllError.ValidationError(err.message)
              is DeleteError.UnknownError -> ReplaceAllError.UnknownError(err.cause, err.message)
            }
          }.bind()
        }

        ReplaceAllSuccess(created = created, updated = updated, unchanged = unchangedRefs.toList(), deleted = refsToDelete)
      }
    }
}
