package io.github.alelk.pws.domain.core.ids

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

    fun parse(value: String): UserTagId = UserTagId(value)
  }
}

object UserTagIdSerializer : KSerializer<UserTagId> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserTagId", PrimitiveKind.STRING)
  override fun serialize(encoder: Encoder, value: UserTagId) = encoder.encodeString(value.value)
  override fun deserialize(decoder: Decoder): UserTagId = UserTagId.parse(decoder.decodeString())
}
