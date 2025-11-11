package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

@Serializable
data class SongSummaryDto(
  val id: SongIdDto,
  val version: VersionDto,
  val locale: LocaleDto,
  val name: String,
  val edited: Boolean = false,
)