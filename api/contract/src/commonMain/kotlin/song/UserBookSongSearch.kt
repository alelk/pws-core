package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.resources.Resource

@Resource("/v1/users/me/books/songs/search")
class UserBookSongSearch(
    val query: String,
    val bookId: BookIdDto? = null,
    val type: SearchTypeDto? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val highlight: Boolean? = null
)