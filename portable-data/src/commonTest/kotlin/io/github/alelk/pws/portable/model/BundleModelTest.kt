package io.github.alelk.pws.portable.model

import com.charleskorn.kaml.Yaml
import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.datetime.LocalDateTime

class BookTest : StringSpec({

  val book = Book(
    id = BookId.parse("pws-ru"),
    version = Version(1, 0),
    locales = listOf(Locale.of("ru")),
    name = "Песнь Возрождения",
    displayShortName = "ПВ",
    displayName = "Песнь Возрождения 3300",
    priority = 100,
  )

  "serialize book to yaml" {
    val yaml = Yaml.default.encodeToString(Book.serializer(), book)
    val decoded = Yaml.default.decodeFromString(Book.serializer(), yaml)
    decoded shouldBe book
  }

  "deserialize book from yaml without optional fields" {
    val yaml = """
      |id: "pws-ru"
      |version: "1.0"
      |locales:
      |- "ru"
      |name: "Песнь Возрождения"
      |displayShortName: "ПВ"
      |displayName: "Песнь Возрождения 3300"""".trimMargin()
    val decoded = Yaml.default.decodeFromString(Book.serializer(), yaml)
    decoded.id shouldBe BookId.parse("pws-ru")
    decoded.priority shouldBe 0
    decoded.releaseDate shouldBe null
  }
})

class SongReferenceTest : StringSpec({

  val ref = SongReference(
    songId = io.github.alelk.pws.domain.core.ids.SongId(1),
    refSongId = io.github.alelk.pws.domain.core.ids.SongId(2),
    reason = "variation",
    volume = 85,
  )

  "serialize and deserialize song reference" {
    val yaml = Yaml.default.encodeToString(SongReference.serializer(), ref)
    val decoded = Yaml.default.decodeFromString(SongReference.serializer(), yaml)
    decoded shouldBe ref
  }

  "song reference cannot reference itself" {
    val id = io.github.alelk.pws.domain.core.ids.SongId(5)
    kotlin.test.assertFailsWith<IllegalArgumentException> {
      SongReference(songId = id, refSongId = id, reason = "variation", volume = 50)
    }
  }
})

class CollectionBundleTest : StringSpec({

  val book1 = Book(
    id = BookId.parse("pws-ru"),
    version = Version(1, 0),
    locales = listOf(Locale.of("ru")),
    name = "ПВ 3300",
    displayShortName = "ПВ",
    displayName = "Песнь Возрождения 3300",
    priority = 100,
  )

  val song1 = Song(
    number = SongNumber(BookId.parse("pws-ru"), 1),
    version = Version(1, 0),
    locale = Locale.of("ru"),
    name = "Тестовая Песня",
    lyric = "Строка 1\nСтрока 2",
  )

  val bundle = CollectionBundle(
    metadata = CollectionBundle.Metadata(
      createdAt = LocalDateTime.parse("2026-01-01T00:00:00"),
      locale = Locale.of("ru"),
      bundleId = "pws-ru-2026.1",
    ),
    books = listOf(book1),
    bookPriorities = mapOf(BookId.parse("pws-ru") to 100),
    songs = listOf(song1),
  )

  "collection bundle roundtrip yaml" {
    val yaml = Yaml.default.encodeToString(CollectionBundle.serializer(), bundle)
    val decoded = Yaml.default.decodeFromString(CollectionBundle.serializer(), yaml)
    decoded shouldBe bundle
  }

  "extractBook returns correct book bundle" {
    val bookBundle = bundle.extractBook(BookId.parse("pws-ru"))
    bookBundle!!.book shouldBe book1
    bookBundle.songs.size shouldBe 1
  }

  "extractBook returns null for unknown bookId" {
    bundle.extractBook(BookId.parse("unknown")) shouldBe null
  }
})

