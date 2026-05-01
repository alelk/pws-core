package io.github.alelk.pws.domain.songnumber.usecase

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository

class GetAllBookSongNumbersUseCase(
  private val readRepository: SongNumberReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(bookId: BookId): Either<ReadError, List<SongNumberLink>> =
    txRunner.inRoTransaction {
      readRepository.getAllByBookId(bookId).sortedBy { it.number }.right()
    }
}