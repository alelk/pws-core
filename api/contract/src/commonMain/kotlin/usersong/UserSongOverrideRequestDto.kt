package io.github.alelk.pws.api.contract.usersong

import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.TonalityDto
import io.github.alelk.pws.api.contract.song.LyricDto
import kotlinx.serialization.Serializable

/**
 * Request to override a global song for current user.
 * Only non-null fields will be applied as overrides.
 *
 * Use explicit `null` to clear an override (restore to global value).
 * Omit field to keep current override unchanged.
 */
@Serializable
data class UserSongOverrideRequestDto(
  val name: String? = null,
  val lyric: LyricDto? = null,
  val author: PersonDto? = null,
  val translator: PersonDto? = null,
  val composer: PersonDto? = null,
  val tonalities: List<TonalityDto>? = null,
  val bibleRef: String? = null
)

