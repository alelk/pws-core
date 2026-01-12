package io.github.alelk.pws.api.contract.usersong

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.TonalityDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

/**
 * Summary of user's song for list views.
 */
@Serializable
data class UserSongSummaryDto(
  val id: SongIdDto,
  val locale: LocaleDto,
  val name: String,
  val author: PersonDto?,
  val tonalities: List<TonalityDto>? = null,

  /** Source of this song: GLOBAL or USER */
  val source: SongSourceDto,

  /** Whether user has any overrides applied to this song */
  val hasOverride: Boolean = false
)

