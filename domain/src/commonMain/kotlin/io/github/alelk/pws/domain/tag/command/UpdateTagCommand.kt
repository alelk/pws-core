package io.github.alelk.pws.domain.tag.command

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Command to update an existing tag.
 */
data class UpdateTagCommand(
  val id: TagId,
  val name: String? = null,
  val color: Color? = null,
  val priority: Int? = null
) {
  init {
    name?.let { require(it.isNotBlank()) { "Tag name must not be blank" } }
  }

  fun hasChanges(): Boolean = name != null || color != null || priority != null
}

