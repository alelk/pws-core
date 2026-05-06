package io.github.alelk.pws.portable.serialization

import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.tonality.Tonality
import io.github.alelk.pws.portable.model.Book
import io.github.alelk.pws.portable.model.BookBundle
import io.github.alelk.pws.portable.model.CollectionBundle
import io.github.alelk.pws.portable.model.Song
import io.github.alelk.pws.portable.model.SongNumber
import io.github.alelk.pws.portable.model.SongReference
import io.github.alelk.pws.portable.model.Tag
import kotlinx.datetime.LocalDateTime

/**
 * Shared fixture data for bundle serialization golden-file tests.
 *
 * RULE: Do not change existing fixture values — this will break golden-file compatibility tests.
 * If the model changes, ADD a new fixture version alongside the old one.
 */
internal object BundleFixtures {

  val CREATED_AT: LocalDateTime = LocalDateTime.parse("2026-01-01T12:00:00")

  /** 32-byte deterministic test encryption key (all zeros with a twist). */
  val ENCRYPTION_KEY: ByteArray = ByteArray(32) { (it * 7 + 3).toByte() }

  // ── Books ──────────────────────────────────────────────────────────────

  val BOOK_RU =
    Book(
      id = BookId.parse("pws-ru"),
      version = Version(3, 300),
      locales = listOf(Locale.of("ru")),
      name = "Песнь Возрождения",
      displayShortName = "ПВ",
      displayName = "Песнь Возрождения 3300",
      priority = 100,
      releaseDate = Year(2003),
      description = "Основной сборник",
    )

  val BOOK_UK =
    Book(
      id = BookId.parse("pws-uk"),
      version = Version(1, 0),
      locales = listOf(Locale.of("uk")),
      name = "Пісня Відродження",
      displayShortName = "ПВ",
      displayName = "Пісня Відродження",
      priority = 80,
    )

  // ── Songs ──────────────────────────────────────────────────────────────

  /** Song shared between two books (deduplicated). */
  val SONG_SHARED =
    Song(
      number = SongNumber(BookId.parse("pws-ru"), 1),
      numbers = listOf(SongNumber(BookId.parse("pws-uk"), 1)),
      id = SongId(1001),
      version = Version(1, 0),
      locale = Locale.of("ru"),
      name = "Господь — мой свет",
      lyric = "Куплет 1 строка 1\nКуплет 1 строка 2\n\nКуплет 2 строка 1\nКуплет 2 строка 2",
      tonalities = listOf(Tonality.A_MAJOR, Tonality.C_MAJOR),
      author = Person("Иван Иванов"),
      composer = Person("Пётр Петров"),
      bibleRef = BibleRef("Пс. 26:1"),
      year = Year(1990),
      priority = 10,
    )

  /** Song belonging only to the Russian book. */
  val SONG_RU_ONLY =
    Song(
      number = SongNumber(BookId.parse("pws-ru"), 42),
      id = SongId(2042),
      version = Version(2, 1),
      locale = Locale.of("ru"),
      name = "Радуйся, земля",
      lyric = "Строк один\n\nСтрока два",
    )

  // ── Cross-references ───────────────────────────────────────────────────

  val REF_1_TO_2 =
    SongReference(
      songId = SongId(1001),
      refSongId = SongId(2042),
      reason = "variation",
      volume = 75,
    )

  // ── Tags ───────────────────────────────────────────────────────────────

  val TAG_WORSHIP =
    Tag(
      id = TagId.parse("worship"),
      name = "Прославление",
      color = Color.parse("#ff6600"),
      priority = 10,
      predefined = true,
      songs = setOf(
        SongNumber(BookId.parse("pws-ru"), 1),
        SongNumber(BookId.parse("pws-ru"), 42),
      ),
    )

  // ── CollectionBundle ───────────────────────────────────────────────────

  val COLLECTION_BUNDLE =
    CollectionBundle(
      metadata = CollectionBundle.Metadata(
        version = 1,
        createdAt = CREATED_AT,
        locale = Locale.of("ru"),
        bundleId = "pws-ru-fixture-v1",
      ),
      books = listOf(BOOK_RU, BOOK_UK),
      bookPriorities = mapOf(BookId.parse("pws-ru") to 100, BookId.parse("pws-uk") to 80),
      songs = listOf(SONG_SHARED, SONG_RU_ONLY),
      songReferences = listOf(REF_1_TO_2),
      tags = listOf(TAG_WORSHIP),
    )

  // ── BookBundle ─────────────────────────────────────────────────────────

  val BOOK_BUNDLE =
    BookBundle(
      metadata = BookBundle.Metadata(
        version = 1,
        createdAt = CREATED_AT,
      ),
      book = BOOK_RU,
      songs = listOf(SONG_SHARED, SONG_RU_ONLY),
      songReferences = listOf(REF_1_TO_2),
      tags = listOf(TAG_WORSHIP),
    )
}

