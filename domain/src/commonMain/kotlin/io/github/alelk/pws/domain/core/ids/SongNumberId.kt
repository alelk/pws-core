package io.github.alelk.pws.domain.core.ids

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.InvalidInputError
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@JvmInline
@Serializable(with = SongNumberIdSerializer::class)
value class SongNumberId private constructor(val identifier: String) {

  constructor(bookId: BookId, songId: SongId) : this("$bookId/$songId")

  val bookId: BookId get() = BookId.parse(identifier.substringBefore('/'))
  val songId: SongId get() = SongId(identifier.substringAfter('/').toLong())

  override fun toString(): String = identifier

  companion object {
    fun parse(string: String): SongNumberId =
      parseValidated(string).fold(
        ifLeft = { error -> throw IllegalArgumentException(error.message) },
        ifRight = { it }
      )

    fun parseValidated(string: String): Either<InvalidInputError, SongNumberId> {
      val parts = string.split('/', limit = 2)
      if (parts.size != 2) {
        return InvalidInputError("songNumberId", "unable to parse song number id from string '$string': expected format 'bookId/songId'").left()
      }
      val bookId = BookId.parseValidated(parts[0]).fold(
        ifLeft = { return InvalidInputError("songNumberId", "invalid bookId in '$string': ${it.message}").left() },
        ifRight = { it }
      )
      val songId = SongId.parseValidated(parts[1]).fold(
        ifLeft = { return InvalidInputError("songNumberId", "invalid songId in '$string': ${it.message}").left() },
        ifRight = { it }
      )
      return SongNumberId(bookId, songId).right()
    }
  }
}