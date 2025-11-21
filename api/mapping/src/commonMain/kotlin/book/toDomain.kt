package io.github.alelk.pws.api.mapping.book

import io.github.alelk.pws.api.contract.book.*
import io.github.alelk.pws.api.mapping.core.*
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.query.BookSort
import io.github.alelk.pws.domain.core.NonEmptyString

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