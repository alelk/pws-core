package io.github.alelk.pws.portable.model

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.LocaleSerializer
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.BookIdSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Portable snapshot of a full deduplicated book collection.
 *
 * Mirrors [io.github.alelk.pws.library.manager.model.BookCollection]:
 * - Songs are deduplicated across all books.
 * - [Song.number] contains book-numbers from all books the song belongs to.
 *
 * Used for asset delivery (one file for the whole locale collection).
 * Stored as YAML + gzip: `{locale}.collection.yaml.gz`
 */
@Serializable
data class CollectionBundle(
  val metadata: Metadata,
  val books: List<Book>,
  val bookPriorities: Map<@Serializable(with = BookIdSerializer::class) BookId, Int> = emptyMap(),
  /** Deduplicated songs. Each song's [Song.number] may reference multiple books. */
  val songs: List<Song>,
  val songReferences: List<SongReference>? = null,
  val tags: List<Tag>? = null,
) {

  @Serializable
  data class Metadata(
    val version: Int = 1,
    val createdAt: LocalDateTime,
    @Serializable(with = LocaleSerializer::class)
    val locale: Locale,
    /** Unique identifier for this bundle, e.g. "pws-ru-2026.1" */
    val bundleId: String,
  )

  init {
    val bookIds = books.map { it.id }.toSet()
    require(bookIds.size == books.size) { "CollectionBundle contains duplicate book ids" }
    require(bookPriorities.keys.all { it in bookIds }) {
      "bookPriorities references unknown bookId(s): ${(bookPriorities.keys - bookIds).joinToString()}"
    }
    val tagIds = tags?.mapNotNull { it.id } ?: emptyList()
    require(tagIds.size == tagIds.distinct().size) { "CollectionBundle contains duplicate tag ids" }
  }
}

