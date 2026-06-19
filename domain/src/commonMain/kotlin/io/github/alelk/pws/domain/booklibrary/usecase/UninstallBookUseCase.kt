package io.github.alelk.pws.domain.booklibrary.usecase

import io.github.alelk.pws.domain.core.ids.BookId

interface UninstallBookUseCase {
    suspend operator fun invoke(bookId: BookId)
}
