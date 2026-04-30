package io.github.alelk.pws.features.app

import androidx.compose.runtime.staticCompositionLocalOf

data class PwsAppInfo(
  val version: String
)

val LocalPwsAppInfo = staticCompositionLocalOf<PwsAppInfo?> { null }
