package io.github.alelk.pws.api.contract.usertag

import io.github.alelk.pws.api.contract.core.ColorDto
import kotlinx.serialization.Serializable

/**
 * Request to update user's override for a global tag.
 */
@Serializable
data class UserTagOverrideUpdateRequestDto(
  val hidden: Boolean? = null,
  val colorOverride: ColorDto? = null
)

