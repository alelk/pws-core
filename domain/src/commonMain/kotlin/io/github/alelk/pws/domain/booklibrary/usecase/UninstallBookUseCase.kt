package io.github.alelk.pws.domain.booklibrary.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.BookId

interface UninstallBookUseCase {
    suspend operator fun invoke(bookId: BookId): Either<DeleteError, Unit>
}
