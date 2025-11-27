package io.github.alelk.pws.api.mapping.book

import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.core.getOrElse

fun UpdateBookCommand.toRequestDto(): BookUpdateRequestDto =
  BookUpdateRequestDto(
    locale = locale?.toDto(),
    name = name?.value,
    displayShortName = displayShortName?.value,
    displayName = displayName?.value,
    version = version?.toDto(),
    enabled = enabled,
    priority = priority,
    releaseDate = releaseDate.getOrElse { null }?.toDto(),
    description = description.getOrElse { null },
    preface = preface.getOrElse { null },
    expectedVersion = expectedVersion?.toDto()
  )

fun CreateBookCommand.toRequestDto(): BookCreateRequestDto =
  BookCreateRequestDto(
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