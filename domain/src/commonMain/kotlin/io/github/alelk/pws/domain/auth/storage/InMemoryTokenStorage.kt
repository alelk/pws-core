package io.github.alelk.pws.domain.auth.storage

import io.github.alelk.pws.domain.auth.model.AuthTokens
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class InMemoryTokenStorage : TokenStorage {
  val tokens: AtomicReference<AuthTokens?> = AtomicReference(null)

  override fun save(tokens: AuthTokens) {
    this.tokens.store(tokens)
  }

  override fun get(): AuthTokens? = this.tokens.load()

  override fun clear() = this.tokens.store(null)
}