package io.github.alelk.pws.domain.auth.storage

interface TokenStorage {
  fun saveToken(token: String)
  fun getToken(): String?
  fun clearToken()
  fun isAuthenticated(): Boolean = getToken() != null
}

