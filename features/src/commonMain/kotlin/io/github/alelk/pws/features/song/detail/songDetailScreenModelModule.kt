package io.github.alelk.pws.features.song.detail

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import org.koin.dsl.module

val songDetailScreenModelModule = module {
  factory { (songNumberId: SongNumberId) ->
    SongDetailScreenModel(
      songNumberId = songNumberId,
      observeSong = get<ObserveSongUseCase>(),
      recordSongView = get<RecordSongViewUseCase>(),
      observeIsFavorite = get<ObserveIsFavoriteUseCase>(),
      toggleFavorite = get<ToggleFavoriteUseCase>()
    )
  }
}
