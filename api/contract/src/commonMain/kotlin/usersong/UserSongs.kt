package io.github.alelk.pws.api.contract.usersong

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.ktor.resources.Resource

/**
 * User songs API endpoints.
 *
 * This API provides a unified view of songs for a user:
 * - Global songs are returned with user's overrides applied (merged)
 * - User can edit global songs (creates/updates override)
 * - User can reset overrides to restore original global song
 *
 * Response includes metadata about whether the song has user overrides.
 */
@Resource("/v1/user/songs")
class UserSongs(
  val sort: SongSortDto? = null,
  /** Filter to only songs with user overrides */
  val overriddenOnly: Boolean? = null
) {
  @Resource("{id}")
  class ById(val parent: UserSongs = UserSongs(), val id: SongIdDto) {

    /** Reset user overrides for this song (restore to global version) */
    @Resource("override")
    class Override(val parent: ById)
  }
}

