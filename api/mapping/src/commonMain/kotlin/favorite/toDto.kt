package io.github.alelk.pws.api.mapping.favorite

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.favorite.FavoriteDto
import io.github.alelk.pws.api.contract.favorite.FavoriteSubjectDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FavoriteSong.toDto(): FavoriteDto = when (val s = subject) {
  is FavoriteSubject.BookedSong -> FavoriteDto.BookedSong(
    songNumberId = s.songNumberId.toDto(),
    songNumber = songNumber ?: 0,
    bookDisplayName = bookDisplayName ?: "",
    songName = songName,
    addedAt = addedAt
  )

  is FavoriteSubject.StandaloneSong -> FavoriteDto.StandaloneSong(
    songId = SongIdDto(s.songId.value),
    songName = songName,
    addedAt = addedAt
  )
}

fun FavoriteSubject.toDto(): FavoriteSubjectDto = when (this) {
  is FavoriteSubject.BookedSong -> FavoriteSubjectDto.BookedSong(songNumberId.toDto())
  is FavoriteSubject.StandaloneSong -> FavoriteSubjectDto.StandaloneSong(SongIdDto(songId.value))
}
