package io.github.alelk.pws.database.book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.person.Person

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: BookId,
    @ColumnInfo(name = "version") val version: Version,
    @ColumnInfo(name = "locales") val locales: List<Locale>,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "display_short_name") val displayShortName: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "release_date") val releaseDate: Year? = null,
    @ColumnInfo(name = "authors") val authors: List<Person>? = null,
    @ColumnInfo(name = "creators") val creators: List<Person>? = null,
    @ColumnInfo(name = "reviewers") val reviewers: List<Person>? = null,
    @ColumnInfo(name = "editors") val editors: List<Person>? = null,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "preface") val preface: String? = null
) {
  init {
    require(name.isNotBlank()) { "book $id name must not be blank" }
    require(displayShortName.isNotBlank()) { "book $id display short name must not be blank" }
    require(displayName.isNotBlank()) { "book $id display name must not be blank" }
    require(locales.isNotEmpty()) { "book $id must have at least one locale" }
  }
}