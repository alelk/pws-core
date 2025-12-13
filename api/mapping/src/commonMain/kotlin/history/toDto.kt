package io.github.alelk.pws.api.mapping.history

import io.github.alelk.pws.api.contract.history.HistoryEntryDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.history.model.HistoryEntry
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun HistoryEntry.toDto(): HistoryEntryDto = HistoryEntryDto(
  songNumberId = songNumberId.toDto(),
  viewedAt = viewedAt.toEpochMilliseconds()
)

