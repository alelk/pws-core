package io.github.alelk.pws.api.client.api

sealed interface ResourceBatchCreateResult<out ID: Any> {
  data class Success<ID: Any>(val resources: List<ID>) : ResourceBatchCreateResult<ID>
  data class AlreadyExists<ID: Any>(val resources: List<ID>) : ResourceBatchCreateResult<Nothing>
  data class ValidationError(val message: String) : ResourceBatchCreateResult<Nothing>
}