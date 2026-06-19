package io.github.alelk.pws.portable.serialization

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.portable.model.BookCatalog
import io.github.alelk.pws.portable.model.BookCatalogEntry
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize

/**
 * Backward- and forward-compatibility tests for [BookCatalog] JSON serialization.
 *
 * PURPOSE: Ensure that catalog files produced by older builder versions can always be
 * parsed by newer app versions (backward), and that older app versions gracefully
 * handle catalogs with additional fields added in the future (forward).
 *
 * RULE: Never delete or modify frozen JSON strings. Only ADD new cases.
 * Each frozen snapshot must continue to deserialize without error for all future versions.
 */
class CatalogCompatibilityTest : StringSpec({

  // ---------------------------------------------------------------------------
  // Frozen snapshot v1 — minimal catalog, only required fields present.
  // Book has no optional fields (description, authors, releaseDate, etc.).
  // When new optional fields are added to Book/BookCatalogEntry in the future,
  // this JSON must still deserialize without error; optional fields = null.
  // ---------------------------------------------------------------------------
  val frozenV1MinimalJson = """
    {
      "version": "3.2.2",
      "books": [
        {
          "book": {
            "id": "NPE",
            "version": "1.1",
            "locales": ["ru"],
            "name": "Новые песни Евангелия",
            "displayShortName": "НПЕ",
            "displayName": "Новые песни Евангелия"
          },
          "songCount": 150,
          "fileSizeBytes": 102400,
          "checksum": "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"
        }
      ]
    }
  """.trimIndent()

  "frozen v1 minimal: catalog version and book count" {
    val catalog = CatalogSerializer.decode(frozenV1MinimalJson)
    catalog.version shouldBe "3.2.2"
    catalog.books shouldHaveSize 1
  }

  "frozen v1 minimal: book fields deserialized correctly" {
    val entry = CatalogSerializer.decode(frozenV1MinimalJson).books[0]
    entry.book.id shouldBe BookId.parse("NPE")
    entry.book.version shouldBe Version(1, 1)
    entry.book.locales shouldBe listOf(Locale.of("ru"))
    entry.book.name shouldBe "Новые песни Евангелия"
    entry.book.displayShortName shouldBe "НПЕ"
    entry.book.displayName shouldBe "Новые песни Евангелия"
  }

  "frozen v1 minimal: optional book fields are null" {
    val entry = CatalogSerializer.decode(frozenV1MinimalJson).books[0]
    entry.book.description.shouldBeNull()
    entry.book.authors.shouldBeNull()
    entry.book.releaseDate.shouldBeNull()
  }

  "frozen v1 minimal: catalog entry metadata" {
    val entry = CatalogSerializer.decode(frozenV1MinimalJson).books[0]
    entry.songCount shouldBe 150
    entry.fileSizeBytes shouldBe 102400L
    entry.checksum shouldBe "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot v1 — full catalog, all currently supported optional fields.
  // ---------------------------------------------------------------------------
  val frozenV1FullJson = """
    {
      "version": "3.2.2",
      "books": [
        {
          "book": {
            "id": "PV800",
            "version": "3.300",
            "locales": ["ru"],
            "name": "Песнь Возрождения",
            "displayShortName": "ПВ",
            "displayName": "Песнь Возрождения 800",
            "description": "Основной сборник евангельских песен"
          },
          "songCount": 800,
          "fileSizeBytes": 512000,
          "checksum": "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        },
        {
          "book": {
            "id": "EvangelskiPisni",
            "version": "1.0",
            "locales": ["uk"],
            "name": "Євангельські пісні",
            "displayShortName": "ЄП",
            "displayName": "Євангельські пісні"
          },
          "songCount": 350,
          "fileSizeBytes": 204800,
          "checksum": "cafebabecafebabecafebabecafebabecafebabecafebabecafebabecafebabe"
        }
      ]
    }
  """.trimIndent()

  "frozen v1 full: two books deserialized correctly" {
    val catalog = CatalogSerializer.decode(frozenV1FullJson)
    catalog.books shouldHaveSize 2
  }

  "frozen v1 full: first book (RU) fields" {
    val entry = CatalogSerializer.decode(frozenV1FullJson).books[0]
    entry.book.id shouldBe BookId.parse("PV800")
    entry.book.version shouldBe Version(3, 300)
    entry.book.locales shouldBe listOf(Locale.of("ru"))
    entry.book.description shouldBe "Основной сборник евангельских песен"
    entry.songCount shouldBe 800
    entry.fileSizeBytes shouldBe 512000L
  }

  "frozen v1 full: second book (UK) fields" {
    val entry = CatalogSerializer.decode(frozenV1FullJson).books[1]
    entry.book.id shouldBe BookId.parse("EvangelskiPisni")
    entry.book.version shouldBe Version(1, 0)
    entry.book.locales shouldBe listOf(Locale.of("uk"))
    entry.book.description.shouldBeNull()
    entry.songCount shouldBe 350
  }

  "frozen v1 full: decode → encode → decode roundtrip" {
    val original = CatalogSerializer.decode(frozenV1FullJson)
    val reEncoded = CatalogSerializer.encode(original)
    val reDecoded = CatalogSerializer.decode(reEncoded)
    reDecoded shouldBe original
  }

  // ---------------------------------------------------------------------------
  // Forward-compatibility: JSON has unknown extra fields added in a future version.
  // An older app reading a newer catalog must not crash — ignoreUnknownKeys = true.
  // ---------------------------------------------------------------------------
  val frozenFutureJson = """
    {
      "version": "4.0.0",
      "schemaVersion": 2,
      "generatedAt": "2027-01-01T00:00:00Z",
      "books": [
        {
          "book": {
            "id": "PV800",
            "version": "3.300",
            "locales": ["ru"],
            "name": "Песнь Возрождения",
            "displayShortName": "ПВ",
            "displayName": "Песнь Возрождения 800",
            "coverUrl": "https://example.com/covers/PV800.png"
          },
          "songCount": 800,
          "fileSizeBytes": 512000,
          "checksum": "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef",
          "downloadPriority": 1,
          "tags": ["worship", "ru"]
        }
      ]
    }
  """.trimIndent()

  "forward compat: unknown top-level fields do not cause failure" {
    val catalog = CatalogSerializer.decode(frozenFutureJson)
    catalog.version shouldBe "4.0.0"
    catalog.books shouldHaveSize 1
  }

  "forward compat: unknown fields inside book entry do not cause failure" {
    val entry = CatalogSerializer.decode(frozenFutureJson).books[0]
    entry.book.id shouldBe BookId.parse("PV800")
    entry.songCount shouldBe 800
    entry.fileSizeBytes shouldBe 512000L
  }

  "forward compat: unknown fields inside book model do not cause failure" {
    val entry = CatalogSerializer.decode(frozenFutureJson).books[0]
    entry.book.name shouldBe "Песнь Возрождения"
    entry.book.description.shouldBeNull()
  }

  // ---------------------------------------------------------------------------
  // Roundtrip: encode a catalog and decode it back — result must equal original.
  // ---------------------------------------------------------------------------
  "roundtrip: encode → decode preserves all fields" {
    val original = BookCatalog(
      version = "3.2.2",
      books = listOf(
        BookCatalogEntry(
          book = io.github.alelk.pws.portable.model.Book(
            id = BookId.parse("NPE"),
            version = Version(1, 1),
            locales = listOf(Locale.of("ru")),
            name = "Новые песни Евангелия",
            displayShortName = "НПЕ",
            displayName = "Новые песни Евангелия",
            description = "Новые песни Евангелия",
          ),
          songCount = 150,
          fileSizeBytes = 102400L,
          checksum = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
        )
      )
    )
    val decoded = CatalogSerializer.decode(CatalogSerializer.encode(original))
    decoded shouldBe original
  }
})
