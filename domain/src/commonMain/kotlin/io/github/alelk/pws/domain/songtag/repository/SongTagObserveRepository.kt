package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.tag.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for Song-Tag associations.
 */
interface SongTagObserveRepository {
  /**
   * Observe all songs for a specific tag.
   */
  fun observeSongsByTag(tagId: TagId): Flow<List<SongWithBookInfo>>

  /**
   * Observe all tags for a specific song.
   */
  fun observeTagsForSong(songNumberId: SongNumberId): Flow<List<Tag>>
}

