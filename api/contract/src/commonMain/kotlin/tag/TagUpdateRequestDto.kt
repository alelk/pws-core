package io.github.alelk.pws.api.contract.tag

import io.github.alelk.pws.api.contract.core.ColorDto
import kotlinx.serialization.Serializable

/**
 * Request to update an existing tag.
 */
@Serializable
data class TagUpdateRequestDto(
  val name: String? = null,
  val color: ColorDto? = null,
  val priority: Int? = null
)

