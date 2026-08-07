package io.github.alelk.pws.domain.telemetry

import io.kotest.core.spec.style.StringSpec

class NoOpTelemetryTest : StringSpec({

  // The default binding must swallow everything: call sites in shared code run in tests and on
  // targets with no provider, and a throwing no-op would turn instrumentation into a crash source.
  "accepts every call without throwing" {
    with(NoOpTelemetry) {
      recordError(IllegalStateException("boom"))
      recordError(IllegalStateException("boom"), "group", mapOf(TelemetryAttr.BOOK_ID to "PV3300"))
      log("breadcrumb")
      event(TelemetryEvent.SONG_OPEN)
      event("undeclared", mapOf("anything" to null))
      setUserProperty(TelemetryAttr.FLAVOR, "ru")
      setUserProperty(TelemetryAttr.FLAVOR, null)
    }
  }
})
