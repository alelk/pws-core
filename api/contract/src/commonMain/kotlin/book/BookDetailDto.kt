package io.github.alelk.pws.api.contract.book

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.Serializable

@Serializable
data class BookDetailDto(
  val id: BookIdDto,
  val version: VersionDto,
  val locale: LocaleDto,
  val name: String,
  val displayShortName: String,
  val displayName: String,
  val releaseDate: YearDto? = null,
  val authors: List<PersonDto>? = null,
  val creators: List<PersonDto>? = null,
  val reviewers: List<PersonDto>? = null,
  val editors: List<PersonDto>? = null,
  val description: String? = null,
  val preface: String? = null,
  val firstSongNumberId: SongNumberIdDto,
  val countSongs: Int,
  val enabled: Boolean = true,
  val priority: Int = 0,
)