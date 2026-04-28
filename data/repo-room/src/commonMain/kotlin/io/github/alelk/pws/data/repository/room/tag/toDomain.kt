package io.github.alelk.pws.data.repository.room.tag

import io.github.alelk.pws.database.tag.TagEntity
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.model.TagDetail

fun TagEntity.toTag(): Tag<TagId> = Tag.create(id, name, priority, color)

fun TagEntity.toTagDetail(songCount: Int): TagDetail<TagId> =
  TagDetail.create(id, name, priority, color, songCount)

