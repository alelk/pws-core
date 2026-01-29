package io.github.alelk.pws.api.client.api

/**
 * Result of a resource upsert operation (create or update).
 */
sealed interface ResourceUpsertResult<out T> {
  data class Success<T>(val resource: T) : ResourceUpsertResult<T>
  data class ValidationError(val message: String) : ResourceUpsertResult<Nothing>
}
