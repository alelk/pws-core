package io.github.alelk.pws.domain.tag.command

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Command to create a new tag.
 * @param ID The type of TagId
 */
data class CreateTagCommand<out ID : TagId>(
  val id: ID,
  val name: String,
  val color: Color,
  val priority: Int = 0
) {
  init {
    require(name.isNotBlank()) { "Tag name must not be blank" }
  }
}

