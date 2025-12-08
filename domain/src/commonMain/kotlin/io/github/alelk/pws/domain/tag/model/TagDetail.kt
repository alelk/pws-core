package io.github.alelk.pws.domain.tag.model

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Detailed tag view.
 */
data class TagDetail(
  val id: TagId,
  val name: String,
  val priority: Int,
  val color: Color,
  val predefined: Boolean,
  val songCount: Int
)

