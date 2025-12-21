package io.github.alelk.pws.api.contract.songreference

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceAllSongReferencesResultDto(
  val created: List<SongReferenceDto>,
  val updated: List<SongReferenceDto>,
  val unchanged: List<SongReferenceDto>,
  val deleted: List<SongReferenceDto>
)
