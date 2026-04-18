package io.github.alelk.pws.domain.core.ids

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = BookIdSerializer::class)
value class BookId private constructor(val identifier: String) : Comparable<BookId> {

  init {
    require(isValid(identifier)) {
      "book id should contain only letters, digits and '-', '_' symbols; should not start with digit; should not end with '-' or '_', " +
        "but provided '$identifier'"
    }
  }

  companion object {

    /** Fast no-regex validation — avoids JNI/ICU overhead on hot paths (e.g. main thread). */
    fun isValid(s: String): Boolean {
      if (s.isEmpty()) return false
      if (!s[0].isAsciiLetter()) return false
      if (s.length == 1) return true
      if (!s[s.length - 1].isAsciiLetterOrDigit()) return false
      for (i in 1 until s.length - 1) {
        val c = s[i]
        if (!c.isAsciiLetterOrDigit() && c != '-' && c != '_') return false
      }
      return true
    }

    private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'
    private fun Char.isAsciiLetterOrDigit() = isAsciiLetter() || this in '0'..'9'

    fun parse(identifier: String): BookId = BookId(identifier)
  }

  override fun toString(): String = identifier

  override fun compareTo(other: BookId): Int = identifier.compareTo(other.identifier)
}