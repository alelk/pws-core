package io.github.alelk.pws.api.mapping.tag

import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.songtag.ReplaceAllSongTagsResultDto
import io.github.alelk.pws.api.contract.tag.songtag.SongTagAssociationDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.core.result.ReplaceAllResourcesResult
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.query.TagSort

fun TagSort.toDto(): TagSortDto = when (this) {
  TagSort.ByName -> TagSortDto.ByName
  TagSort.ByPriority -> TagSortDto.ByPriority
  TagSort.BySongCount -> TagSortDto.BySongCount
}

fun Tag<*>.toDto(): TagSummaryDto = when (this) {
  is Tag.Predefined -> TagSummaryDto.Predefined(
    id = id.toDto(),
    name = name,
    priority = priority,
    color = color.toDto(),
    edited = edited
  )

  is Tag.Custom -> TagSummaryDto.Custom(
    id = id.toDto(),
    name = name,
    priority = priority,
    color = color.toDto()
  )
}

fun TagDetail<*>.toDto(): TagDetailDto = when (this) {
  is TagDetail.Predefined -> TagDetailDto.Predefined(
    id = id.toDto(),
    name = name,
    priority = priority,
    color = color.toDto(),
    edited = edited,
    songCount = songCount
  )

  is TagDetail.Custom -> TagDetailDto.Custom(
    id = id.toDto(),
    name = name,
    priority = priority,
    color = color.toDto(),
    songCount = songCount
  )
}

fun SongTagAssociation<*>.toDto(): SongTagAssociationDto = SongTagAssociationDto(
  songId = songId.toDto(),
  tagId = tagId.toDto()
)

fun ReplaceAllResourcesResult.Success<SongTagAssociation<*>>.toDto(): ReplaceAllSongTagsResultDto =
  ReplaceAllSongTagsResultDto(
    created = created.map { it.toDto() },
    unchanged = unchanged.map { it.toDto() },
    deleted = deleted.map { it.toDto() }
  )

