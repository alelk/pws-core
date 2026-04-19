package io.github.alelk.pws.api.client.repository

import arrow.core.Either
import io.github.alelk.pws.api.client.api.AdminSongApi
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.song.toRequestDto
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.repository.SongWriteRepository

class RemoteSongWriteRepository(private val api: AdminSongApi) : SongWriteRepository {

  override suspend fun create(command: CreateSongCommand): Either<CreateError, SongId> =
    api.create(command.toRequestDto()).map { command.id }

  override suspend fun update(command: UpdateSongCommand): Either<UpdateError, SongId> =
    api.update(command.id.toDto(), command.toRequestDto()).map { command.id }

  override suspend fun delete(id: SongId): Either<DeleteError, SongId> =
    api.delete(id.toDto()).map { id }
}
