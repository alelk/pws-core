package io.github.alelk.pws.domain.telemetry

/**
 * The complete vocabulary of product-analytics event names. Keeping them here (rather than as
 * string literals at call sites) makes the collected surface reviewable in one place — which is
 * exactly what the privacy audit and the store data-safety declarations need.
 *
 * Adding an event means adding a constant here and listing it in [names].
 */
object TelemetryEvent {
  /** A screen became the top of the navigation stack. Param: [TelemetryAttr.SCREEN]. */
  const val SCREEN_VIEW = "screen_view"

  /** A song page was opened. Params: [TelemetryAttr.SONG_ID], [TelemetryAttr.BOOK_ID]. */
  const val SONG_OPEN = "song_open"

  /** A search ran. Params: [TelemetryAttr.QUERY_LENGTH], [TelemetryAttr.RESULT_COUNT] — never the query text. */
  const val SEARCH = "search"

  /** A songbook install finished. Params: [TelemetryAttr.BOOK_ID], [TelemetryAttr.RESULT]. */
  const val BOOK_INSTALL = "book_install"

  /** A songbook update finished. Params: [TelemetryAttr.BOOK_ID], [TelemetryAttr.RESULT]. */
  const val BOOK_UPDATE = "book_update"

  /** A songbook was removed. Params: [TelemetryAttr.BOOK_ID], [TelemetryAttr.RESULT]. */
  const val BOOK_UNINSTALL = "book_uninstall"

  /** A bundle was imported from a local file. Params: [TelemetryAttr.RESULT]. */
  const val BOOK_IMPORT = "book_import"

  /** First-launch onboarding was left. Params: [TelemetryAttr.RESULT] (`installed`/`skipped`). */
  const val ONBOARDING_COMPLETE = "onboarding_complete"

  /** The store paywall was opened (premium-selling builds only). */
  const val PAYWALL_SHOWN = "paywall_shown"

  /** A purchase attempt finished. Params: [TelemetryAttr.RESULT]. */
  const val PURCHASE = "purchase"

  /** The donation prompt was shown or acted on. Params: [TelemetryAttr.RESULT]. */
  const val DONATION_PROMPT = "donation_prompt"

  /** Every known event name — the allow-list enforced by [TelemetryPrivacy.isKnownEvent]. */
  val names: Set<String> = setOf(
    SCREEN_VIEW, SONG_OPEN, SEARCH,
    BOOK_INSTALL, BOOK_UPDATE, BOOK_UNINSTALL, BOOK_IMPORT,
    ONBOARDING_COMPLETE, PAYWALL_SHOWN, PURCHASE, DONATION_PROMPT,
  )
}

/**
 * The complete vocabulary of event/error attribute keys. Anything not listed here is dropped by
 * [TelemetryPrivacy.sanitize] — a key can only ever carry non-personal, non-content data.
 */
object TelemetryAttr {
  /** Screen identifier (class name), e.g. `SongDetailScreen`. */
  const val SCREEN = "screen"

  /** Songbook id, e.g. `PV3300`. Content identifier, not personal data. */
  const val BOOK_ID = "book_id"

  /** Song id. Content identifier, not personal data. */
  const val SONG_ID = "song_id"

  /** Where in a multi-step operation something happened, e.g. `download`, `decode`, `import`. */
  const val STAGE = "stage"

  /** Coarse outcome: `ok`, `error`, `cancelled`, `skipped`, … Never a raw exception message. */
  const val RESULT = "result"

  /** Content/catalog source name or `asset`/`file`/`network`. */
  const val SOURCE = "source"

  /** Exception class name (not its message — messages can embed user data). */
  const val ERROR_TYPE = "error_type"

  /** Length of the user's search query. The query text itself is never reported. */
  const val QUERY_LENGTH = "query_length"

  /** Number of results/items involved. */
  const val RESULT_COUNT = "result_count"

  /** Product flavor: `ru` / `uk` / `full` / `rustore`. */
  const val FLAVOR = "flavor"

  /** Content bundle variant the build talks to: `release` / `debug`. */
  const val BUNDLE_VARIANT = "bundle_variant"

  /** App version name. */
  const val APP_VERSION = "app_version"

  /** Device UI language (ISO code), e.g. `ru`. Coarse locale, not a precise identifier. */
  const val DEVICE_LANGUAGE = "device_language"

  /** How many songbooks the user currently has installed. */
  const val INSTALLED_BOOKS = "installed_books"

  /** Every allowed key — the allow-list enforced by [TelemetryPrivacy.sanitize]. */
  val keys: Set<String> = setOf(
    SCREEN, BOOK_ID, SONG_ID, STAGE, RESULT, SOURCE, ERROR_TYPE,
    QUERY_LENGTH, RESULT_COUNT, FLAVOR, BUNDLE_VARIANT, APP_VERSION,
    DEVICE_LANGUAGE, INSTALLED_BOOKS,
  )
}

/** Values for [TelemetryAttr.RESULT]. Keeping outcomes enum-like keeps the reports groupable. */
object TelemetryResult {
  const val OK = "ok"
  const val ERROR = "error"
  const val CANCELLED = "cancelled"
  const val SKIPPED = "skipped"
  const val INSTALLED = "installed"
  const val SHOWN = "shown"
  const val CLICKED = "clicked"
  const val DISMISSED = "dismissed"
}
