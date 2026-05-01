package io.github.alelk.pws.domain.bookstatistic.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.bookstatistic.command.UpdateBookStatisticCommand
import io.github.alelk.pws.domain.bookstatistic.model.BookStatisticDetail
import io.github.alelk.pws.domain.bookstatistic.repository.BookStatisticRepository
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

class UpdateBookStatisticUseCase(
  private val repository: BookStatisticRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateBookStatisticCommand): Either<UpdateError, BookStatisticDetail> =
    txRunner.inRwTransaction {
      if (command.isEmpty()) {
        Either.Left(UpdateError.ValidationError("No changes specified for book statistic update"))
      } else {
        repository.update(command)
      }
    }
}

