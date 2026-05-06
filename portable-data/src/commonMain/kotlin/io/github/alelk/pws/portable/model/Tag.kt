package io.github.alelk.pws.portable.model

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.TagIdSerializer
import io.github.alelk.pws.domain.core.Color
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ConsistentCopyVisibility
@Serializable
data class Tag @OptIn(ExperimentalSerializationApi::class) private constructor(
  val name: String,
  val color: Color,
  @SerialName("songs")
  private val songNumbers: Map<BookId, List<Numbers>>,
  // Fields added in portable-data v2 — absent in old Backup YAML, use defaults.
  // @EncodeDefault(NEVER) prevents them from appearing in output when default.
  @EncodeDefault(EncodeDefault.Mode.NEVER)
  @Serializable(with = TagIdSerializer::class)
  val id: TagId? = null,
  @EncodeDefault(EncodeDefault.Mode.NEVER)
  val priority: Int = 0,
  @EncodeDefault(EncodeDefault.Mode.NEVER)
  val predefined: Boolean = false,
) {
  init {
    require(name.isNotBlank()) { "tag name should not be blank" }
  }

  val songs: Set<SongNumber> get() = songNumbers.flatMap { (bookId, numbers) -> numbers.flatMap { it.get() }.map { SongNumber(bookId, it) } }.toSet()

  /** Constructor for user backup tags (no id). */
  constructor(name: String, color: Color, songs: Set<SongNumber>) : this(
    name = name,
    color = color,
    songNumbers = songs.groupBy { it.bookId }.map { (bookId, numbers) -> bookId to Numbers(numbers.map { it.number }) }.toMap(),
  )

  /** Constructor for predefined/content-bundle tags (with id). */
  constructor(id: TagId, name: String, color: Color, priority: Int = 0, predefined: Boolean = true, songs: Set<SongNumber>) : this(
    name = name,
    color = color,
    songNumbers = songs.groupBy { it.bookId }.map { (bookId, numbers) -> bookId to Numbers(numbers.map { it.number }) }.toMap(),
    id = id,
    priority = priority,
    predefined = predefined,
  )
}

