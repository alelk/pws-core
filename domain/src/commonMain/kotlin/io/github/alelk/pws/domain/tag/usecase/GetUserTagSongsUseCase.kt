package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.UserTagReadRepository

/**
 * Use case: get all song IDs for a tag (for user's view).
 */
class GetUserTagSongsUseCase(
  private val userTagRepository: UserTagReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, tagId: TagId): List<SongId> =
    txRunner.inRoTransaction { userTagRepository.getSongIdsByTag(userId, tagId) }
}

