package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.tag.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for Song-Tag associations.
 * @param ID The type of TagId this repository works with
 */
interface SongTagObserveRepository<ID : TagId> {
  /**
   * Observe all songs for a specific tag.
   */
  fun observeSongsByTag(tagId: ID): Flow<List<SongWithBookInfo>>

  /**
   * Observe all tags for a specific song.
   */
  fun observeTagsForSong(songNumberId: SongNumberId): Flow<List<Tag<ID>>>
}

