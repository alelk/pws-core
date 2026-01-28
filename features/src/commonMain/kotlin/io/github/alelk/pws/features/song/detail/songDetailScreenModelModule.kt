package io.github.alelk.pws.features.song.detail

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import org.koin.dsl.module

import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase

val songDetailScreenModelModule = module {
  factory { (songNumberId: SongNumberId) -> SongDetailScreenModel(songNumberId, observeSong = get<ObserveSongUseCase>(), recordSongView = get<RecordSongViewUseCase>()) }
}
