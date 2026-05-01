package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.MergedSongDetail
import io.github.alelk.pws.domain.song.model.SongField
import io.github.alelk.pws.domain.song.model.SongSource
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.UserSongOverrideReadRepository

/**
 * Get song with user's overrides applied (merged view).
 */
class GetMergedSongDetailUseCase(
  private val songReadRepository: SongReadRepository,
  private val overrideReadRepository: UserSongOverrideReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, songId: SongId): Either<ReadError, MergedSongDetail> =
    txRunner.inRoTransaction {
      // First check if user has overrides for this song
      val hasOverrides = overrideReadRepository.hasOverrides(userId, songId)

      if (hasOverrides) {
        // Get song with overrides already applied
        val mergedSong = overrideReadRepository.getSongWithOverrides(userId, songId)
          ?: return@inRoTransaction ReadError.NotFound().left()

        // Get original to determine which fields are overridden
        val original = songReadRepository.get(songId)
          ?: return@inRoTransaction ReadError.NotFound().left()

        val overriddenFields = mutableSetOf<SongField>()
        if (mergedSong.name != original.name) overriddenFields += SongField.NAME
        if (mergedSong.lyric != original.lyric) overriddenFields += SongField.LYRIC
        if (mergedSong.author != original.author) overriddenFields += SongField.AUTHOR
        if (mergedSong.translator != original.translator) overriddenFields += SongField.TRANSLATOR
        if (mergedSong.composer != original.composer) overriddenFields += SongField.COMPOSER
        if (mergedSong.tonalities != original.tonalities) overriddenFields += SongField.TONALITIES
        if (mergedSong.bibleRef != original.bibleRef) overriddenFields += SongField.BIBLE_REF

        MergedSongDetail(
          id = mergedSong.id,
          version = mergedSong.version,
          locale = mergedSong.locale,
          name = mergedSong.name,
          lyric = mergedSong.lyric,
          author = mergedSong.author,
          translator = mergedSong.translator,
          composer = mergedSong.composer,
          tonalities = mergedSong.tonalities,
          year = mergedSong.year,
          bibleRef = mergedSong.bibleRef,
          source = SongSource.GLOBAL,
          hasOverride = true,
          overriddenFields = overriddenFields
        ).right()
      } else {
        // No overrides, return original song
        val song = songReadRepository.get(songId) ?: return@inRoTransaction ReadError.NotFound().left()

        MergedSongDetail(
          id = song.id,
          version = song.version,
          locale = song.locale,
          name = song.name,
          lyric = song.lyric,
          author = song.author,
          translator = song.translator,
          composer = song.composer,
          tonalities = song.tonalities,
          year = song.year,
          bibleRef = song.bibleRef,
          source = SongSource.GLOBAL,
          hasOverride = false,
          overriddenFields = emptySet()
        ).right()
      }
    }
}

