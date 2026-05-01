package io.github.alelk.pws.domain.tag.command

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.error.InvalidInputError
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

  companion object {
    fun <ID : TagId> validated(
      id: ID,
      name: String,
      color: Color,
      priority: Int = 0
    ): Either<InvalidInputError, CreateTagCommand<ID>> =
      if (name.isBlank()) {
        InvalidInputError("tag.name", "Tag name must not be blank").left()
      } else {
        CreateTagCommand(id = id, name = name, color = color, priority = priority).right()
      }
  }
}

