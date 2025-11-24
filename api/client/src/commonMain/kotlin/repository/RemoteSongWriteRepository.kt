package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.repository.SongWriteRepository

class RemoteSongWriteRepository(private val api: SongApi) : SongWriteRepository {

  override suspend fun create(command: CreateSongCommand) {
    api.create(command.toDto())
  }

  override suspend fun update(command: UpdateSongCommand): Boolean {
    throw UnsupportedOperationException("Remote song update is not implemented")
  }

  override suspend fun delete(id: io.github.alelk.pws.domain.core.ids.SongId): Boolean {
    throw UnsupportedOperationException("Remote song delete is not implemented")
  }
}

