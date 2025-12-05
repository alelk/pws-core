package io.github.alelk.pws.api.contract.auth

import io.ktor.resources.*

@Resource("/auth")
class Auth {

  @Resource("/register")
  class Register(val parent: Auth = Auth())

  @Resource("/login")
  class Login(val parent: Auth = Auth())

  @Resource("/google")
  class Google(val parent: Auth = Auth()) {
    @Resource("/callback")
    class Callback(val parent: Google = Google())
  }

  @Resource("/vk")
  class Vk(val parent: Auth = Auth()) {
    @Resource("/callback")
    class Callback(val parent: Vk = Vk())
  }

  @Resource("/telegram")
  class Telegram(val parent: Auth = Auth())

  @Resource("/me")
  class Me(val parent: Auth = Auth())

  @Resource("/link-telegram")
  class LinkTelegram(val parent: Auth = Auth())
}

