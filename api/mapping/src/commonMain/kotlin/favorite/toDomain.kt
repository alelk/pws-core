package io.github.alelk.pws.api.mapping.favorite

import io.github.alelk.pws.api.contract.favorite.FavoriteSubjectDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject

fun FavoriteSubjectDto.toDomain(): FavoriteSubject = when (this) {
  is FavoriteSubjectDto.BookedSong -> FavoriteSubject.BookedSong(songNumberId.toDomain())
  is FavoriteSubjectDto.StandaloneSong -> FavoriteSubject.StandaloneSong(songId.toDomain())
}