package io.github.alelk.pws.features.song.detail

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import io.github.alelk.pws.domain.songreference.usecase.GetSongReferencesWithDetailsUseCase
import org.koin.dsl.module

val songDetailScreenModelModule = module {
  factory { GetSongReferencesWithDetailsUseCase(get(), get(), get()) }
  factory { (songNumberId: SongNumberId) ->
    SongDetailScreenModel(
      songNumberId = songNumberId,
      observeSong = get<ObserveSongUseCase>(),
      songObserveRepository = get<SongObserveRepository>(),
      recordSongView = get<RecordSongViewUseCase>(),
      observeIsFavorite = get<ObserveIsFavoriteUseCase>(),
      toggleFavorite = get<ToggleFavoriteUseCase>(),
      getSongReferences = get<GetSongReferencesWithDetailsUseCase>()
    )
  }
  factory { (songId: SongId) ->
    SongDetailBySongIdScreenModel(
      songId = songId,
      observeSong = get<ObserveSongUseCase>()
    )
  }
}
