package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.client.error.ApiException
import io.github.alelk.pws.api.client.error.handleResponse
import io.github.alelk.pws.api.client.http.JsonProvider
import io.github.alelk.pws.api.contract.core.error.ErrorCodes
import io.github.alelk.pws.api.contract.core.error.asResourcesNotFound
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

internal abstract class BaseResourceApi(protected val client: HttpClient) {
  protected val json: Json get() = JsonProvider.instance

  protected suspend inline fun <reified T : Any> executeGet(crossinline block: suspend () -> HttpResponse): Result<T?> =
    handleResponse(json) {
      block()
    }.recoverCatching { exc ->
      if (exc is ApiException.Server) {
        when (exc.error?.code) {
          ErrorCodes.RESOURCE_NOT_FOUND -> null
          else -> throw exc
        }
      } else throw exc
    }.mapCatching { response ->
      response?.let { execute<T> { it } }?.getOrThrow()
    }

  protected suspend inline fun <reified T, ID> executeCreate(resource: ID, crossinline block: suspend () -> HttpResponse): Result<ResourceCreateResult<ID>> =
    handleResponse(json) { block() }
      .mapCatching {
        try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
        ResourceCreateResult.Success(resource)
      }.recoverCatching { exc ->
        if (exc is ApiException.Server) {
          when (exc.error?.code) {
            ErrorCodes.ALREADY_EXISTS -> ResourceCreateResult.AlreadyExists(resource)
            ErrorCodes.VALIDATION_ERROR -> ResourceCreateResult.ValidationError(exc.error.message)
            else -> throw exc
          }
        } else throw exc
      }

  protected suspend inline fun <reified T, ID : Any> executeBatchCreate(
    resources: List<ID>,
    crossinline resourceIdParser: (resourceId: String) -> ID,
    crossinline block: suspend () -> HttpResponse
  ): Result<ResourceBatchCreateResult<ID>> =
    handleResponse(json) { block() }
      .mapCatching {
        try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
        ResourceBatchCreateResult.Success(resources)
      }.recoverCatching { exc ->
        if (exc is ApiException.Server) {
          when (exc.error?.code) {
            ErrorCodes.ALREADY_EXISTS -> {
              val error = exc.error.asResourcesNotFound()
              val resourceIds = error.resourceIds.map { resourceIdParser(it) }
              ResourceBatchCreateResult.AlreadyExists(resourceIds)
            }

            ErrorCodes.VALIDATION_ERROR -> ResourceBatchCreateResult.ValidationError(exc.error.message)
            else -> throw exc
          }
        } else throw exc
      }

  protected suspend inline fun <reified T, ID> executeUpdate(resourceId: ID, crossinline block: suspend () -> HttpResponse): Result<ResourceUpdateResult<ID>> =
    handleResponse(json) { block() }
      .mapCatching {
        try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
        ResourceUpdateResult.Success(resourceId)
      }.recoverCatching { exc ->
        if (exc is ApiException.Server) {
          when (exc.error?.code) {
            ErrorCodes.RESOURCE_NOT_FOUND -> ResourceUpdateResult.NotFound(resourceId)
            ErrorCodes.VALIDATION_ERROR -> ResourceUpdateResult.ValidationError(exc.error.message)
            else -> throw exc
          }
        } else throw exc
      }

  protected suspend inline fun <reified T : Any> executeUpsert(crossinline block: suspend () -> HttpResponse): Result<ResourceUpsertResult<T>> =
    handleResponse(json) { block() }
      .mapCatching {
        val resource = try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
        ResourceUpsertResult.Success(resource)
      }.recoverCatching { exc ->
        if (exc is ApiException.Server) {
          when (exc.error?.code) {
            ErrorCodes.VALIDATION_ERROR -> ResourceUpsertResult.ValidationError(exc.error.message)
            else -> throw exc
          }
        } else throw exc
      }

  protected suspend inline fun <ID> executeDelete(resourceId: ID, crossinline block: suspend () -> HttpResponse): Result<ResourceDeleteResult<ID>> =
    handleResponse(json) { block() }
      .mapCatching {
        ResourceDeleteResult.Success(resourceId)
      }.recoverCatching { exc ->
        if (exc is ApiException.Server) {
          when (exc.error?.code) {
            ErrorCodes.RESOURCE_NOT_FOUND -> ResourceDeleteResult.NotFound(resourceId)
            ErrorCodes.VALIDATION_ERROR -> ResourceDeleteResult.ValidationError(exc.error.message)
            else -> throw exc
          }
        } else throw exc
      }

  protected suspend inline fun <reified T> execute(crossinline block: suspend () -> HttpResponse): Result<T> =
    handleResponse(json) { block() }
      .mapCatching {
        try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
      }
}