class ExtractBookTest : StringSpec({

  val bookA = Book(
    id = BookId.parse("book-a"),
    version = Version(1, 0),
    locales = listOf(Locale.of("ru")),
    name = "Book A",
    displayShortName = "A",
    displayName = "Book A Full",
    priority = 100,
  )

  val bookB = Book(
    id = BookId.parse("book-b"),
    version = Version(1, 0),
    locales = listOf(Locale.of("ru")),
    name = "Book B",
    displayShortName = "B",
    displayName = "Book B Full",
    priority = 50,
  )

  // Song present in both books (deduplicated). Primary number belongs to book-a (higher priority).
  val sharedSong = Song(
    id = SongId(1),
    number = SongNumber(BookId.parse("book-a"), 10),
    numbers = listOf(SongNumber(BookId.parse("book-b"), 20)),
    version = Version(1, 0),
    locale = Locale.of("ru"),
    name = "Shared",
    lyric = "Lyric",
  )

  // Song belonging only to book-a.
  val songOnlyA = Song(
    id = SongId(2),
    number = SongNumber(BookId.parse("book-a"), 11),
    version = Version(1, 0),
    locale = Locale.of("ru"),
    name = "Only A",
    lyric = "Lyric A",
  )

  // Song belonging only to book-b.
  val songOnlyB = Song(
    id = SongId(3),
    number = SongNumber(BookId.parse("book-b"), 21),
    version = Version(1, 0),
    locale = Locale.of("ru"),
    name = "Only B",
    lyric = "Lyric B",
  )

  val refSharedToA = SongReference(SongId(1), SongId(2), "variation", 75)
  val refSharedToB = SongReference(SongId(1), SongId(3), "variation", 60)

  // Tag with songs from both books (as in a full collection).
  val tagAll = Tag(
    id = TagId.parse("praise"),
    name = "Praise",
    color = Color.parse("#ff0000"),
    priority = 1,
    predefined = true,
    songs = setOf(
      SongNumber(BookId.parse("book-a"), 10),
      SongNumber(BookId.parse("book-a"), 11),
      SongNumber(BookId.parse("book-b"), 20),
      SongNumber(BookId.parse("book-b"), 21),
    ),
  )

  val collection = CollectionBundle(
    metadata = CollectionBundle.Metadata(
      createdAt = LocalDateTime.parse("2026-01-01T00:00:00"),
      locale = Locale.of("ru"),
      bundleId = "test-bundle",
    ),
    books = listOf(bookA, bookB),
    bookPriorities = mapOf(BookId.parse("book-a") to 100, BookId.parse("book-b") to 50),
    songs = listOf(sharedSong, songOnlyA, songOnlyB),
    songReferences = listOf(refSharedToA, refSharedToB),
    tags = listOf(tagAll),
  )

  "extractBook for book-a includes only its songs" {
    val bundle = collection.extractBook(BookId.parse("book-a"))!!
    bundle.songs.map { it.id } shouldContainExactlyInAnyOrder listOf(SongId(1), SongId(2))
  }

  "extractBook for book-b includes only its songs" {
    val bundle = collection.extractBook(BookId.parse("book-b"))!!
    bundle.songs.map { it.id } shouldContainExactlyInAnyOrder listOf(SongId(1), SongId(3))
  }

  "extractBook remaps primary number to the extracted book" {
    val bundle = collection.extractBook(BookId.parse("book-b"))!!
    val shared = bundle.songs.first { it.id == SongId(1) }
    // book-b number must be primary
    shared.number shouldBe SongNumber(BookId.parse("book-b"), 20)
    // book-a number goes to extras
    shared.numbers shouldContainExactlyInAnyOrder listOf(SongNumber(BookId.parse("book-a"), 10))
  }

  "extractBook for book-a keeps primary number unchanged" {
    val bundle = collection.extractBook(BookId.parse("book-a"))!!
    val shared = bundle.songs.first { it.id == SongId(1) }
    shared.number shouldBe SongNumber(BookId.parse("book-a"), 10)
    shared.numbers shouldContainExactlyInAnyOrder listOf(SongNumber(BookId.parse("book-b"), 20))
  }

  "extractBook filters tags to current book song numbers only" {
    val bundleB = collection.extractBook(BookId.parse("book-b"))!!
    val tag = bundleB.tags?.firstOrNull { it.id == TagId.parse("praise") }
    tag shouldNotBe null
    // Only book-b numbers should survive
    tag!!.songs shouldContainExactlyInAnyOrder setOf(
      SongNumber(BookId.parse("book-b"), 20),
      SongNumber(BookId.parse("book-b"), 21),
    )
  }

  "extractBook drops tags with no songs in current book" {
    // Tag with only book-a songs → must be absent from book-b bundle
    val tagOnlyA = Tag(
      id = TagId.parse("only-a-tag"),
      name = "Only A",
      color = Color.parse("#00ff00"),
      priority = 1,
      predefined = true,
      songs = setOf(SongNumber(BookId.parse("book-a"), 10)),
    )
    val col = collection.copy(tags = listOf(tagOnlyA))
    val bundleB = col.extractBook(BookId.parse("book-b"))!!
    bundleB.tags.shouldBeNull()
  }

  "extractBook includes refs where either side belongs to the book" {
    val bundleB = collection.extractBook(BookId.parse("book-b"))!!
    // sharedSong (id=1) is in book-b → both refs from shared should be included
    val refIds = bundleB.songReferences?.map { it.songId to it.refSongId } ?: emptyList()
    refIds shouldContainExactlyInAnyOrder listOf(
      SongId(1) to SongId(2),
      SongId(1) to SongId(3),
    )
  }
})

