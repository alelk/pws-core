package io.github.alelk.pws.domain.bookstatistic.command

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.InvalidInputError
import io.github.alelk.pws.domain.core.ids.BookId

/** Patch-like update for BookStatisticDetail. Any non-null field will be applied. */
data class UpdateBookStatisticCommand(
    val id: BookId,
    val priority: Int? = null,
    val readings: Int? = null,
    val rating: Int? = null,
) {
  init {
    if (priority != null) require(priority >= 0) { "priority must be >= 0" }
    if (readings != null) require(readings >= 0) { "readings must be >= 0" }
  }

  fun isEmpty(): Boolean = priority == null && readings == null && rating == null

  companion object {
    fun validated(
      id: BookId,
      priority: Int? = null,
      readings: Int? = null,
      rating: Int? = null,
    ): Either<InvalidInputError, UpdateBookStatisticCommand> {
      if (priority != null && priority < 0) {
        return InvalidInputError("bookStatistic.priority", "priority must be >= 0").left()
      }
      if (readings != null && readings < 0) {
        return InvalidInputError("bookStatistic.readings", "readings must be >= 0").left()
      }
      return UpdateBookStatisticCommand(id = id, priority = priority, readings = readings, rating = rating).right()
    }
  }
}