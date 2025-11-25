package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.client.error.ApiException
import io.github.alelk.pws.api.client.error.handleResponse
import io.github.alelk.pws.api.client.http.JsonProvider
import io.github.alelk.pws.api.contract.core.error.ErrorCodes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

internal abstract class BaseResourceApi(protected val client: HttpClient) {
  protected val json: Json get() = JsonProvider.instance

  protected suspend inline fun <reified T : Any> executeGet(block: suspend () -> HttpResponse): Result<T?> =
    handleResponse(json) { block() }
      .mapCatching { response ->
        if (response.status == HttpStatusCode.NotFound) null
        else execute<T> { response }.getOrThrow()
      }

  protected suspend inline fun <reified T, ID> executeCreate(resourceId: ID, block: suspend () -> HttpResponse): Result<ResourceCreateResult<ID>> =
    handleResponse(json) { block() }
      .mapCatching {
        try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
        ResourceCreateResult.Success(resourceId)
      }.recoverCatching { exc ->
        if (exc is ApiException.Server) {
          when (exc.error?.code) {
            ErrorCodes.ALREADY_EXISTS -> ResourceCreateResult.AlreadyExists(resourceId)
            ErrorCodes.VALIDATION_ERROR -> ResourceCreateResult.ValidationError(exc.error.message)
            else -> throw exc
          }
        } else throw exc
      }

  protected suspend inline fun <reified T, ID> executeUpdate(resourceId: ID, block: suspend () -> HttpResponse): Result<ResourceUpdateResult<ID>> =
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

  protected suspend inline fun <reified T> execute(block: suspend () -> HttpResponse): Result<T> =
    handleResponse(json) { block() }
      .mapCatching {
        try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
      }
}

