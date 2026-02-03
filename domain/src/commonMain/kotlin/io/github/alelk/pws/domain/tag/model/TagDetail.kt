package io.github.alelk.pws.domain.tag.model

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Detailed tag view with song count - sealed hierarchy for predefined and custom tags.
 */
sealed class TagDetail<out ID : TagId>(
  open val id: ID,
  open val name: String,
  open val priority: Int,
  open val color: Color,
  open val songCount: Int
) {
  /** Predefined (global) tag detail with song count */
  data class Predefined(
    override val id: TagId.Predefined,
    override val name: String,
    override val priority: Int,
    override val color: Color,
    /** True if user has overridden this tag's properties */
    val edited: Boolean = false,
    override val songCount: Int
  ) : TagDetail<TagId.Predefined>(id, name, priority, color, songCount)

  /** User-created custom tag detail with song count */
  data class Custom(
    override val id: TagId.Custom,
    override val name: String,
    override val priority: Int,
    override val color: Color,
    override val songCount: Int
  ) : TagDetail<TagId.Custom>(id, name, priority, color, songCount)

  companion object {
    /** Create from Tag with song count */
    fun from(tag: Tag<*>, songCount: Int): TagDetail<*> = when (tag) {
      is Tag.Predefined -> Predefined(tag.id, tag.name, tag.priority, tag.color, tag.edited, songCount)
      is Tag.Custom -> Custom(tag.id, tag.name, tag.priority, tag.color, songCount)
    }

    /** Create TagDetail - determines type automatically */
    fun create(
      id: TagId,
      name: String,
      priority: Int,
      color: Color,
      songCount: Int,
      edited: Boolean = false
    ): TagDetail<TagId> = when (id) {
      is TagId.Predefined -> Predefined(id, name, priority, color, edited, songCount)
      is TagId.Custom -> Custom(id, name, priority, color, songCount)
    }
  }
}

