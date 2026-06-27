package io.github.alelk.pws.portable.model

import com.charleskorn.kaml.Yaml
import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.Pv3300
import io.github.alelk.pws.domain.core.ids.Pv800
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.tonality.Tonality
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.datetime.LocalDateTime

/**
 * Backward-compatibility tests for [Backup] serialization format.
 *
 * PURPOSE: Ensure that real user backup files created with older versions of the app
 * can always be deserialized after model changes (new optional fields, renamed fields, etc.).
 *
 * RULE: Never delete or modify the frozen YAML strings in this file.
 * Only ADD new test cases when extending the model.
 * Each frozen snapshot must continue to deserialize without error for all future versions.
 */
class BackupCompatibilityTest : StringSpec({

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Backup v1 — original format, first public release
  // Fields present: metadata(v1), songs, favorites, tags, bookPreferences, settings
  // Fields absent: metadata.defaultLocale, metadata.source (not yet in v1)
  // ---------------------------------------------------------------------------
  val frozenV1Yaml = """
    |metadata:
    |  createdAt: "2025-01-01T00:00:10"
    |  version: 1
    |songs:
    |  - number:
    |      bookId: PV3300
    |      number: 1
    |    id: 1
    |    version: "1.0"
    |    locale: "en"
    |    name: "Song Name"
    |    lyric: |-
    |      Verse 1 Line 1
    |      Verse 1 Line 2
    |
    |      Verse 2 Line 1
    |      Verse 2 Line 2
    |    tonalities:
    |    - "a major"
    |    - "b major"
    |    author: "Author"
    |    translator: "Translator"
    |    composer: "Composer"
    |    bibleRef: "Bible Ref"
    |favorites:
    |  - bookId: "PV3300"
    |    number: 1
    |  - bookId: "PV3300"
    |    number: 2
    |tags:
    |  - name: "tag1"
    |    color: "#ff0000"
    |    songs:
    |      "PV3300":
    |        - "1-3"
    |        - "10"
    |bookPreferences:
    |  - bookId: "PV3300"
    |    preference: 5
    |  - bookId: "PV800"
    |    preference: 10
    |settings:
    |  "setting-1": "value-1"
    |  "setting-2": "value-2"""".trimMargin()

  "frozen v1: backup metadata deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV1Yaml)
    backup.metadata.version shouldBe 1
    backup.metadata.createdAt shouldBe LocalDateTime.parse("2025-01-01T00:00:10")
    backup.metadata.defaultLocale.shouldBeNull()
    backup.metadata.source.shouldBeNull()
  }

  "frozen v1: songs deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV1Yaml)
    val songs = backup.songs!!
    songs shouldHaveSize 1
    songs[0].run {
      number shouldBe SongNumber(BookId.Pv3300, 1)
      id shouldBe SongId(1)
      version shouldBe Version(1, 0)
      locale shouldBe Locale.of("en")
      name shouldBe "Song Name"
      lyric shouldBe "Verse 1 Line 1\nVerse 1 Line 2\n\nVerse 2 Line 1\nVerse 2 Line 2"
      tonalities shouldContainExactly listOf(Tonality.A_MAJOR, Tonality.B_MAJOR)
      author shouldBe Person("Author")
      translator shouldBe Person("Translator")
      composer shouldBe Person("Composer")
      bibleRef shouldBe BibleRef("Bible Ref")
    }
  }

  "frozen v1: favorites deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV1Yaml)
    backup.favorites shouldContainExactly listOf(
      SongNumber(BookId.Pv3300, 1),
      SongNumber(BookId.Pv3300, 2)
    )
  }

  "frozen v1: tags deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV1Yaml)
    val tags = backup.tags!!
    tags shouldHaveSize 1
    tags[0].run {
      name shouldBe "tag1"
      color shouldBe Color.parse("#ff0000")
      songs shouldContainExactly setOf(
        SongNumber(BookId.Pv3300, 1),
        SongNumber(BookId.Pv3300, 2),
        SongNumber(BookId.Pv3300, 3),
        SongNumber(BookId.Pv3300, 10),
      )
    }
  }

  "frozen v1: bookPreferences deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV1Yaml)
    backup.bookPreferences shouldContainExactly listOf(
      BookPreference(BookId.Pv3300, 5),
      BookPreference(BookId.Pv800, 10),
    )
  }

  "frozen v1: settings deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV1Yaml)
    backup.settings shouldBe mapOf("setting-1" to "value-1", "setting-2" to "value-2")
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Backup v2 — minimal (all data fields null)
  // ---------------------------------------------------------------------------
  val frozenV2MinimalYaml = """
    |metadata:
    |  createdAt: "2025-01-01T00:00:10"
    |  defaultLocale: "en"
    |  source: "source-1"
    |  version: 2
    |songs: null
    |favorites: null
    |tags: null
    |bookPreferences: null
    |settings: null""".trimMargin()

  "frozen v2 minimal: roundtrip deserialization" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV2MinimalYaml)
    backup.metadata.version shouldBe 2
    backup.metadata.createdAt shouldBe LocalDateTime.parse("2025-01-01T00:00:10")
    backup.metadata.defaultLocale shouldBe Locale.of("en")
    backup.metadata.source shouldBe "source-1"
    backup.songs.shouldBeNull()
    backup.favorites.shouldBeNull()
    backup.tags.shouldBeNull()
    backup.bookPreferences.shouldBeNull()
    backup.settings.shouldBeNull()
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Song — v1 format, without future optional fields
  // When fields like `year`, `priority` are added to Song with defaults,
  // this YAML must still deserialize without error.
  // ---------------------------------------------------------------------------
  val frozenSongMinimalYaml = """
    |number:
    |  bookId: "PV3300"
    |  number: 5
    |version: "2.1"
    |locale: "ru"
    |name: "Minimal Song"
    |lyric: |-
    |  Line 1
    |  Line 2""".trimMargin()

  "frozen song minimal: deserialized without optional fields" {
    val song = Yaml.default.decodeFromString(Song.serializer(), frozenSongMinimalYaml)
    song.number shouldBe SongNumber(BookId.Pv3300, 5)
    song.version shouldBe Version(2, 1)
    song.locale shouldBe Locale.of("ru")
    song.name shouldBe "Minimal Song"
    song.lyric shouldBe "Line 1\nLine 2"
    song.id.shouldBeNull()
    song.tonalities.shouldBeNull()
    song.author.shouldBeNull()
    song.translator.shouldBeNull()
    song.composer.shouldBeNull()
    song.bibleRef.shouldBeNull()
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Song — full v1 format with all currently supported fields
  // ---------------------------------------------------------------------------
  val frozenSongFullYaml = """
    |number:
    |  bookId: "PV3300"
    |  number: 1
    |id: 42
    |version: "1.0"
    |locale: "en"
    |name: "Full Song"
    |lyric: |-
    |  Verse 1
    |
    |  Verse 2
    |tonalities:
    |- "a major"
    |author: "John"
    |translator: "Jane"
    |composer: "Jack"
    |bibleRef: "John 3:16"""".trimMargin()

  "frozen song full: all fields deserialized correctly" {
    val song = Yaml.default.decodeFromString(Song.serializer(), frozenSongFullYaml)
    song.number shouldBe SongNumber(BookId.Pv3300, 1)
    song.id shouldBe SongId(42)
    song.version shouldBe Version(1, 0)
    song.locale shouldBe Locale.of("en")
    song.name shouldBe "Full Song"
    song.lyric shouldBe "Verse 1\n\nVerse 2"
    song.tonalities shouldContainExactly listOf(Tonality.A_MAJOR)
    song.author shouldBe Person("John")
    song.translator shouldBe Person("Jane")
    song.composer shouldBe Person("Jack")
    song.bibleRef shouldBe BibleRef("John 3:16")
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Song — multi-book (numbers field, added in portable-data v3)
  // Old YAML without `numbers` must still deserialize; allNumbers falls back to [number].
  // ---------------------------------------------------------------------------
  val frozenSongMultiBookYaml = """
    |number:
    |  bookId: "PV3300"
    |  number: 1
    |numbers:
    |- bookId: "PV800"
    |  number: 42
    |- bookId: "pws-uk"
    |  number: 7
    |version: "1.0"
    |locale: "ru"
    |name: "Multi-Book Song"
    |lyric: |-
    |  Line 1""".trimMargin()

  "frozen song multi-book: numbers field deserialized correctly" {
    val song = Yaml.default.decodeFromString(Song.serializer(), frozenSongMultiBookYaml)
    song.number shouldBe SongNumber(BookId.Pv3300, 1)
    song.numbers shouldContainExactly listOf(
      SongNumber(BookId.parse("PV800"), 42),
      SongNumber(BookId.parse("pws-uk"), 7),
    )
    song.allNumbers shouldContainExactly listOf(
      SongNumber(BookId.Pv3300, 1),
      SongNumber(BookId.parse("PV800"), 42),
      SongNumber(BookId.parse("pws-uk"), 7),
    )
  }

  "frozen song minimal: allNumbers falls back to [number] when numbers is absent" {
    val song = Yaml.default.decodeFromString(Song.serializer(), frozenSongMinimalYaml)
    song.numbers shouldContainExactly emptyList()
    song.allNumbers shouldContainExactly listOf(SongNumber(BookId.Pv3300, 5))
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Tag — v1 format, without future optional fields (id, priority, predefined)
  // When those fields are added with defaults, this YAML must still deserialize.
  // ---------------------------------------------------------------------------
  val frozenTagMinimalYaml = """
    |name: "worship"
    |color: "#0000ff"
    |songs:
    |  "PV3300":
    |  - "1"
    |  - "5-7"""".trimMargin()

  "frozen tag minimal: deserialized without id/priority/predefined fields" {
    val tag = Yaml.default.decodeFromString(Tag.serializer(), frozenTagMinimalYaml)
    tag.name shouldBe "worship"
    tag.color shouldBe Color.parse("#0000ff")
    tag.songs shouldContainExactly setOf(
      SongNumber(BookId.Pv3300, 1),
      SongNumber(BookId.Pv3300, 5),
      SongNumber(BookId.Pv3300, 6),
      SongNumber(BookId.Pv3300, 7),
    )
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Tag — multi-book
  // ---------------------------------------------------------------------------
  val frozenTagMultiBookYaml = """
    |name: "praise"
    |color: "#00ff00"
    |songs:
    |  "PV3300":
    |  - "1-3"
    |  - "10"
    |  "PV800":
    |  - "2"""".trimMargin()

  "frozen tag multi-book: songs from all books deserialized" {
    val tag = Yaml.default.decodeFromString(Tag.serializer(), frozenTagMultiBookYaml)
    tag.name shouldBe "praise"
    tag.songs shouldContainExactly setOf(
      SongNumber(BookId.Pv3300, 1),
      SongNumber(BookId.Pv3300, 2),
      SongNumber(BookId.Pv3300, 3),
      SongNumber(BookId.Pv3300, 10),
      SongNumber(BookId.Pv800, 2),
    )
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Backup v2 — with songs and tags, no future fields
  // Simulates a real user backup file that must survive model evolution.
  // ---------------------------------------------------------------------------
  val frozenV2FullYaml = """
    |metadata:
    |  createdAt: "2026-01-15T12:30:00"
    |  defaultLocale: "ru"
    |  source: "pws-android-2.0"
    |  version: 2
    |songs:
    |  - number:
    |      bookId: PV3300
    |      number: 10
    |    id: 100
    |    version: "1.0"
    |    locale: "ru"
    |    name: "Тестовая Песня"
    |    lyric: |-
    |      Строка 1
    |      Строка 2
    |  - number:
    |      bookId: PV800
    |      number: 3
    |    version: "1.5"
    |    locale: "ru"
    |    name: "Другая Песня"
    |    lyric: |-
    |      Куплет 1
    |
    |      Куплет 2
    |    author: "Автор"
    |favorites:
    |  - bookId: "PV3300"
    |    number: 10
    |tags:
    |  - name: "избранное"
    |    color: "#ff6600"
    |    songs:
    |      "PV3300":
    |      - "10"
    |      "PV800":
    |      - "3"
    |bookPreferences:
    |  - bookId: "PV3300"
    |    preference: 1
    |settings:
    |  "theme": "dark"
    |  "font_size": "18"""".trimMargin()

  "frozen v2 full: complete backup deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV2FullYaml)
    backup.metadata.version shouldBe 2
    backup.metadata.defaultLocale shouldBe Locale.of("ru")
    backup.metadata.source shouldBe "pws-android-2.0"

    val songs = backup.songs!!
    songs shouldHaveSize 2
    songs[0].number shouldBe SongNumber(BookId.Pv3300, 10)
    songs[0].id shouldBe SongId(100)
    songs[1].number shouldBe SongNumber(BookId.Pv800, 3)
    songs[1].id.shouldBeNull()
    songs[1].author shouldBe Person("Автор")

    backup.favorites shouldContainExactly listOf(SongNumber(BookId.Pv3300, 10))

    val tags = backup.tags!!
    tags shouldHaveSize 1
    tags[0].name shouldBe "избранное"
    tags[0].songs shouldContainExactly setOf(
      SongNumber(BookId.Pv3300, 10),
      SongNumber(BookId.Pv800, 3),
    )

    backup.bookPreferences shouldContainExactly listOf(BookPreference(BookId.Pv3300, 1))
    backup.settings shouldBe mapOf("theme" to "dark", "font_size" to "18")
  }

  "frozen v2 full: deserialization is idempotent (decode → encode → decode)" {
    val original = Yaml.default.decodeFromString(Backup.serializer(), frozenV2FullYaml)
    val reEncoded = Yaml.default.encodeToString(Backup.serializer(), original)
    val reDecoded = Yaml.default.decodeFromString(Backup.serializer(), reEncoded)
    reDecoded shouldBe original
  }

  // ---------------------------------------------------------------------------
  // Frozen snapshot: Backup v2 — with history field (added after initial release)
  // Old YAML without `history` must still deserialize with history = null.
  // New YAML with `history` must deserialize correctly.
  // ---------------------------------------------------------------------------
  val frozenV2WithHistoryYaml = """
    |metadata:
    |  createdAt: "2026-06-26T10:00:00"
    |  source: "pws-android-3.4"
    |  version: 2
    |songs: null
    |favorites:
    |- bookId: "PV3300"
    |  number: 10
    |tags: null
    |bookPreferences: null
    |history:
    |- songNumber:
    |    bookId: "PV3300"
    |    number: 10
    |  accessTimestamp: "2026-06-26T09:30:00"
    |- songNumber:
    |    bookId: "PV3300"
    |    number: 5
    |  accessTimestamp: "2026-06-25T20:00:00"
    |settings: null""".trimMargin()

  "frozen v2 with history: history entries deserialized correctly" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV2WithHistoryYaml)
    val history = backup.history!!
    history shouldHaveSize 2
    history[0].songNumber shouldBe SongNumber(BookId.Pv3300, 10)
    history[0].accessTimestamp shouldBe LocalDateTime.parse("2026-06-26T09:30:00")
    history[1].songNumber shouldBe SongNumber(BookId.Pv3300, 5)
    history[1].accessTimestamp shouldBe LocalDateTime.parse("2026-06-25T20:00:00")
  }

  "frozen v2 with history: idempotent roundtrip" {
    val original = Yaml.default.decodeFromString(Backup.serializer(), frozenV2WithHistoryYaml)
    val reEncoded = Yaml.default.encodeToString(Backup.serializer(), original)
    val reDecoded = Yaml.default.decodeFromString(Backup.serializer(), reEncoded)
    reDecoded shouldBe original
  }

  "frozen v1 (no history field): history deserializes as null" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV1Yaml)
    backup.history.shouldBeNull()
  }

  "frozen v2 minimal (no history field): history deserializes as null" {
    val backup = Yaml.default.decodeFromString(Backup.serializer(), frozenV2MinimalYaml)
    backup.history.shouldBeNull()
  }
})

