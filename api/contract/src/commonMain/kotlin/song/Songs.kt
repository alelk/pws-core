package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.ktor.resources.Resource

@Resource("/v1/songs")
class Songs {

  @Resource("create")
  class Create(val parent: Songs = Songs())

  @Resource("{id}")
  class ById(val parent: Songs = Songs(), val id: SongIdDto) {

    @Resource("update")
    class Update(val parent: ById)
  }
}
