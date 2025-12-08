package io.github.alelk.pws.domain.tag.command

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Command to create a new tag.
 */
data class CreateTagCommand(
  val id: TagId,
  val name: String,
  val color: Color,
  val priority: Int = 0
) {
  init {
    require(name.isNotBlank()) { "Tag name must not be blank" }
  }
}

