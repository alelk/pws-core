package io.github.alelk.pws.domain.core.result

sealed interface DeleteResourceResult<out R : Any> {
  data class Success<out R : Any>(val resource: R) : DeleteResourceResult<R>

  data class NotFound<out R : Any>(val resource: R) : DeleteResourceResult<R>

  data class ValidationError<out R : Any>(val resource: R, val message: String) : DeleteResourceResult<R>

  data class UnknownError<out R : Any>(
    val resource: R,
    val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : DeleteResourceResult<R>
}

fun <A : Any, B : Any> DeleteResourceResult<A>.map(transform: (A) -> B): DeleteResourceResult<B> =
  when (this) {
    is DeleteResourceResult.Success -> DeleteResourceResult.Success(resource = transform(this.resource))
    is DeleteResourceResult.NotFound -> DeleteResourceResult.NotFound(resource = transform(this.resource))
    is DeleteResourceResult.ValidationError -> DeleteResourceResult.ValidationError(
      resource = transform(this.resource),
      message = this.message
    )

    is DeleteResourceResult.UnknownError -> DeleteResourceResult.UnknownError(
      resource = transform(this.resource),
      exception = this.exception,
      message = this.message
    )
  }