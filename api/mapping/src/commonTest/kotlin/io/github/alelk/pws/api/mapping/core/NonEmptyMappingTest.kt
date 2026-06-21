package io.github.alelk.pws.api.mapping.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class NonEmptyMappingTest : StringSpec({

  "nonEmpty returns a NonEmptyString for non-blank input" {
    nonEmpty("hello", "field").value shouldBe "hello"
  }

  "nonEmpty throws on blank input" {
    shouldThrow<IllegalArgumentException> { nonEmpty("   ", "name") }
  }

  "validateNonEmpty flags blank input with the field name" {
    validateNonEmpty("", "name").shouldNotBeNull().field shouldBe "name"
    validateNonEmpty("ok", "name").shouldBeNull()
  }

  "validateNonEmptyList flags an empty collection" {
    validateNonEmptyList(emptyList<String>(), "items").shouldNotBeNull().field shouldBe "items"
    validateNonEmptyList(listOf("a"), "items").shouldBeNull()
  }
})
