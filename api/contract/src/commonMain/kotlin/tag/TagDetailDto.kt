package io.github.alelk.pws.api.contract.tag

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Detailed tag view - sealed hierarchy with polymorphic serialization.
 */
@Serializable
sealed class TagDetailDto {
  abstract val id: TagIdDto
  abstract val name: String
  abstract val priority: Int
  abstract val color: ColorDto
  abstract val songCount: Int

  /** Predefined (global) tag detail */
  @Serializable
  @SerialName("predefined")
  data class Predefined(
    override val id: TagIdDto,
    override val name: String,
    override val priority: Int,
    override val color: ColorDto,
    /** True if user has overridden this tag's properties */
    val edited: Boolean = false,
    override val songCount: Int
  ) : TagDetailDto()

  /** User-created custom tag detail */
  @Serializable
  @SerialName("custom")
  data class Custom(
    override val id: TagIdDto,
    override val name: String,
    override val priority: Int,
    override val color: ColorDto,
    override val songCount: Int
  ) : TagDetailDto()
}

