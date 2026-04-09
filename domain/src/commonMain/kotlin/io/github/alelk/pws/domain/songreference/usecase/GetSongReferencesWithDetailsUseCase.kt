package io.github.alelk.pws.domain.songreference.usecase

import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository

/**
 * Enriched view of a song reference — reference metadata + basic song info.
 */
data class SongReferenceDetail(
  val refSongId: SongId,
  val songName: String,
  val reason: SongRefReason,
  val volume: Int,
)

/**
 * Use case: get all outgoing references for a song, enriched with song name.
 * Returns only references where the referenced song exists in the repository.
 */
class GetSongReferencesWithDetailsUseCase(
  private val referenceRepository: SongReferenceReadRepository,
  private val songRepository: SongReadRepository,
  private val txRunner: TransactionRunner,
) {
  suspend operator fun invoke(songId: SongId): List<SongReferenceDetail> =
    txRunner.inRoTransaction {
      val refs = referenceRepository.getReferencesForSong(songId) +
        referenceRepository.getReferencesToSong(songId)
      refs
        .distinctBy { it.refSongId.takeIf { id -> id != songId } ?: it.songId }
        .mapNotNull { ref ->
          val targetSongId = if (ref.songId == songId) ref.refSongId else ref.songId
          val summary: SongSummary? = songRepository.getMany()
            .firstOrNull { it.id == targetSongId }
          summary?.let {
            SongReferenceDetail(
              refSongId = targetSongId,
              songName = it.name.value,
              reason = ref.reason,
              volume = ref.volume,
            )
          }
        }
        .sortedByDescending { it.volume }
    }
}

