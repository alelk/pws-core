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
 * Songs are filtered to those belonging to the requested book. Each song's [Song.number]
 * is remapped so the extracted book's number is always primary (first), regardless of which
 * book "owns" the song in the deduplicated collection.
 *
 * SongReferences are filtered to those where at least one side belongs to the book's songs.
 *
 * Tags are filtered so each tag's [Tag.songs] set only contains numbers that belong to this
 * book. Tags with no remaining songs are dropped.
 */
fun CollectionBundle.extractBook(bookId: BookId): BookBundle? {
  val book = books.find { it.id == bookId } ?: return null
  val bookSongs = songs
    .filter { song -> song.allNumbers.any { it.bookId == bookId } }
    .map { song ->
      val bookNumber = song.allNumbers.first { it.bookId == bookId }
      val otherNumbers = song.allNumbers.filter { it.bookId != bookId }
      song.copy(number = bookNumber, numbers = otherNumbers)
    }
  val songIds = bookSongs.map { it.id }.toSet()
  val refs = songReferences?.filter { it.songId in songIds || it.refSongId in songIds }
  val bookSongNumbers: Set<SongNumber> = bookSongs.map { it.number }.toSet()
  val filteredTags = tags?.mapNotNull { tag ->
    val filteredSongs = tag.songs.filter { it in bookSongNumbers }.toSet()
    if (filteredSongs.isEmpty()) null
    else if (tag.id != null)
      Tag(tag.id, tag.name, tag.color, tag.priority, tag.predefined, filteredSongs)
    else
      Tag(tag.name, tag.color, filteredSongs)
  }?.ifEmpty { null }
  return BookBundle(
    metadata = BookBundle.Metadata(createdAt = metadata.createdAt),
    book = book,
    songs = bookSongs,
    songReferences = refs?.ifEmpty { null },
    tags = filteredTags,
  )
}


