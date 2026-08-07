package io.github.alelk.pws.domain.telemetry

/**
 * Last line of defence before anything leaves the device.
 *
 * The app stores song lyrics and the user's own edits, so a telemetry payload that accidentally
 * carries content would be a privacy incident, not just noise. Every [Telemetry] implementation runs
 * its payloads through here:
 *
 * - keys outside [TelemetryAttr.keys] are **dropped** (allow-list, not a block-list — a new call
 *   site cannot leak by forgetting to sanitise);
 * - `null` values are dropped;
 * - string values are trimmed and truncated to [MAX_VALUE_LENGTH], which is short enough that a
 *   lyric line or a full search query cannot survive intact;
 * - numbers and booleans pass through unchanged, everything else is stringified first.
 */
object TelemetryPrivacy {

  /** Values longer than this are cut. Deliberately short: ids and enum-like values are all we send. */
  const val MAX_VALUE_LENGTH = 64

  /** Truncation marker appended to a shortened value, so a cut value is recognisable in reports. */
  const val TRUNCATION_MARKER = "…"

  /** True when [name] is part of the declared event vocabulary. */
  fun isKnownEvent(name: String): Boolean = name in TelemetryEvent.names

  /** Applies the allow-list and value limits to event parameters. */
  fun sanitize(params: Map<String, Any?>): Map<String, Any> =
    params.asSequence()
      .filter { (key, _) -> key in TelemetryAttr.keys }
      .mapNotNull { (key, value) -> sanitizeValue(value)?.let { key to it } }
      .toMap()

  /** Applies the allow-list and value limits to error attributes (string-valued). */
  fun sanitizeAttributes(attributes: Map<String, String>): Map<String, String> =
    attributes.asSequence()
      .filter { (key, _) -> key in TelemetryAttr.keys }
      .mapNotNull { (key, value) -> truncate(value)?.let { key to it } }
      .toMap()

  /**
   * Sanitises a breadcrumb. Breadcrumbs are free-form by nature, so they are truncated but never
   * enriched — call sites must pass identifiers only.
   */
  fun sanitizeMessage(message: String): String? = truncate(message)

  private fun sanitizeValue(value: Any?): Any? = when (value) {
    null -> null
    is Number, is Boolean -> value
    is String -> truncate(value)
    else -> truncate(value.toString())
  }

  private fun truncate(value: String): String? {
    val trimmed = value.trim()
    return when {
      trimmed.isEmpty() -> null
      trimmed.length <= MAX_VALUE_LENGTH -> trimmed
      else -> trimmed.take(MAX_VALUE_LENGTH) + TRUNCATION_MARKER
    }
  }
}
