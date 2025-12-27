package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

@Serializable
data class SongSearchSuggestionDto(
    val id: SongIdDto,
    val name: String,
    val books: List<String>,
    val snippet: String? = null
)