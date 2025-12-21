package io.github.alelk.pws.api.mapping.songreference

import io.github.alelk.pws.api.contract.songreference.SongRefReasonDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.songreference.model.SongReference

fun SongRefReason.toDto(): SongRefReasonDto = when (this) {
  SongRefReason.Variation -> SongRefReasonDto.VARIATION
}

fun SongReference.toDto(): SongReferenceDto = SongReferenceDto(
  songId = songId.toDto(),
  refSongId = refSongId.toDto(),
  reason = reason.toDto(),
  volume = volume,
  priority = priority
)
