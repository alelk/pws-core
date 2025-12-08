package io.github.alelk.pws.domain.auth.storage

import io.github.alelk.pws.domain.auth.model.AuthTokens

interface TokenStorage {
  fun save(tokens: AuthTokens)
  fun get(): AuthTokens?
  fun clear()
  fun isAuthenticated(): Boolean = get() != null
}

