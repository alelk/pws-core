package io.github.alelk.pws.domain.core.ids

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.checkAll

class TagIdTest : StringSpec({

  // Predefined TagId tests
  "create predefined tag id from valid string contains latin letters and digits" {
    val tagId = TagId.parse("Text-1234")
    tagId.shouldBeInstanceOf<TagId.Predefined>()
    tagId.identifier shouldBe "Text-1234"
  }

  "create predefined tag id from valid string contains cyrillic" {
    val tagId = TagId.parse("Текст_1234")
    tagId.shouldBeInstanceOf<TagId.Predefined>()
    tagId.identifier shouldBe "Текст_1234"
  }

  "create predefined tag id directly" {
    val tagId = TagId.Predefined("favorite")
    tagId.identifier shouldBe "favorite"
    tagId.toString() shouldBe "favorite"
  }

  "predefined tag id fails when string starts from digit" {
    shouldThrow<IllegalArgumentException> {
      TagId.parse("1Tag")
    }.message shouldContain "tag id should"
  }

  "predefined tag id fails when string ends with underscore" {
    shouldThrow<IllegalArgumentException> {
      TagId.parse("Tag123_")
    }.message shouldContain "tag id should"
  }

  "predefined tag id fails when starts with custom prefix" {
    shouldThrow<IllegalArgumentException> {
      TagId.Predefined("custom-12345")
    }.message shouldContain "should not start with"
  }

  "predefined tag id fails when too long" {
    shouldThrow<IllegalArgumentException> {
      TagId.Predefined("verylongtagidthatexceedslimit_12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
    }.message shouldContain "less than 64"
  }

  // Custom TagId tests
  "create custom tag id from number" {
    val tagId = TagId.Custom(123)
    tagId.shouldBeInstanceOf<TagId.Custom>()
    tagId.identifier shouldBe "custom-00123"
    tagId.number shouldBe 123
  }

  "create custom tag id from string" {
    val tagId = TagId.Custom.parse("custom-00042")
    tagId.number shouldBe 42
  }

  "parse custom tag id via TagId.parse" {
    val tagId = TagId.parse("custom-00001")
    tagId.shouldBeInstanceOf<TagId.Custom>()
    tagId.number shouldBe 1
  }

  "custom tag id fails when number is zero" {
    shouldThrow<IllegalArgumentException> {
      TagId.Custom(0)
    }.message shouldContain "greater than 0"
  }

  "custom tag id fails when missing prefix" {
    shouldThrow<IllegalArgumentException> {
      TagId.Custom("wrong-00123")
    }.message shouldContain "should start with"
  }

  "custom tag id fails when contains non-digits after prefix" {
    shouldThrow<IllegalArgumentException> {
      TagId.Custom("custom-abc12")
    }.message shouldContain "only digits"
  }

  "custom tag id fails when wrong length" {
    shouldThrow<IllegalArgumentException> {
      TagId.Custom("custom-123")
    }.message shouldContain "exactly"
  }

  "custom tag id random generates valid id" {
    val tagId = TagId.Custom.random()
    tagId.identifier.startsWith("custom-") shouldBe true
    tagId.number shouldBe tagId.number // just verify it doesn't throw
  }

  "custom tag id next returns incremented id" {
    val tagId = TagId.Custom(5)
    val next = tagId.next()
    next.number shouldBe 6
  }

  // Predefined property tests
  "predefined tag has predefined property true" {
    val tagId: TagId = TagId.Predefined("favorite")
    tagId.predefined shouldBe true
  }

  "custom tag has predefined property false" {
    val tagId: TagId = TagId.Custom(123)
    tagId.predefined shouldBe false
  }

  "parsed predefined tag has predefined property true" {
    val tagId = TagId.parse("my-tag")
    tagId.predefined shouldBe true
  }

  "parsed custom tag has predefined property false" {
    val tagId = TagId.parse("custom-00001")
    tagId.predefined shouldBe false
  }

  // Comparable tests
  "tag ids are comparable" {
    val a = TagId.Predefined("alpha")
    val b = TagId.Predefined("beta")
    (a < b) shouldBe true
    (b > a) shouldBe true
  }

  "predefined and custom tag ids are comparable" {
    val predefined = TagId.Predefined("zzz")
    val custom = TagId.Custom(1)
    // custom-00001 < zzz alphabetically
    (custom < predefined) shouldBe true
  }

  // Round-trip tests
  "convert predefined tag id to string and parse it back" {
    val original = TagId.Predefined("my-tag")
    val parsed = TagId.parse(original.toString())
    parsed shouldBe original
  }

  "convert custom tag id to string and parse it back" {
    val original = TagId.Custom(999)
    val parsed = TagId.parse(original.toString())
    parsed shouldBe original
  }

  "convert tag id to string and parse it back for random tag id" {
    checkAll(Arb.tagId()) { tagId ->
      val string = tagId.toString()
      val parsed = TagId.parse(string)
      parsed shouldBe tagId
    }
  }
})