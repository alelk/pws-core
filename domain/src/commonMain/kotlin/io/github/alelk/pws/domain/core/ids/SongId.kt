package io.github.alelk.pws.domain.core.ids

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.InvalidInputError
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = SongIdSerializer::class)
value class SongId(val value: Long): Comparable<SongId> {
  init {
    require(value >= 0) { "song id must not be negative" }
  }

  override fun toString(): String = value.toString()

  override fun compareTo(other: SongId): Int = this.value.compareTo(other.value)

  companion object {
    fun parse(string: String): SongId =
      parseValidated(string).fold(
        ifLeft = { error -> throw IllegalArgumentException(error.message) },
        ifRight = { it }
      )

    fun parseValidated(string: String): Either<InvalidInputError, SongId> {
      val id = string.toLongOrNull()
        ?: return InvalidInputError("songId", "song id must be a non-negative long number: '$string'").left()
      return if (id >= 0) {
        SongId(id).right()
      } else {
        InvalidInputError("songId", "song id must be non-negative: '$string'").left()
      }
    }
  }
}