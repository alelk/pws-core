package io.github.alelk.pws.api.contract.auth

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/auth")
class Auth {

  @Serializable
  @Resource("/register")
  class Register(val parent: Auth = Auth())

  @Serializable
  @Resource("/login")
  class Login(val parent: Auth = Auth())

  @Serializable
  @Resource("/google")
  class Google(val parent: Auth = Auth()) {
    @Serializable
    @Resource("/callback")
    class Callback(val parent: Google = Google())
  }

  @Serializable
  @Resource("/vk")
  class Vk(val parent: Auth = Auth()) {
    @Serializable
    @Resource("/callback")
    class Callback(val parent: Vk = Vk())
  }

  @Serializable
  @Resource("/telegram")
  class Telegram(val parent: Auth = Auth())

  @Serializable
  @Resource("/me")
  class Me(val parent: Auth = Auth())

  @Serializable
  @Resource("/link-telegram")
  class LinkTelegram(val parent: Auth = Auth())
}

