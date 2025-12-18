package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.UserTagWriteRepository

/**
 * Use case: add a tag to a song for a user.
 */
class AddUserSongTagUseCase(
  private val userTagRepository: UserTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, songId: SongId, tagId: TagId): CreateResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.addTagToSong(userId, songId, tagId) }
}

