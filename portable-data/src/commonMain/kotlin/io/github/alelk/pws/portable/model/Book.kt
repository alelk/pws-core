package io.github.alelk.pws.portable.model

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.LocaleSerializer
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.VersionSerializer
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.YearSerializer
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.BookIdSerializer
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.person.PersonSerializer
import kotlinx.serialization.Serializable

/**
 * Serializable book descriptor (no Room annotations).
 * Mirrors [io.github.alelk.pws.database.book.BookEntity] +
 * [io.github.alelk.pws.database.bookstatistic.BookStatisticEntity.priority].
 *
 * Used in [CollectionBundle] and [BookBundle].
 */
@Serializable
data class Book(
  @Serializable(with = BookIdSerializer::class)
  val id: BookId,
  @Serializable(with = VersionSerializer::class)
  val version: Version,
  val locales: List<@Serializable(with = LocaleSerializer::class) Locale>,
  val name: String,
  val displayShortName: String,
  val displayName: String,
  val priority: Int = 0,
  @Serializable(with = YearSerializer::class)
  val releaseDate: Year? = null,
  val authors: List<@Serializable(with = PersonSerializer::class) Person>? = null,
  val creators: List<@Serializable(with = PersonSerializer::class) Person>? = null,
  val reviewers: List<@Serializable(with = PersonSerializer::class) Person>? = null,
  val editors: List<@Serializable(with = PersonSerializer::class) Person>? = null,
  val description: String? = null,
  val preface: String? = null,
) {
  init {
    require(name.isNotBlank()) { "book $id name must not be blank" }
    require(displayShortName.isNotBlank()) { "book $id displayShortName must not be blank" }
    require(displayName.isNotBlank()) { "book $id displayName must not be blank" }
    require(locales.isNotEmpty()) { "book $id must have at least one locale" }
  }
}

