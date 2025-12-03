package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.TonalityDto
import kotlinx.serialization.Serializable

@Serializable
data class SongDetailDto(
  val id: SongIdDto,
  val version: VersionDto,
  val locale: LocaleDto,
  val name: String,
  val lyric: LyricDto,
  val author: PersonDto?,
  val translator: PersonDto? = null,
  val composer: PersonDto? = null,
  val tonalities: List<TonalityDto>? = null,
  val year: YearDto? = null,
  val bibleRef: String? = null,
  val edited: Boolean = false,
)