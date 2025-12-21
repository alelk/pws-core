package io.github.alelk.pws.api.mapping.songreference

import io.github.alelk.pws.api.contract.songreference.SongRefReasonDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceCreateRequestDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference

fun SongRefReasonDto.toDomain(): SongRefReason = when (this) {
  SongRefReasonDto.VARIATION -> SongRefReason.Variation
}

fun SongReferenceDto.toDomain(): SongReference = SongReference(
  songId = songId.toDomain(),
  refSongId = refSongId.toDomain(),
  reason = reason.toDomain(),
  volume = volume,
  priority = priority
)

fun SongReferenceCreateRequestDto.toDomainCommand(songId: SongId): CreateSongReferenceCommand =
  CreateSongReferenceCommand(
    songId = songId,
    refSongId = refSongId.toDomain(),
    reason = reason.toDomain(),
    volume = volume,
    priority = priority
  )

fun SongReferenceUpdateRequestDto.toDomainCommand(songId: SongId, refSongId: SongId): UpdateSongReferenceCommand =
  UpdateSongReferenceCommand(
    songId = songId,
    refSongId = refSongId,
    reason = reason?.toDomain(),
    volume = volume,
    priority = priority
  )
