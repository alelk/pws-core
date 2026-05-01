package io.github.alelk.pws.domain.book.command

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.error.InvalidInputError
import io.github.alelk.pws.domain.core.ids.BookId

/** Patch-like update for Book fields. */
data class UpdateBookCommand(
  val id: BookId,
  val locales: List<Locale>? = null,
  val name: NonEmptyString? = null,
  val displayShortName: NonEmptyString? = null,
  val displayName: NonEmptyString? = null,
  val releaseDate: OptionalField<Year?> = OptionalField.Unchanged,
  val description: OptionalField<String?> = OptionalField.Unchanged,
  val preface: OptionalField<String?> = OptionalField.Unchanged,
  val version: Version? = null,
  val expectedVersion: Version? = null,
  val enabled: Boolean? = null,
  val priority: Int? = null
) {

  init {
    if (locales != null) require(locales.isNotEmpty()) { "book $id locales must not be empty" }
  }

  fun hasChanges(): Boolean =
    locales != null ||
      name != null ||
      displayShortName != null ||
      displayName != null ||
      version != null ||
      enabled != null ||
      priority != null ||
      releaseDate is OptionalField.Set ||
      description is OptionalField.Set ||
      preface is OptionalField.Set

  companion object {
    fun validated(
      id: BookId,
      locales: List<Locale>? = null,
      name: NonEmptyString? = null,
      displayShortName: NonEmptyString? = null,
      displayName: NonEmptyString? = null,
      releaseDate: OptionalField<Year?> = OptionalField.Unchanged,
      description: OptionalField<String?> = OptionalField.Unchanged,
      preface: OptionalField<String?> = OptionalField.Unchanged,
      version: Version? = null,
      expectedVersion: Version? = null,
      enabled: Boolean? = null,
      priority: Int? = null
    ): Either<InvalidInputError, UpdateBookCommand> {
      if (locales != null && locales.isEmpty()) {
        return InvalidInputError("book.locales", "book $id locales must not be empty").left()
      }
      val command = UpdateBookCommand(
        id = id,
        locales = locales,
        name = name,
        displayShortName = displayShortName,
        displayName = displayName,
        releaseDate = releaseDate,
        description = description,
        preface = preface,
        version = version,
        expectedVersion = expectedVersion,
        enabled = enabled,
        priority = priority
      )
      return if (command.hasChanges()) {
        command.right()
      } else {
        InvalidInputError("book", "At least one field should be changed").left()
      }
    }
  }
}

