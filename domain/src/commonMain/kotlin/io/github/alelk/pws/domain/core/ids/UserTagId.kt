package io.github.alelk.pws.domain.core.ids

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.InvalidInputError
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * Composite identifier for user-created tags.
 * Format: `userId/customTagId` where customTagId is [TagId.Custom.identifier]
 *
 * Maximum length: 64 (userId) + 1 (separator) + 12 (custom-XXXXX) = 77 characters
 */
@Serializable(with = UserTagIdSerializer::class)
@JvmInline
value class UserTagId(val value: String) {

  val userId: UserId get() = UserId(value.substringBefore(SEPARATOR))
  val customTagId: TagId.Custom get() = TagId.Custom.parse(value.substringAfter(SEPARATOR))

  init {
    require(value.contains(SEPARATOR)) { "UserTagId must contain '$SEPARATOR' separator" }
    require(value.length <= MAX_LENGTH) { "UserTagId length must be <= $MAX_LENGTH, but was ${value.length}" }
    // Validate parts
    val parts = value.split(SEPARATOR, limit = 2)
    require(parts.size == 2) { "UserTagId must have exactly 2 parts separated by '$SEPARATOR'" }
    require(parts[0].isNotBlank()) { "UserId part must not be blank" }
    require(parts[1].startsWith(TagId.Custom.PREFIX)) { "Tag part must be a custom tag id starting with '${TagId.Custom.PREFIX}'" }
  }

  constructor(userId: UserId, customTagId: TagId.Custom) : this("${userId.value}$SEPARATOR${customTagId.identifier}")

  override fun toString(): String = value

  companion object {
    const val SEPARATOR = "/"
    const val MAX_LENGTH = 77 // 64 (userId) + 1 (/) + 12 (custom-XXXXX)

    fun parse(value: String): UserTagId = parseValidated(value).fold(
      ifLeft = { error -> throw IllegalArgumentException(error.message) },
      ifRight = { it }
    )

    fun parseValidated(value: String): Either<InvalidInputError, UserTagId> {
      if (!value.contains(SEPARATOR)) {
        return InvalidInputError("userTagId", "UserTagId must contain '$SEPARATOR' separator").left()
      }
      if (value.length > MAX_LENGTH) {
        return InvalidInputError("userTagId", "UserTagId length must be <= $MAX_LENGTH, but was ${value.length}").left()
      }
      val parts = value.split(SEPARATOR, limit = 2)
      if (parts.size != 2 || parts[0].isBlank()) {
        return InvalidInputError("userTagId", "UserTagId must have exactly 2 parts separated by '$SEPARATOR' with non-blank user part").left()
      }
      TagId.Custom.parseValidated(parts[1]).fold(
        ifLeft = { return InvalidInputError("userTagId", "Tag part must be a valid custom tag id").left() },
        ifRight = { }
      )
      return UserTagId(value).right()
    }
  }
}

object UserTagIdSerializer : KSerializer<UserTagId> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserTagId", PrimitiveKind.STRING)
  override fun serialize(encoder: Encoder, value: UserTagId) = encoder.encodeString(value.value)
  override fun deserialize(decoder: Decoder): UserTagId = UserTagId.parse(decoder.decodeString())
}
