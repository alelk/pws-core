package io.github.alelk.pws.api.contract.book

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import kotlinx.serialization.Serializable

@Serializable
data class BookCreateRequestDto(
  val id: BookIdDto,
  val locale: LocaleDto,
  val name: String,
  val displayShortName: String? = null,
  val displayName: String? = null,
  val releaseDate: YearDto? = null,
  val authors: List<PersonDto>? = null,
  val creators: List<PersonDto>? = null,
  val reviewers: List<PersonDto>? = null,
  val editors: List<PersonDto>? = null,
  val description: String? = null,
  val preface: String? = null,
  val enabled: Boolean = true,
  val priority: Int = 0
)

