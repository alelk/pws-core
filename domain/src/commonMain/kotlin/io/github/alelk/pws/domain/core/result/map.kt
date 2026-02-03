package io.github.alelk.pws.domain.core.result

suspend fun <A : Any, B : Any> CreateResourceResult<A>.map(transform: suspend (A) -> B): CreateResourceResult<B> =
  when (this) {
    is CreateResourceResult.Success -> CreateResourceResult.Success(resource = transform(this.resource))
    is CreateResourceResult.AlreadyExists -> CreateResourceResult.AlreadyExists(resource = transform(this.resource), message = this.message)
    is CreateResourceResult.ValidationError -> CreateResourceResult.ValidationError(resource = transform(this.resource), message = this.message)
    is CreateResourceResult.UnknownError ->
      CreateResourceResult.UnknownError(resource = transform(this.resource), exception = this.exception, message = this.message)
  }

suspend fun <A : Any, B : Any> DeleteResourceResult<A>.map(transform: suspend (A) -> B): DeleteResourceResult<B> =
  when (this) {
    is DeleteResourceResult.Success -> DeleteResourceResult.Success(resource = transform(this.resource))
    is DeleteResourceResult.NotFound -> DeleteResourceResult.NotFound(resource = transform(this.resource))
    is DeleteResourceResult.ValidationError -> DeleteResourceResult.ValidationError(resource = transform(this.resource), message = this.message)

    is DeleteResourceResult.UnknownError ->
      DeleteResourceResult.UnknownError(resource = transform(this.resource), exception = this.exception, message = this.message)
  }

suspend fun <A : Any, B : Any> UpdateResourceResult<A>.map(transform: suspend (A) -> B): UpdateResourceResult<B> =
  when (this) {
    is UpdateResourceResult.Success -> UpdateResourceResult.Success(resource = transform(this.resource))
    is UpdateResourceResult.NotFound -> UpdateResourceResult.NotFound(resource = transform(this.resource))
    is UpdateResourceResult.ValidationError -> UpdateResourceResult.ValidationError(resource = transform(this.resource), message = this.message)
    is UpdateResourceResult.UnknownError ->
      UpdateResourceResult.UnknownError(resource = transform(this.resource), exception = this.exception, message = this.message)
  }