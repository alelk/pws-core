package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.AdminSongApi
import io.github.alelk.pws.api.client.api.ResourceCreateResult
import io.github.alelk.pws.api.client.api.ResourceDeleteResult
import io.github.alelk.pws.api.client.api.ResourceUpdateResult
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.song.toRequestDto
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.repository.SongWriteRepository

class RemoteSongWriteRepository(private val api: AdminSongApi) : SongWriteRepository {

  override suspend fun create(command: CreateSongCommand): CreateResourceResult<SongId> =
    runCatching {
      when (val result = api.create(command.toRequestDto())) {
        is ResourceCreateResult.AlreadyExists<*> -> CreateResourceResult.AlreadyExists(command.id)
        is ResourceCreateResult.Success<*> -> CreateResourceResult.Success(command.id)
        is ResourceCreateResult.ValidationError -> CreateResourceResult.ValidationError(command.id, result.message)
      }
    }.getOrElse { exc -> CreateResourceResult.UnknownError(command.id, exc) }

  override suspend fun update(command: UpdateSongCommand): UpdateResourceResult<SongId> =
    runCatching {
      when (val result = api.update(command.id.toDto(), command.toRequestDto())) {
        is ResourceUpdateResult.NotFound<*> -> UpdateResourceResult.NotFound(command.id)
        is ResourceUpdateResult.Success<*> -> UpdateResourceResult.Success(command.id)
        is ResourceUpdateResult.ValidationError -> UpdateResourceResult.ValidationError(command.id, result.message)
      }
    }.getOrElse { exc -> UpdateResourceResult.UnknownError(command.id, exc) }

  override suspend fun delete(id: SongId): DeleteResourceResult<SongId> =
    runCatching {
      when (val result = api.delete(id.toDto())) {
        is ResourceDeleteResult.Success<*> -> DeleteResourceResult.Success(id)
        is ResourceDeleteResult.NotFound<*> -> DeleteResourceResult.NotFound(id)
        is ResourceDeleteResult.ValidationError -> DeleteResourceResult.ValidationError(id, result.message)
      }
    }.getOrElse { exc -> DeleteResourceResult.UnknownError(id, exc) }
}
