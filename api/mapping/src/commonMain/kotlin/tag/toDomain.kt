package io.github.alelk.pws.api.mapping.tag

import io.github.alelk.pws.api.contract.tag.TagCreateRequestDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.TagUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.model.TagSummary
import io.github.alelk.pws.domain.tag.query.TagSort

fun TagSortDto.toDomain(): TagSort = when (this) {
  TagSortDto.ByName -> TagSort.ByName
  TagSortDto.ByPriority -> TagSort.ByPriority
  TagSortDto.BySongCount -> TagSort.BySongCount
}

fun TagCreateRequestDto.toDomain(): CreateTagCommand = CreateTagCommand(
  id = id.toDomain(),
  name = name,
  color = color.toDomain(),
  priority = priority
)

fun TagUpdateRequestDto.toDomain(tagId: io.github.alelk.pws.domain.core.ids.TagId): UpdateTagCommand = UpdateTagCommand(
  id = tagId,
  name = name,
  color = color?.toDomain(),
  priority = priority
)

fun TagSummaryDto.toDomain(): TagSummary = TagSummary(
  id = id.toDomain(),
  name = name,
  color = color.toDomain(),
  songCount = songCount,
  predefined = predefined
)

fun TagDetailDto.toDomain(): TagDetail = TagDetail(
  id = id.toDomain(),
  name = name,
  priority = priority,
  color = color.toDomain(),
  predefined = predefined,
  songCount = songCount
)

