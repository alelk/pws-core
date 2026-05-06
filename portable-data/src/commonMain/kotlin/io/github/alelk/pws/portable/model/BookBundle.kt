package io.github.alelk.pws.portable.model

import io.github.alelk.pws.domain.core.ids.BookId
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Portable snapshot of a single book.
 *
 * Used for per-book dynamic delivery (Play Asset Delivery, CDN).
 * Songs contain only the number within this book (not cross-book numbers).
 * Stored as YAML + gzip: `{bookId}.book.yaml.gz`
 *
 * Can be extracted from a [CollectionBundle] via [CollectionBundle.extractBook].
 */
@Serializable
data class BookBundle(
  val metadata: Metadata,
  val book: Book,
  val songs: List<Song>,
  val songReferences: List<SongReference>? = null,
  val tags: List<Tag>? = null,
) {

  @Serializable
  data class Metadata(
    val version: Int = 1,
    val createdAt: LocalDateTime,
  )
}

/**
 * Extracts a single [BookBundle] from this [CollectionBundle] for the given [bookId].
 * Returns `null` if the book is not found.
 *
 * Songs are filtered to those belonging to the requested book.
 * SongReferences are filtered to those where at least one side belongs to the book's songs.
 */
fun CollectionBundle.extractBook(bookId: BookId): BookBundle? {
  val book = books.find { it.id == bookId } ?: return null
  val bookSongs = songs.filter { song -> song.allNumbers.any { it.bookId == bookId } }
  val songIds = bookSongs.map { it.id }.toSet()
  val refs = songReferences?.filter { it.songId in songIds || it.refSongId in songIds }
  return BookBundle(
    metadata = BookBundle.Metadata(createdAt = metadata.createdAt),
    book = book,
    songs = bookSongs,
    songReferences = refs?.ifEmpty { null },
    tags = tags,
  )
}


