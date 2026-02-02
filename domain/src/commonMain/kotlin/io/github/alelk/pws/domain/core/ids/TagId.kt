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
      require(pattern.matches(identifier)) {
        "predefined tag id should contain only letters, digits and '-', '_' symbols; should not start with digit; should not end with '-' or '_'"
      }
      require(identifier.startsWith(Custom.PREFIX).not()) { "predefined tag id should not start with '${Custom.PREFIX}'" }
      require(identifier.length < 24) { "predefined tag id length should be less than 24 characters" }
    }

    companion object {
      val pattern = Regex("""^\p{L}+([\p{L}\d_-]*[\p{L}\d])?$""")

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

    val number: Int get() = identifier.drop(PREFIX.length).dropWhile { it == '0' }.toInt()

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