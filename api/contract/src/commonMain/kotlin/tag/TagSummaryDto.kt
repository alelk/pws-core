package io.github.alelk.pws.api.contract.tag

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tag summary for list display - sealed hierarchy with polymorphic serialization.
 */
@Serializable
sealed class TagSummaryDto {
  abstract val id: TagIdDto
  abstract val name: String
  abstract val priority: Int
  abstract val color: ColorDto

  /** Predefined (global) tag summary */
  @Serializable
  @SerialName("predefined")
  data class Predefined(
    override val id: TagIdDto,
    override val name: String,
    override val priority: Int,
    override val color: ColorDto,
    /** True if user has overridden this tag's properties */
    val edited: Boolean = false
  ) : TagSummaryDto()

  /** User-created custom tag summary */
  @Serializable
  @SerialName("custom")
  data class Custom(
    override val id: TagIdDto,
    override val name: String,
    override val priority: Int,
    override val color: ColorDto
  ) : TagSummaryDto()
}

