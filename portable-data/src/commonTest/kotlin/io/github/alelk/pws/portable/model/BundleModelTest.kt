package io.github.alelk.pws.portable.model

import com.charleskorn.kaml.Yaml
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.ids.BookId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import io.github.alelk.pws.domain.core.Color

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

