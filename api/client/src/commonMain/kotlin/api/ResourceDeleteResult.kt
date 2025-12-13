package io.github.alelk.pws.api.client.api

/**
 * Result of a resource delete operation.
 */
sealed interface ResourceDeleteResult<out ID> {
  data class Success<ID>(val resourceId: ID) : ResourceDeleteResult<ID>
  data class NotFound<ID>(val resourceId: ID) : ResourceDeleteResult<ID>
  data class ValidationError(val message: String) : ResourceDeleteResult<Nothing>
}

