package io.github.alelk.pws.portable.serialization

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.portable.model.Book
import io.github.alelk.pws.portable.model.CollectionBundle
import io.github.alelk.pws.portable.model.Song
import io.github.alelk.pws.portable.model.SongNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.comparables.shouldBeLessThan
import kotlinx.datetime.LocalDateTime

class BundleGzipTest : StringSpec({

  val bundle = CollectionBundle(
    metadata = CollectionBundle.Metadata(
      createdAt = LocalDateTime.parse("2026-01-01T00:00:00"),
      locale = Locale.of("ru"),
      bundleId = "pws-ru-2026.1",
    ),
    books = listOf(
      Book(
        id = BookId.parse("pws-ru"),
        version = Version(1, 0),
        locales = listOf(Locale.of("ru")),
        name = "Песнь Возрождения",
        displayShortName = "ПВ",
        displayName = "Песнь Возрождения 3300",
      )
    ),
    songs = listOf(
      Song(
        number = SongNumber(BookId.parse("pws-ru"), 1),
        version = Version(1, 0),
        locale = Locale.of("ru"),
        name = "Песня 1",
        lyric = "Строка 1\nСтрока 2\n\nСтрока 3",
      )
    ),
  )

  "encode gzip then decode returns identical bundle" {
    val bytes = BundleSerializer.encodeGzip(bundle)
    val decoded = BundleSerializer.decodeCollectionGzip(bytes)
    decoded shouldBe bundle
  }

  "gzip bytes smaller than plain yaml" {
    val yaml = BundleSerializer.encodeToString(bundle)
    val gzip = BundleSerializer.encodeGzip(bundle)
    gzip.size shouldBeLessThan yaml.length
  }

  "plain yaml roundtrip" {
    val yaml = BundleSerializer.encodeToString(bundle)
    val decoded = BundleSerializer.decodeCollectionFromString(yaml)
    decoded shouldBe bundle
  }
})

