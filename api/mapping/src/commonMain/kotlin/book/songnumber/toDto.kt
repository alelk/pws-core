package io.github.alelk.pws.api.mapping.book.songnumber

import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink

fun SongNumberLink.toDto() = SongNumberLinkDto(songId.toDto(), number)