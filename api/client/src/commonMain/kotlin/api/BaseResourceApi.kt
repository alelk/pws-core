package io.github.alelk.pws.api.client.api

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import io.github.alelk.pws.api.client.error.ApiException
import io.github.alelk.pws.api.client.error.handleResponse
import io.github.alelk.pws.api.client.http.JsonProvider
import io.github.alelk.pws.api.contract.core.error.ErrorCodes
import io.github.alelk.pws.api.contract.core.error.asResourcesNotFound
import io.github.alelk.pws.domain.core.error.BulkCreateError
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.error.UpsertError
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

  protected suspend inline fun <reified T, ID> executeCreate(
    resource: ID,
    crossinline block: suspend () -> HttpResponse
  ): Either<CreateError, ID> = either {
    val httpResponse: HttpResponse = catch({
      handleResponse(json) { block() }.getOrThrow()
    }) { exc ->
      raise(
        when {
          exc is ApiException.Server && exc.error?.code == ErrorCodes.ALREADY_EXISTS ->
            CreateError.AlreadyExists()
          exc is ApiException.Server && exc.error?.code == ErrorCodes.VALIDATION_ERROR ->
            CreateError.ValidationError(exc.error.message)
          else -> CreateError.UnknownError(exc)
        }
      )
    }
    catch({ httpResponse.body<T>() }) { exc ->
      raise(CreateError.UnknownError(ApiException.Serialization(exc)))
    }
    resource
  }

  protected suspend inline fun <reified T, ID : Any> executeBatchCreate(
    resources: List<ID>,
    crossinline resourceIdParser: (resourceId: String) -> ID,
    crossinline block: suspend () -> HttpResponse
  ): Either<BulkCreateError<ID>, List<ID>> = either {
    val httpResponse: HttpResponse = catch({
      handleResponse(json) { block() }.getOrThrow()
    }) { exc ->
      raise(
        when {
          exc is ApiException.Server && exc.error?.code == ErrorCodes.ALREADY_EXISTS -> {
            val error = exc.error.asResourcesNotFound()
            val resourceIds = error.resourceIds.map { resourceIdParser(it) }
            BulkCreateError.AlreadyExists(resourceIds)
          }
          exc is ApiException.Server && exc.error?.code == ErrorCodes.VALIDATION_ERROR ->
            BulkCreateError.ValidationError(exc.error.message)
          else -> BulkCreateError.UnknownError(exc)
        }
      )
    }
    catch({ httpResponse.body<T>() }) { exc ->
      raise(BulkCreateError.UnknownError(ApiException.Serialization(exc)))
    }
    resources
  }

  protected suspend inline fun <reified T, ID> executeUpdate(
    resourceId: ID,
    crossinline block: suspend () -> HttpResponse
  ): Either<UpdateError, ID> = either {
    val httpResponse: HttpResponse = catch({
      handleResponse(json) { block() }.getOrThrow()
    }) { exc ->
      raise(
        when {
          exc is ApiException.Server && exc.error?.code == ErrorCodes.RESOURCE_NOT_FOUND ->
            UpdateError.NotFound
          exc is ApiException.Server && exc.error?.code == ErrorCodes.VALIDATION_ERROR ->
            UpdateError.ValidationError(exc.error.message)
          else -> UpdateError.UnknownError(exc)
        }
      )
    }
    catch({ httpResponse.body<T>() }) { exc ->
      raise(UpdateError.UnknownError(ApiException.Serialization(exc)))
    }
    resourceId
  }

  protected suspend inline fun <reified T : Any> executeUpsert(
    crossinline block: suspend () -> HttpResponse
  ): Either<UpsertError, T> = either {
    val httpResponse: HttpResponse = catch({
      handleResponse(json) { block() }.getOrThrow()
    }) { exc ->
      raise(
        when {
          exc is ApiException.Server && exc.error?.code == ErrorCodes.VALIDATION_ERROR ->
            UpsertError.ValidationError(exc.error.message)
          else -> UpsertError.UnknownError(exc)
        }
      )
    }
    catch({ httpResponse.body<T>() }) { exc ->
      raise(UpsertError.UnknownError(ApiException.Serialization(exc)))
    }
  }

  protected suspend inline fun <ID> executeDelete(
    resourceId: ID,
    crossinline block: suspend () -> HttpResponse
  ): Either<DeleteError, ID> = either {
    catch({
      handleResponse(json) { block() }.getOrThrow()
    }) { exc ->
      raise(
        when {
          exc is ApiException.Server && exc.error?.code == ErrorCodes.RESOURCE_NOT_FOUND ->
            DeleteError.NotFound
          exc is ApiException.Server && exc.error?.code == ErrorCodes.VALIDATION_ERROR ->
            DeleteError.ValidationError(exc.error.message)
          else -> DeleteError.UnknownError(exc)
        }
      )
    }
    resourceId
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
