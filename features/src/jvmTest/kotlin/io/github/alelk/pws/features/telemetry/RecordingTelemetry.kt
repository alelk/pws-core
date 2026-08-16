package io.github.alelk.pws.features.telemetry

import io.github.alelk.pws.domain.telemetry.Telemetry
import io.github.alelk.pws.domain.telemetry.TelemetryPrivacy

/**
 * [Telemetry] test double that records what instrumentation emitted.
 *
 * Payloads are stored **after** [TelemetryPrivacy] sanitisation — the same treatment a real provider
 * applies — so a test asserting "the search query text is not reported" checks what would actually
 * leave the device, not what the call site happened to pass.
 */
class RecordingTelemetry : Telemetry {

  data class RecordedEvent(val name: String, val params: Map<String, Any>)
  data class RecordedError(val throwable: Throwable, val message: String?, val attributes: Map<String, String>)

  val events = mutableListOf<RecordedEvent>()
  val errors = mutableListOf<RecordedError>()
  val breadcrumbs = mutableListOf<String>()
  val userProperties = mutableMapOf<String, String?>()

  override fun recordError(throwable: Throwable, message: String?, attributes: Map<String, String>) {
    errors += RecordedError(throwable, message, TelemetryPrivacy.sanitizeAttributes(attributes))
  }

  override fun log(message: String) {
    TelemetryPrivacy.sanitizeMessage(message)?.let { breadcrumbs += it }
  }

  override fun event(name: String, params: Map<String, Any?>) {
    events += RecordedEvent(name, TelemetryPrivacy.sanitize(params))
  }

  override fun setUserProperty(key: String, value: String?) {
    userProperties[key] = value
  }

  /** All values ever passed to this recorder, flattened — handy for "no content leaked" assertions. */
  fun allValues(): List<String> =
    events.flatMap { it.params.values.map(Any::toString) } +
      errors.flatMap { error -> error.attributes.values + listOfNotNull(error.message) } +
      breadcrumbs +
      userProperties.values.filterNotNull()
}
