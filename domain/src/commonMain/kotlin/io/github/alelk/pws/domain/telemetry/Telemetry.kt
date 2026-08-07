package io.github.alelk.pws.domain.telemetry

/**
 * Port for crash/error reporting and product analytics.
 *
 * Business code depends only on this interface — never on a concrete vendor SDK. The shell binds a
 * platform implementation (Android: AppMetrica); every other target and every test gets
 * [NoOpTelemetry], so a call site is always safe to write.
 *
 * **Privacy contract.** Nothing passed here may contain user content or personal data: no song
 * lyrics, no user edits, no search query text, no names or addresses. Only action/screen
 * identifiers, ids, counters, and enum-like values. Implementations additionally run every payload
 * through [TelemetryPrivacy], which drops keys outside the allow-list and truncates values — that is
 * a safety net, not a licence to pass content in.
 */
interface Telemetry {

  /**
   * Reports a caught (non-fatal) [throwable]. [message] is a short, stable, human-readable group
   * label — errors sharing it are grouped together in the console, so it must not embed variable
   * parts (ids, counts, messages); put those in [attributes] instead.
   */
  fun recordError(throwable: Throwable, message: String? = null, attributes: Map<String, String> = emptyMap())

  /** Records a breadcrumb that gives context to a later crash/error. Identifiers only, no content. */
  fun log(message: String)

  /** Reports a product-analytics event. Use the names from [TelemetryEvent]. */
  fun event(name: String, params: Map<String, Any?> = emptyMap())

  /** Sets (or clears, when [value] is `null`) a user-level property used for slicing reports. */
  fun setUserProperty(key: String, value: String?)
}

/**
 * Telemetry that discards everything. Default binding: used on targets without a provider (jvm/js),
 * in tests, and in builds that ship without an API key.
 */
object NoOpTelemetry : Telemetry {
  override fun recordError(throwable: Throwable, message: String?, attributes: Map<String, String>) = Unit
  override fun log(message: String) = Unit
  override fun event(name: String, params: Map<String, Any?>) = Unit
  override fun setUserProperty(key: String, value: String?) = Unit
}
