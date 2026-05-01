package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagReadRepository

/**
 * Use case: get all tags.
 * @param ID The type of TagId this use case works with
 */
class GetTagsUseCase<ID : TagId>(
  private val tagReadRepository: TagReadRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(sort: TagSort = TagSort.ByPriority): Either<ReadError, List<Tag<ID>>> =
    txRunner.inRoTransaction { tagReadRepository.getAll(sort).right() }
}

