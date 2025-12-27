package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.SongId

/**
 * Song search suggestion for autocomplete.
 */
data class SongSearchSuggestion(
    val id: SongId,
    val name: NonEmptyString,
    val books: List<String>,
    val snippet: String? = null
)