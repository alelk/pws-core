package io.github.alelk.pws.domain.core

import io.github.alelk.pws.domain.core.ids.BookId
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ReferenceTest : StringSpec({
  val json = kotlinx.serialization.json.Json { encodeDefaults = true }

  "bible ref serialise to json" {
    val r = BibleRef(text = "John 3:16")
    val expectedJson = """{"type":"bible-ref","value":"John 3:16"}"""
    json.encodeToString(Reference.serializer(), r) shouldEqualJson expectedJson
  }

  "bible ref deserialize from json" {
    val expectedJson = """{"type":"bible-ref","value":"John 3:16"}"""
    json.decodeFromString(Reference.serializer(), expectedJson) shouldBe BibleRef(text = "John 3:16")
  }

  "song ref serialise to json" {
    val r = SongRef(
      reason = SongRefReason.Variation,
      number = SongNumber(BookId.parse("book1"), 10),
      80
    )
    val expectedJson = """{"type":"song-ref","reason":"variation","bookId":"book1","number":10,"volume":80}"""
    json.encodeToString(Reference.serializer(), r) shouldEqualJson expectedJson
  }

  "song ref deserialize from json" {
    val r = SongRef(
      reason = SongRefReason.Variation,
      number = SongNumber(BookId.parse("book1"), 10),
      80
    )
    val expectedJson = """{"type":"song-ref","reason":"variation","bookId":"book1","number":10,"volume":80}"""
    json.decodeFromString(Reference.serializer(), expectedJson) shouldBe r
  }

})