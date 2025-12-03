package io.github.alelk.pws.features.books

import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import org.koin.dsl.module

val booksScreenModelModule = module {
  factory { BooksScreenModel(observeBooks = get<ObserveBooksUseCase>()) }
}
