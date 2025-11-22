package io.github.alelk.pws.api.mapping.book

import io.github.alelk.pws.api.contract.book.*
import io.github.alelk.pws.api.mapping.core.*
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.query.BookSort
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.ids.BookId

fun BookCreateRequestDto.toDomain(): CreateBookCommand = CreateBookCommand(
  id = BookId.parse(id.value),
  locale = locale.toDomain(),
  name = NonEmptyString(name),
  displayShortName = NonEmptyString((displayShortName ?: name).take(5)),
  displayName = NonEmptyString(displayName ?: name),
  releaseDate = releaseDate?.toDomain(),
  authors = authors?.map { it.toDomain() } ?: emptyList(),
  creators = creators?.map { it.toDomain() } ?: emptyList(),
  reviewers = reviewers?.map { it.toDomain() } ?: emptyList(),
  editors = editors?.map { it.toDomain() } ?: emptyList(),
  description = description,
  preface = preface,
  enabled = enabled,
  priority = priority
)

fun BookUpdateRequestDto.toDomain(id: BookIdDto): UpdateBookCommand = UpdateBookCommand(
  id = BookId.parse(id.value),
  locale = locale?.toDomain(),
  name = name?.let { NonEmptyString(it) },
  displayShortName = displayShortName?.let { NonEmptyString(it.take(5)) },
  displayName = displayName?.let { NonEmptyString(it) },
  releaseDate = when (releaseDate) {
    null -> OptionalField.Unchanged
    else -> OptionalField.Set(releaseDate?.toDomain())
  },
  description = when (description) {
    null -> OptionalField.Unchanged
    else -> OptionalField.Set(description)
  },
  preface = when (preface) {
    null -> OptionalField.Unchanged
    else -> OptionalField.Set(preface)
  },
  expectedVersion = expectedVersion?.toDomain(),
  version = version?.toDomain(),
  enabled = enabled,
  priority = priority
)

fun BookSummaryDto.toDomain(): BookSummary = BookSummary(
  id = id.toDomain(),
  version = version.toDomain(),
  locale = locale.toDomain(),
  name = NonEmptyString(name),
  displayShortName = NonEmptyString(displayShortName),
  displayName = NonEmptyString(displayName),
  countSongs = countSongs,
  firstSongNumberId = firstSongNumberId?.toDomain(),
  enabled = enabled,
  priority = priority
)

fun BookDetailDto.toDomain(): BookDetail = BookDetail(
  id = id.toDomain(),
  version = version.toDomain(),
  locale = locale.toDomain(),
  name = nonEmpty(name, "BookDetailDto.name"),
  displayShortName = nonEmpty(displayShortName, "BookDetailDto.displayShortName"),
  displayName = nonEmpty(displayName, "BookDetailDto.displayName"),
  releaseDate = releaseDate?.toDomain(),
  authors = authors?.map { it.toDomain() },
  creators = creators?.map { it.toDomain() },
  reviewers = reviewers?.map { it.toDomain() },
  editors = editors?.map { it.toDomain() },
  description = description,
  preface = preface,
  firstSongNumberId = firstSongNumberId?.toDomain(),
  countSongs = countSongs,
  enabled = enabled,
  priority = priority
)

fun BookSortDto.toDomain(): BookSort =
  when (this) {
    BookSortDto.ByName -> BookSort.ByName
    BookSortDto.ByNameDesc -> BookSort.ByNameDesc
    BookSortDto.ByPriority -> BookSort.ByPriority
    BookSortDto.ByPriorityDesc -> BookSort.ByPriorityDesc
  }