package io.github.alelk.pws.api.mapping.book

import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.query.BookSort
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.Year

fun BookSummary.toDto(): BookSummaryDto = BookSummaryDto(
  id = id.toDto(),
  version = version.toDto(),
  locale = locale.toDto(),
  name = name.value,
  displayShortName = displayShortName.value,
  displayName = displayName.value,
  countSongs = countSongs,
  firstSongNumberId = firstSongNumberId?.toDto(),
  enabled = enabled,
  priority = priority
)

fun BookDetail.toDto(): BookDetailDto = BookDetailDto(
  id = id.toDto(),
  version = version.toDto(),
  locale = locale.toDto(),
  name = name.value,
  displayShortName = displayShortName.value,
  displayName = displayName.value,
  releaseDate = releaseDate?.toDto(),
  authors = authors?.map { it.toDto() },
  creators = creators?.map { it.toDto() },
  reviewers = reviewers?.map { it.toDto() },
  editors = editors?.map { it.toDto() },
  description = description,
  preface = preface,
  firstSongNumberId = firstSongNumberId?.toDto(),
  countSongs = countSongs,
  enabled = enabled,
  priority = priority
)

fun BookSort.toDto(): BookSortDto =
  when (this) {
    BookSort.ByName -> BookSortDto.ByName
    BookSort.ByNameDesc -> BookSortDto.ByNameDesc
    BookSort.ByPriority -> BookSortDto.ByPriority
    BookSort.ByPriorityDesc -> BookSortDto.ByPriorityDesc
  }

// ---------- Domain -> DTO mappings for commands ----------

fun CreateBookCommand.toCreateRequestDto(): BookCreateRequestDto = BookCreateRequestDto(
  id = id.toDto(),
  locale = locale.toDto(),
  name = name.value,
  displayShortName = displayShortName.value,
  displayName = displayName.value,
  releaseDate = releaseDate?.toDto(),
  authors = authors.takeIf { it.isNotEmpty() }?.map { it.toDto() },
  creators = creators.takeIf { it.isNotEmpty() }?.map { it.toDto() },
  reviewers = reviewers.takeIf { it.isNotEmpty() }?.map { it.toDto() },
  editors = editors.takeIf { it.isNotEmpty() }?.map { it.toDto() },
  description = description,
  preface = preface,
  enabled = enabled,
  priority = priority
)

fun UpdateBookCommand.toUpdateRequestDto(): BookUpdateRequestDto {
  val releaseDateValue = when (releaseDate) {
    is OptionalField.Unchanged -> null
    is OptionalField.Set<*> -> (releaseDate as OptionalField.Set<Year?>).value?.toDto()
    else -> null
  }

  val descriptionValue = when (description) {
    is OptionalField.Unchanged -> null
    is OptionalField.Set<*> -> (description as OptionalField.Set<String?>).value
    else -> null
  }

  val prefaceValue = when (preface) {
    is OptionalField.Unchanged -> null
    is OptionalField.Set<*> -> (preface as OptionalField.Set<String?>).value
    else -> null
  }

  return BookUpdateRequestDto(
    locale = locale?.toDto(),
    name = name?.value,
    displayShortName = displayShortName?.value,
    displayName = displayName?.value,
    version = version?.toDto(),
    enabled = enabled,
    priority = priority,
    releaseDate = releaseDateValue,
    description = descriptionValue,
    preface = prefaceValue,
    expectedVersion = expectedVersion?.toDto()
  )
}
