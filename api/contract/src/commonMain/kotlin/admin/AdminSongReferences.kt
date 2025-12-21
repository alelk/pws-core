package io.github.alelk.pws.api.contract.admin

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.ktor.resources.Resource

/**
 * Admin API for managing song-to-song references.
 * Requires admin role.
 */
@Resource("/v1/admin/songs/{songId}/references")
class AdminSongReferences(val songId: SongIdDto) {

  @Resource("{refSongId}")
  class ByRefSongId(val parent: AdminSongReferences, val refSongId: SongIdDto)
}
