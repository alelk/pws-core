package io.github.alelk.pws.domain.core.ids

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json

class UserTagIdTest : StringSpec({

  "create UserTagId from UserId and TagId.Custom" {
    val userId = UserId("user-123")
    val customTagId = TagId.Custom(42)
    val userTagId = UserTagId(userId, customTagId)

    userTagId.value shouldBe "user-123/custom-00042"
    userTagId.userId shouldBe userId
    userTagId.customTagId shouldBe customTagId
  }

  "create UserTagId from string" {
    val userTagId = UserTagId.parse("user-abc/custom-00001")

    userTagId.userId shouldBe UserId("user-abc")
    userTagId.customTagId shouldBe TagId.Custom(1)
  }

  "UserTagId toString returns value" {
    val userTagId = UserTagId(UserId("test"), TagId.Custom(5))
    userTagId.toString() shouldBe "test/custom-00005"
  }

  "UserTagId fails when separator is missing" {
    shouldThrow<IllegalArgumentException> {
      UserTagId("user123custom-00001")
    }.message shouldContain "separator"
  }

  "UserTagId fails when userId part is blank" {
    shouldThrow<IllegalArgumentException> {
      UserTagId("/custom-00001")
    }.message shouldContain "blank"
  }

  "UserTagId fails when tag part is not custom" {
    shouldThrow<IllegalArgumentException> {
      UserTagId("user-123/predefined-tag")
    }.message shouldContain "custom tag"
  }

  "UserTagId fails when too long" {
    val longUserId = "a".repeat(70)
    shouldThrow<IllegalArgumentException> {
      UserTagId("$longUserId/custom-00001")
    }.message shouldContain "77"
  }

  "UserTagId max length is valid" {
    // 64 (userId) + 1 (/) + 12 (custom-XXXXX) = 77
    val userId = "a".repeat(64)
    val userTagId = UserTagId("$userId/custom-00001")
    userTagId.value.length shouldBe 77
  }

  // Serialization tests
  "serialize UserTagId to json" {
    val userTagId = UserTagId(UserId("user-1"), TagId.Custom(123))
    val json = Json.encodeToString(UserTagIdSerializer, userTagId)
    json shouldBe "\"user-1/custom-00123\""
  }

  "deserialize UserTagId from json" {
    val value = Json.decodeFromString(UserTagIdSerializer, "\"user-2/custom-00456\"")
    value.userId shouldBe UserId("user-2")
    value.customTagId shouldBe TagId.Custom(456)
  }

  "round-trip serialization" {
    val original = UserTagId(UserId("my-user"), TagId.Custom(99999))
    val json = Json.encodeToString(UserTagIdSerializer, original)
    val parsed = Json.decodeFromString(UserTagIdSerializer, json)
    parsed shouldBe original
  }
})
