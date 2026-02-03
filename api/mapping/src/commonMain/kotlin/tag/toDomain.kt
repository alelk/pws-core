package io.github.alelk.pws.api.mapping.tag

import io.github.alelk.pws.api.contract.tag.TagCreateRequestDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.TagUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.query.TagSort

fun TagSortDto.toDomain(): TagSort = when (this) {
  TagSortDto.ByName -> TagSort.ByName
  TagSortDto.ByPriority -> TagSort.ByPriority
  TagSortDto.BySongCount -> TagSort.BySongCount
}

fun TagCreateRequestDto.toDomain(): CreateTagCommand<TagId> =
  CreateTagCommand(
    id = id.toDomain(),
    name = name,
    color = color.toDomain(),
    priority = priority
  )

fun TagUpdateRequestDto.toDomain(tagId: TagId): UpdateTagCommand<TagId> =
  UpdateTagCommand(
    id = tagId,
    name = name,
    color = color?.toDomain(),
    priority = priority
  )

fun TagSummaryDto.toDomain(): Tag<*> = when (this) {
  is TagSummaryDto.Predefined -> Tag.Predefined(
    id = id.toDomain() as TagId.Predefined,
    name = name,
    priority = priority,
    color = color.toDomain(),
    edited = edited
  )

  is TagSummaryDto.Custom -> Tag.Custom(
    id = id.toDomain() as TagId.Custom,
    name = name,
    priority = priority,
    color = color.toDomain()
  )
}

fun TagDetailDto.toDomain(): TagDetail<*> = when (this) {
  is TagDetailDto.Predefined -> TagDetail.Predefined(
    id = id.toDomain() as TagId.Predefined,
    name = name,
    priority = priority,
    color = color.toDomain(),
    edited = edited,
    songCount = songCount
  )

  is TagDetailDto.Custom -> TagDetail.Custom(
    id = id.toDomain() as TagId.Custom,
    name = name,
    priority = priority,
    color = color.toDomain(),
    songCount = songCount
  )
}

