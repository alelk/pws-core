package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.getOrElse
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository

/** Use case: update a song (patch semantics). */
class UpdateSongUseCase(
  private val readRepository: SongReadRepository,
  private val writeRepository: SongWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateSongCommand): Either<UpdateError, SongId> =
    txRunner.inRwTransaction {
      val existing = readRepository.get(command.id) ?: return@inRwTransaction Either.Left(UpdateError.NotFound)

      command.expectVersion?.let { expected ->
        if (existing.version != expected)
          return@inRwTransaction Either.Left(UpdateError.ValidationError("Version conflict for song ${command.id}: expected=$expected actual=${existing.version}"))
      }

      command.version?.let { nextVersion ->
        if (nextVersion <= existing.version)
          return@inRwTransaction Either.Left(UpdateError.ValidationError("Invalid version $nextVersion for song ${command.id}: expected > ${existing.version}"))
      }

      if (!command.hasChanges()) return@inRwTransaction Either.Right(command.id)

      val updated = applyPatch(existing, command)
      writeRepository.update(updated)
    }

  private fun applyPatch(existing: SongDetail, command: UpdateSongCommand): SongDetail =
    existing.copy(
      version = command.version ?: existing.version,
      locale = command.locale ?: existing.locale,
      name = command.name ?: existing.name,
      lyric = command.lyric ?: existing.lyric,
      author = command.author.getOrElse { existing.author },
      translator = command.translator.getOrElse { existing.translator },
      composer = command.composer.getOrElse { existing.composer },
      tonalities = command.tonalities.getOrElse { existing.tonalities },
      year = command.year.getOrElse { existing.year },
      bibleRef = command.bibleRef.getOrElse { existing.bibleRef },
      edited = true
    )
}
