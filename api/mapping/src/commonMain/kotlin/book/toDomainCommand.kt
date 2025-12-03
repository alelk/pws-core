package io.github.alelk.pws.api.mapping.book

import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.ids.BookId

fun BookUpdateRequestDto.toDomainCommand(id: BookIdDto): UpdateBookCommand = UpdateBookCommand(
  id = BookId.parse(id.value),
  locale = locale?.toDomain(),
  name = name?.let(::NonEmptyString),
  displayShortName = displayShortName?.let(::NonEmptyString),
  displayName = displayName?.let(::NonEmptyString),
  releaseDate = OptionalField.fromNullable(releaseDate?.toDomain(), treatNullAsClear = false),
  description = OptionalField.fromNullable(description, treatNullAsClear = false),
  preface = OptionalField.fromNullable(preface, treatNullAsClear = false),
  expectedVersion = expectedVersion?.toDomain(),
  version = version?.toDomain(),
  enabled = enabled,
  priority = priority
)

fun BookCreateRequestDto.toDomainCommand(): CreateBookCommand = CreateBookCommand(
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