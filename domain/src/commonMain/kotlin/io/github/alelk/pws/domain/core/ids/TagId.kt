package io.github.alelk.pws.domain.core.ids

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.random.Random

@Serializable(with = TagIdSerializer::class)
sealed interface TagId : Comparable<TagId> {

  val identifier: String
  val predefined: Boolean get() = this is Predefined

  override fun compareTo(other: TagId): Int = this.identifier.compareTo(other.identifier)

  @Serializable
  @JvmInline
  value class Predefined(override val identifier: String) : TagId {
    init {
      require(isValid(identifier)) {
        "predefined tag id should contain only letters, digits and '-', '_' symbols; should not start with digit; should not end with '-' or '_'"
      }
      require(identifier.startsWith(Custom.PREFIX).not()) { "predefined tag id should not start with '${Custom.PREFIX}'" }
      require(identifier.length < 64) { "predefined tag id length should be less than 64 characters: '$identifier'" }
    }

    companion object {

      /** Fast no-regex validation — avoids JNI/ICU overhead on hot paths. */
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

      fun parse(identifier: String): Predefined = Predefined(identifier)
    }

    override fun toString(): String = identifier
  }

  @JvmInline
  @Serializable
  value class Custom(override val identifier: String) : TagId {

    constructor(number: Int) : this("$PREFIX${number.toString().padStart(5, '0')}")

    init {
      require(identifier.startsWith(PREFIX)) { "custom tag id should start with '$PREFIX'" }
      require(identifier.drop(PREFIX.length).all { it.isDigit() }) { "custom tag id should contain only digits after '$PREFIX' prefix" }
      require(identifier.length == PREFIX.length + 5) { "custom tag id should be exactly ${PREFIX.length + 5} characters long" }
      require(number > 0) { "custom tag number should be greater than 0" }
    }

    val number: Int get() = identifier.drop(PREFIX.length).toInt()

    companion object {
      const val PREFIX = "custom-"
      fun parse(identifier: String): Custom = Custom(identifier)
      fun random() = Custom(Random.nextInt(1, 99_999))
    }

    override fun toString(): String = identifier
  }

  companion object {
    fun parse(identifier: String): TagId = if (identifier.startsWith(Custom.PREFIX)) Custom.parse(identifier) else Predefined.parse(identifier)
  }

  operator fun invoke(identifier: String): TagId = parse(identifier)
}


fun String.toTagId(): TagId = TagId.parse(this)

fun TagId.Custom.next(): TagId.Custom = TagId.Custom(number + 1)