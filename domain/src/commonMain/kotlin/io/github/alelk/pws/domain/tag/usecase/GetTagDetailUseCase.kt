package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.repository.TagReadRepository

/**
 * Use case: get tag details by id.
 * @param ID The type of TagId this use case works with
 */
class GetTagDetailUseCase<ID : TagId>(
  private val tagReadRepository: TagReadRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: ID): Either<ReadError, TagDetail<ID>> =
    txRunner.inRoTransaction {
      tagReadRepository.get(id)?.right() ?: ReadError.NotFound().left()
    }
}

