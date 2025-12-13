package io.github.alelk.pws.api.mapping.favorite

import io.github.alelk.pws.api.contract.favorite.FavoriteDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.favorite.model.Favorite
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Favorite.toDto(): FavoriteDto = FavoriteDto(
  songNumberId = songNumberId.toDto(),
  addedAt = addedAt.toEpochMilliseconds()
)

