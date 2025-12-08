package io.github.alelk.pws.domain.songtag.model

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Song-Tag association.
 */
data class SongTag(
  val songNumberId: SongNumberId,
  val tagId: TagId
)

