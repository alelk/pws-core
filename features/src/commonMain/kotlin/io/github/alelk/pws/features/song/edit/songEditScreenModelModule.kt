package io.github.alelk.pws.features.song.edit

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.song.usecase.GetSongDetailUseCase
import io.github.alelk.pws.domain.song.usecase.UpdateSongUseCase
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.songtag.usecase.GetSongTagIdsUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import org.koin.dsl.module

val songEditScreenModelModule = module {
  factory { (songId: SongId) ->
    SongEditScreenModel(
      songId = songId,
      getSongDetailUseCase = get<GetSongDetailUseCase>(),
      updateSongUseCase = get<UpdateSongUseCase>(),
      observeTagsUseCase = ObserveTagsUseCase(get<TagObserveRepository<TagId>>()),
      getSongTagIdsUseCase = GetSongTagIdsUseCase(get<SongTagReadRepository<TagId>>(), get()),
      replaceAllSongTagsUseCase = ReplaceAllSongTagsUseCase(
        get<SongTagReadRepository<TagId>>(),
        get<SongTagWriteRepository<TagId>>(),
        get()
      ),
    )
  }
}
