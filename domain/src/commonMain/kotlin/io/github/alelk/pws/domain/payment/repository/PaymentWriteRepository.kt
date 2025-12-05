package io.github.alelk.pws.domain.payment.repository

import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.payment.command.CreatePaymentCommand
import io.github.alelk.pws.domain.payment.command.UpdatePaymentCommand

interface PaymentWriteRepository {
  suspend fun create(command: CreatePaymentCommand): CreateResourceResult<PaymentId>
  suspend fun update(command: UpdatePaymentCommand): UpdateResourceResult<PaymentId>
  suspend fun delete(id: PaymentId): DeleteResourceResult<PaymentId>
}