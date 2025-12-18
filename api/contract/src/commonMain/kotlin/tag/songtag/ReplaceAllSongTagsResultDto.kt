package io.github.alelk.pws.api.contract.tag.songtag

import kotlinx.serialization.Serializable

/**
 * Result of replacing all song-tag associations.
 */
@Serializable
data class ReplaceAllSongTagsResultDto(
  val created: List<SongTagAssociationDto>,
  val unchanged: List<SongTagAssociationDto>,
  val deleted: List<SongTagAssociationDto>
)

