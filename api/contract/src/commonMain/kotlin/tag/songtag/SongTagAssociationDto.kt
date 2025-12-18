package io.github.alelk.pws.api.contract.tag.songtag

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import kotlinx.serialization.Serializable

/**
 * Song-tag association.
 */
@Serializable
data class SongTagAssociationDto(
  val songId: SongIdDto,
  val tagId: TagIdDto
)

