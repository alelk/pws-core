package io.github.alelk.pws.api.contract.usertag

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import kotlinx.serialization.Serializable

/**
 * User's override for a global tag.
 */
@Serializable
data class UserTagOverrideDto(
  val tagId: TagIdDto,
  val hidden: Boolean = false,
  val colorOverride: ColorDto? = null
)

