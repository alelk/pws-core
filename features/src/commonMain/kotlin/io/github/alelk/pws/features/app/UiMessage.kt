package io.github.alelk.pws.features.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.home_load_error_message
import io.github.alelk.pws.features.resources.search_error_title
import io.github.alelk.pws.features.resources.song_detail_not_found_message
import io.github.alelk.pws.features.resources.tag_not_found
import io.github.alelk.pws.features.resources.tags_snackbar_error_prefix
import org.jetbrains.compose.resources.getString

/**
 * Typed UI message produced by ScreenModels. Carries no localized strings — the
 * composable layer is responsible for resolving the key + args via [resolveString].
 *
 * Rationale: the project skill bans localized strings inside ScreenModels (the
 * i18n boundary lives at the composable layer). Raw [Throwable.message] strings
 * also leak English into UI on any locale.
 */
sealed interface UiMessage {
  data object SongNotFound : UiMessage
  data object TagNotFound : UiMessage
  data object GenericLoadError : UiMessage
  data object SearchFailed : UiMessage
  data class Failure(val detail: String? = null) : UiMessage
}

suspend fun UiMessage.resolveString(): String = when (this) {
  UiMessage.SongNotFound -> getString(Res.string.song_detail_not_found_message)
  UiMessage.TagNotFound -> getString(Res.string.tag_not_found)
  UiMessage.GenericLoadError -> getString(Res.string.home_load_error_message)
  UiMessage.SearchFailed -> getString(Res.string.search_error_title)
  is UiMessage.Failure -> getString(Res.string.tags_snackbar_error_prefix, detail ?: "")
}

/** Resolve [message] to a localized string asynchronously; returns null until resolved. */
@Composable
fun rememberResolved(message: UiMessage?): String? {
  val resolved = produceState<String?>(initialValue = null, message) {
    value = message?.resolveString()
  }
  return resolved.value
}
