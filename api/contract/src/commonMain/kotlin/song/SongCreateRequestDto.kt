package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.TonalityDto
import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

@Serializable
data class SongCreateRequestDto(
  val id: SongIdDto,
  val locale: LocaleDto,
  val name: String,
  val lyric: LyricDto,
  val author: PersonDto? = null,
  val translator: PersonDto? = null,
  val composer: PersonDto? = null,
  val tonalities: List<TonalityDto>? = null,
  val year: YearDto? = null,
  val bibleRef: String? = null,
  val edited: Boolean = false,
  val numbersInBook: List<SongNumberDto>? = null
)
