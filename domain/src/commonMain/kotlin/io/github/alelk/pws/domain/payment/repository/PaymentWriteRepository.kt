package io.github.alelk.pws.domain.payment.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.payment.command.CreatePaymentCommand
import io.github.alelk.pws.domain.payment.command.UpdatePaymentCommand

interface PaymentWriteRepository {
  suspend fun create(command: CreatePaymentCommand): Either<CreateError, PaymentId>
  suspend fun update(command: UpdatePaymentCommand): Either<UpdateError, PaymentId>
  suspend fun delete(id: PaymentId): Either<DeleteError, PaymentId>
}