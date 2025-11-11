package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.ktor.resources.Resource

@Resource("/v1/songs")
class Songs {

  @Resource("{id}")
  class ById(val parent: Songs = Songs(), val id: SongIdDto)
}