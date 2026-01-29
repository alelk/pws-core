package io.github.alelk.pws.domain.core.ids

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.checkAll

class TagIdTest : StringSpec({
  "create tag id from valid string contains latin letters and digits" {
    TagId.parse("Text-1234") shouldBe TagId("Text-1234")
  }

  "create tag id from valid string contains cyrillic" {
    TagId.parse("Текст_1234") shouldBe TagId("Текст_1234")
  }

  "create tag id fails when string starts from digit" {
    shouldThrow<IllegalArgumentException> {
      TagId.parse("1Tag")
    }.message shouldContain "tag id should"
  }

  "create tag id fails when string ends with underscore" {
    shouldThrow<IllegalArgumentException> {
      TagId.parse("Tag123_")
    }.message shouldContain "tag id should"
  }

  "convert tag id to string and parse it back for random tag id" {
    checkAll(Arb.tagId()) { tagId ->
      val string = tagId.toString()
      val parsed = TagId.parse(string)
      parsed shouldBe tagId
    }
  }
})