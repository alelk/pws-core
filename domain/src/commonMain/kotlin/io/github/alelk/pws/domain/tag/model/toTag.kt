package io.github.alelk.pws.domain.tag.model

import io.github.alelk.pws.domain.core.ids.TagId

fun TagDetail.Predefined.toTag(): Tag.Predefined = Tag.Predefined(id, name, priority, color, edited)
fun TagDetail.Custom.toTag(): Tag.Custom = Tag.Custom(id, name, priority, color)

@Suppress("UNCHECKED_CAST")
fun <ID : TagId> TagDetail<ID>.toTag(): Tag<ID> =
  when (this) {
    is TagDetail.Predefined -> this.toTag() as Tag<ID>
    is TagDetail.Custom -> this.toTag() as Tag<ID>
  }