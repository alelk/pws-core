package io.github.alelk.pws.api.contract.book

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.YearDto
import kotlinx.serialization.Serializable

@Serializable
data class BookUpdateRequestDto(
  val locales: List<LocaleDto>? = null,
  val name: String? = null,
  val displayShortName: String? = null,
  val displayName: String? = null,
  val version: VersionDto? = null,
  val enabled: Boolean? = null,
  val priority: Int? = null,
  val releaseDate: YearDto? = null,
  val description: String? = null,
  val preface: String? = null,
  val expectedVersion: VersionDto? = null
)

