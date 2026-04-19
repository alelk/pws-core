package io.github.alelk.pws.domain.songnumber.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink

/**
 * Many-to-many association between Book and Song with an extra ordinal (song number) within the book.
 * Invariants per book:
 *  - (bookId, songId) is unique.
 *  - (bookId, number) is unique.
 *  - number > 0 (1-based indexing).
 */
interface SongNumberWriteRepository {
  /** Link a song to a book with a specific number. */
  suspend fun create(bookId: BookId, link: SongNumberLink): Either<CreateError, SongNumberLink>

  /** Change the song's number within the book. */
  suspend fun update(bookId: BookId, link: SongNumberLink): Either<UpdateError, SongNumberLink>

  /** Remove association. */
  suspend fun delete(bookId: BookId, songId: SongId): Either<DeleteError, SongNumberId>
}