package io.github.alelk.pws.portable.model

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongIdSerializer
import kotlinx.serialization.Serializable

/**
 * Cross-reference between two songs.
 * Mirrors [io.github.alelk.pws.database.song_reference.SongReferenceEntity].
 *
 * Used in [CollectionBundle] and [BookBundle].
 */
@Serializable
data class SongReference(
  @Serializable(with = SongIdSerializer::class)
  val songId: SongId,
  @Serializable(with = SongIdSerializer::class)
  val refSongId: SongId,
  val reason: String,  // e.g. "variation"
  val volume: Int,     // similarity degree 1-100
) {
  init {
    require(songId != refSongId) { "song should not reference itself: $songId" }
    require(volume in 1..100) { "volume must be in 1..100, was $volume" }
  }
}

