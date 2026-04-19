package io.github.alelk.pws.domain.songnumber.usecase

import arrow.core.Either
import arrow.core.raise.either
import io.github.alelk.pws.domain.core.error.BulkCreateError
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository

class CreateSongNumbersUseCase(
  private val readRepository: SongNumberReadRepository,
  private val writeRepository: SongNumberWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(bookId: BookId, links: List<SongNumberLink>): Either<BulkCreateError<SongNumberLink>, List<SongNumberLink>> =
    txRunner.inRwTransaction {
      either {
        val existing = readRepository.getAllByBookId(bookId).intersect(links.toSet())
        val duplicates = links.groupBy { it.songId }.filter { (_, l) -> l.size > 1 }
        if (existing.isNotEmpty()) raise(BulkCreateError.AlreadyExists(existing.toList()))
        if (duplicates.isNotEmpty()) {
          val duplicatedLinks = duplicates.values.map { it.first() }
          raise(BulkCreateError.ValidationError("duplicated song numbers: ${duplicatedLinks.joinToString(", ")}"))
        }
        for (link in links) {
          writeRepository.create(bookId, link).mapLeft { err ->
            when (err) {
              is CreateError.AlreadyExists -> error("impossible state: link $link exists during creation")
              is CreateError.ValidationError -> BulkCreateError.ValidationError(err.message)
              is CreateError.UnknownError -> BulkCreateError.UnknownError(err.cause, err.message)
            }
          }.bind()
        }
        links
      }
    }
}