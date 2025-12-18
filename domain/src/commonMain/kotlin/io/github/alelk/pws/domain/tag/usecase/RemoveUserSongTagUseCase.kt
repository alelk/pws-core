package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.UserTagWriteRepository

/**
 * Use case: remove a tag from a song for a user.
 */
class RemoveUserSongTagUseCase(
  private val userTagRepository: UserTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, songId: SongId, tagId: TagId): DeleteResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.removeTagFromSong(userId, songId, tagId) }
}

