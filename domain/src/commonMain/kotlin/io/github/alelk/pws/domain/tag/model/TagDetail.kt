package io.github.alelk.pws.domain.tag.model

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.Tag

/**
 * Detailed tag view with song count.
 * Extends [Tag] with additional computed information.
 */
data class TagDetail(
  val id: TagId,
  val name: String,
  val priority: Int,
  val color: Color,
  val predefined: Boolean,
  val songCount: Int
) {
  constructor(tag: Tag, songCount: Int) : this(tag.id, tag.name, tag.priority, tag.color, tag.predefined, songCount)

  fun toTag(): Tag = Tag(id, name, priority, color, predefined)
}

