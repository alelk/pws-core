package io.github.alelk.pws.features.song.detail

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.usecase.GetSongReferencesWithDetailsUseCase
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.songtag.usecase.ObserveTagsForSongUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import org.koin.dsl.module

val songDetailScreenModelModule = module {

  factory { (songNumberId: SongNumberId) ->
    SongDetailScreenModel(
      songNumberId = songNumberId,
      observeSong = get<ObserveSongUseCase>(),
      songObserveRepository = get<SongObserveRepository>(),
      recordSongView = get<RecordSongViewUseCase>(),
      observeIsFavorite = get<ObserveIsFavoriteUseCase>(),
      toggleFavorite = get<ToggleFavoriteUseCase>(),
      getSongReferences = GetSongReferencesWithDetailsUseCase(
        get<SongReferenceReadRepository>(),
        get<SongReadRepository>(),
        get()
      ),
      observeTagsForSong = ObserveTagsForSongUseCase(get<SongTagObserveRepository<TagId>>()),
      observeAllTags = ObserveTagsUseCase(get<TagObserveRepository<TagId>>()),
      replaceAllSongTags = ReplaceAllSongTagsUseCase(
        get<SongTagReadRepository<TagId>>(),
        get<SongTagWriteRepository<TagId>>(),
        get()
      ),
    )
  }

  // SongId is a @JvmInline value class — accept Long on Kotlin/JS to avoid unboxing issues
  factory { (songIdRaw: Long) ->
    SongDetailBySongIdScreenModel(
      songId = SongId(songIdRaw),
      observeSong = get<ObserveSongUseCase>()
    )
  }
}
