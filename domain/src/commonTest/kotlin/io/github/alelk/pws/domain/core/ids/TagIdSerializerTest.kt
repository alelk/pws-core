package io.github.alelk.pws.domain.core.ids

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class TagIdSerializerTest : StringSpec({

  // TagId sealed interface serialization tests
  "serialize predefined tag id to json" {
    val tagId: TagId = TagId.Predefined("favorite")
    val json = Json.encodeToString(TagId.serializer(), tagId)
    json shouldBe """"favorite""""
  }

  "deserialize predefined tag id from json" {
    val value = Json.decodeFromString(TagId.serializer(), "\"favorite\"")
    value shouldBe TagId.Predefined("favorite")
    value.shouldBeInstanceOf<TagId.Predefined>()
  }

  "serialize custom tag id to json" {
    val tagId: TagId = TagId.Custom(123)
    val json = Json.encodeToString(TagId.serializer(), tagId)
    json shouldBe """"custom-00123""""
  }

  "deserialize custom tag id from json" {
    val value = Json.decodeFromString(TagId.serializer(), "\"custom-00123\"")
    value shouldBe TagId.Custom(123)
    value.shouldBeInstanceOf<TagId.Custom>()
  }

  "serialize and deserialize cyrillic predefined tag id" {
    val tagId: TagId = TagId.Predefined("Избранное")
    val json = Json.encodeToString(TagId.serializer(), tagId)
    json shouldBe """"Избранное""""
    val parsed = Json.decodeFromString(TagId.serializer(), json)
    parsed shouldBe tagId
  }

  // TagId.Predefined own serializer tests
  "serialize TagId.Predefined directly" {
    val predefined = TagId.Predefined("my-tag")
    val json = Json.encodeToString(TagId.Predefined.serializer(), predefined)
    json shouldBe """"my-tag""""
  }

  "deserialize TagId.Predefined directly" {
    val value = Json.decodeFromString(TagId.Predefined.serializer(), "\"my-tag\"")
    value shouldBe TagId.Predefined("my-tag")
  }

  "serialize TagId.Predefined with digits" {
    val predefined = TagId.Predefined("tag123")
    val json = Json.encodeToString(TagId.Predefined.serializer(), predefined)
    json shouldBe """"tag123""""
  }

  "deserialize TagId.Predefined with underscore" {
    val value = Json.decodeFromString(TagId.Predefined.serializer(), "\"my_tag_1\"")
    value shouldBe TagId.Predefined("my_tag_1")
  }

  // TagId.Custom own serializer tests
  "serialize TagId.Custom directly" {
    val custom = TagId.Custom(42)
    val json = Json.encodeToString(TagId.Custom.serializer(), custom)
    json shouldBe """"custom-00042""""
  }

  "deserialize TagId.Custom directly" {
    val value = Json.decodeFromString(TagId.Custom.serializer(), "\"custom-00042\"")
    value shouldBe TagId.Custom(42)
  }

  "serialize TagId.Custom with large number" {
    val custom = TagId.Custom(99999)
    val json = Json.encodeToString(TagId.Custom.serializer(), custom)
    json shouldBe """"custom-99999""""
  }

  "deserialize TagId.Custom with leading zeros" {
    val value = Json.decodeFromString(TagId.Custom.serializer(), "\"custom-00001\"")
    value shouldBe TagId.Custom(1)
    value.number shouldBe 1
  }
})