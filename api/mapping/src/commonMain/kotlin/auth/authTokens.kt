package io.github.alelk.pws.api.mapping.auth

import io.github.alelk.pws.api.contract.auth.AuthResponseDto
import io.github.alelk.pws.domain.auth.model.AuthTokens

val AuthResponseDto.authTokens: AuthTokens get() = AuthTokens(accessToken, refreshToken)