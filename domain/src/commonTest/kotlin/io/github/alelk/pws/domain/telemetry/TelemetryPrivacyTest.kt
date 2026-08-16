package io.github.alelk.pws.domain.telemetry

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class TelemetryPrivacyTest : StringSpec({

  "drops keys outside the allow-list" {
    val result = TelemetryPrivacy.sanitize(
      mapOf(
        TelemetryAttr.BOOK_ID to "PV3300",
        "song_lyrics" to "Господь — Пастырь мой",
        "query" to "какой-то поисковый запрос",
      )
    )
    result shouldContainExactly mapOf(TelemetryAttr.BOOK_ID to "PV3300")
  }

  "drops null values" {
    TelemetryPrivacy.sanitize(mapOf(TelemetryAttr.BOOK_ID to null)) shouldContainExactly emptyMap()
  }

  "keeps numbers and booleans as-is" {
    TelemetryPrivacy.sanitize(
      mapOf(TelemetryAttr.RESULT_COUNT to 42, TelemetryAttr.QUERY_LENGTH to 7)
    ) shouldContainExactly mapOf(TelemetryAttr.RESULT_COUNT to 42, TelemetryAttr.QUERY_LENGTH to 7)
  }

  "truncates long values so free text cannot survive intact" {
    val long = "a".repeat(TelemetryPrivacy.MAX_VALUE_LENGTH * 3)
    val value = TelemetryPrivacy.sanitize(mapOf(TelemetryAttr.SCREEN to long))[TelemetryAttr.SCREEN] as String
    value.length shouldBe TelemetryPrivacy.MAX_VALUE_LENGTH + TelemetryPrivacy.TRUNCATION_MARKER.length
    value.endsWith(TelemetryPrivacy.TRUNCATION_MARKER) shouldBe true
  }

  "sanitizeAttributes applies the same allow-list" {
    TelemetryPrivacy.sanitizeAttributes(
      mapOf(TelemetryAttr.STAGE to "download", "user_note" to "personal")
    ) shouldContainExactly mapOf(TelemetryAttr.STAGE to "download")
  }

  "sanitizeMessage drops blank breadcrumbs" {
    TelemetryPrivacy.sanitizeMessage("   ").shouldBeNull()
    TelemetryPrivacy.sanitizeMessage(" book_install_started ") shouldBe "book_install_started"
  }

  "only declared events are recognised" {
    TelemetryEvent.names.forEach { TelemetryPrivacy.isKnownEvent(it) shouldBe true }
    TelemetryPrivacy.isKnownEvent("ad_hoc_event") shouldBe false
  }

  "every declared event name is unique and lowercase snake_case" {
    TelemetryEvent.names.toList().size shouldBe TelemetryEvent.names.size
    TelemetryEvent.names.filterNot { it.matches(Regex("[a-z][a-z0-9_]*")) } shouldContainExactly emptyList()
    TelemetryAttr.keys.filterNot { it.matches(Regex("[a-z][a-z0-9_]*")) } shouldContainExactly emptyList()
  }
})
