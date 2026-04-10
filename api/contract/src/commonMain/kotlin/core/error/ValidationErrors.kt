package io.github.alelk.pws.api.contract.core.error

import kotlinx.serialization.Serializable

/**
 * Structured validation error for API boundary validation (format, presence, length).
 * Returned from `toDomainCommandValidated()` mapping functions instead of throwing exceptions.
 *
 * Maps to HTTP 422 Unprocessable Entity.
 */
@Serializable
data class ValidationErrors(
  val fields: List<FieldError>
) {
  constructor(vararg errors: FieldError) : this(errors.toList())

  override fun toString(): String =
    fields.joinToString("; ") { "${it.field}: ${it.message}" }
}

@Serializable
data class FieldError(
  val field: String,
  val message: String,
)

/** Build an [ErrorDto] from [ValidationErrors] for use in transport layer. */
fun ValidationErrors.toErrorDto(correlationId: String? = null): ErrorDto = ErrorDto(
  code = ErrorCodes.VALIDATION_ERROR,
  message = toString(),
  correlationId = correlationId,
  details = fields.associate { it.field to it.message },
)

