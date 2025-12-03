package io.github.alelk.pws.features.book.songs

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.cross.usecase.ObserveBookWithSongsUseCase
import org.koin.dsl.module

val bookSongsScreenModelModule = module {
  factory { (bookId: BookId) -> BookSongsScreenModel(bookId, observeBookWithSongs = get<ObserveBookWithSongsUseCase>()) }
}
