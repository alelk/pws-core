package io.github.alelk.pws.api.contract.book

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.Serializable

@Serializable
data class BookSummaryDto(
  val id: BookIdDto,
  val version: VersionDto,
  val locale: LocaleDto,
  val name: String,
  val displayShortName: String,
  val displayName: String,
  val countSongs: Int,
  val firstSongNumberId: SongNumberIdDto,
  val enabled: Boolean,
  val priority: Int,
)