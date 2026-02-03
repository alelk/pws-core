package io.github.alelk.pws.domain.tag.model

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Tag domain model - sealed hierarchy for predefined and custom tags.
 */
sealed class Tag<out ID : TagId>(
  open val id: ID,
  open val name: String,
  open val priority: Int,
  open val color: Color
) {
  /** Predefined (global) tag, optionally edited by user */
  data class Predefined(
      override val id: TagId.Predefined,
      override val name: String,
      override val priority: Int,
      override val color: Color,
      /** True if user has overridden this tag's properties */
    val edited: Boolean = false
  ) : Tag<TagId.Predefined>(id, name, priority, color) {
    init {
      require(name.isNotBlank()) { "predefined tag name must not be blank" }
    }
  }

  /** User-created custom tag */
  data class Custom(
      override val id: TagId.Custom,
      override val name: String,
      override val priority: Int,
      override val color: Color
  ) : Tag<TagId.Custom>(id, name, priority, color) {
    init {
      require(name.isNotBlank()) { "custom tag name must not be blank" }
    }
  }

  companion object {
    /** Create tag from TagId - determines type automatically */
    fun create(
        id: TagId,
        name: String,
        priority: Int,
        color: Color,
        edited: Boolean = false
    ): Tag<TagId> = when (id) {
      is TagId.Predefined -> Predefined(id, name, priority, color, edited)
      is TagId.Custom -> Custom(id, name, priority, color)
    }
  }
}