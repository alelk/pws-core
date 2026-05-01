package io.github.alelk.pws.domain.book.command

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.error.InvalidInputError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.person.Person

/** Command to create a Book. */
data class CreateBookCommand(
  val id: BookId,
  val version: Version = Version(1, 0),
  val locales: List<Locale>,
  val name: NonEmptyString,
  val displayShortName: NonEmptyString,
  val displayName: NonEmptyString,
  val releaseDate: Year? = null,
  val authors: List<Person> = emptyList(),
  val creators: List<Person> = emptyList(),
  val reviewers: List<Person> = emptyList(),
  val editors: List<Person> = emptyList(),
  val description: String? = null,
  val preface: String? = null,
  val enabled: Boolean = true,
  val priority: Int = if (enabled) 10 else 0
) {
  init {
      require(locales.isNotEmpty()) { "book $id locales must not be empty" }
  }

  companion object {
    fun validated(
      id: BookId,
      version: Version = Version(1, 0),
      locales: List<Locale>,
      name: NonEmptyString,
      displayShortName: NonEmptyString,
      displayName: NonEmptyString,
      releaseDate: Year? = null,
      authors: List<Person> = emptyList(),
      creators: List<Person> = emptyList(),
      reviewers: List<Person> = emptyList(),
      editors: List<Person> = emptyList(),
      description: String? = null,
      preface: String? = null,
      enabled: Boolean = true,
      priority: Int = if (enabled) 10 else 0
    ): Either<InvalidInputError, CreateBookCommand> =
      if (locales.isEmpty()) {
        InvalidInputError("book.locales", "book $id locales must not be empty").left()
      } else {
        CreateBookCommand(
          id = id,
          version = version,
          locales = locales,
          name = name,
          displayShortName = displayShortName,
          displayName = displayName,
          releaseDate = releaseDate,
          authors = authors,
          creators = creators,
          reviewers = reviewers,
          editors = editors,
          description = description,
          preface = preface,
          enabled = enabled,
          priority = priority
        ).right()
      }
  }
}