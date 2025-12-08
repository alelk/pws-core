package io.github.alelk.pws.domain.tag.model

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Tag summary for list display.
 */
data class TagSummary(
  val id: TagId,
  val name: String,
  val color: Color,
  val songCount: Int,
  val predefined: Boolean
)

