package io.github.alelk.pws.domain.songtag.model

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Represents a song-tag association (many-to-many link).
 */
data class SongTagAssociation<out ID: TagId>(
  val songId: SongId,
  val tagId: ID
)

