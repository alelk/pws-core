package io.github.alelk.pws.domain.cross.usecase

import arrow.core.Either
import arrow.core.raise.either
import io.github.alelk.pws.domain.core.SongNumber
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository

/**
 * Use case: create a Song aggregate and link it to multiple Books with explicit numbers.
 */
class CreateSongAndLinkToBooksUseCase(
  private val songWriteRepository: SongWriteRepository,
  private val songNumberWriteRepository: SongNumberWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: CreateSongCommand, assignments: Collection<SongNumber>): Either<CreateError, SongId> =
    txRunner.inRwTransaction {
      either {
        assignments.groupBy { it.bookId }.forEach { (bookId, list) ->
          val duplicates = list.groupBy { it.number }.filter { it.value.size > 1 }.keys
          if (duplicates.isNotEmpty())
            raise(CreateError.ValidationError("Duplicate song numbers for book $bookId: ${duplicates.joinToString()}"))
        }

        songWriteRepository.create(command).bind()

        for (assignment in assignments) {
          songNumberWriteRepository.create(assignment.bookId, SongNumberLink(command.id, assignment.number))
            .mapLeft { err ->
              when (err) {
                is CreateError.AlreadyExists ->
                  CreateError.ValidationError("illegal state: song number already exists: $assignment")
                is CreateError.ValidationError -> err
                is CreateError.UnknownError -> err
              }
            }.bind()
        }
        command.id
      }
    }
}
