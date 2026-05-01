package io.github.alelk.pws.domain.tag.command

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.error.InvalidInputError
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Command to update an existing tag.
 * @param ID The type of TagId
 */
data class UpdateTagCommand<out ID : TagId>(
  val id: ID,
  val name: String? = null,
  val color: Color? = null,
  val priority: Int? = null
) {
  init {
    name?.let { require(it.isNotBlank()) { "Tag name must not be blank" } }
  }

  fun hasChanges(): Boolean = name != null || color != null || priority != null

  companion object {
    fun <ID : TagId> validated(
      id: ID,
      name: String? = null,
      color: Color? = null,
      priority: Int? = null
    ): Either<InvalidInputError, UpdateTagCommand<ID>> {
      if (name != null && name.isBlank()) {
        return InvalidInputError("tag.name", "Tag name must not be blank").left()
      }
      val command = UpdateTagCommand(id = id, name = name, color = color, priority = priority)
      if (!command.hasChanges()) {
        return InvalidInputError("tag", "At least one field should be changed").left()
      }
      return command.right()
    }
  }
}

