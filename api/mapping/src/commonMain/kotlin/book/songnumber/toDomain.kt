package io.github.alelk.pws.api.mapping.book.songnumber

import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink

fun SongNumberLinkDto.toDomain() = SongNumberLink(songId.toDomain(), number)