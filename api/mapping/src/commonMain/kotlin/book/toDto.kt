package io.github.alelk.pws.api.mapping.book

import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.model.BookSummary

fun BookSummary.toDto(): BookSummaryDto = BookSummaryDto(
  id = id.toDto(),
  version = version.toDto(),
  locale = locale.toDto(),
  name = name,
  displayShortName = displayShortName,
  displayName = displayName,
  countSongs = countSongs,
  firstSongNumberId = firstSongNumberId.toDto(),
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
  firstSongNumberId = firstSongNumberId.toDto(),
  countSongs = countSongs,
  enabled = enabled,
  priority = priority
)