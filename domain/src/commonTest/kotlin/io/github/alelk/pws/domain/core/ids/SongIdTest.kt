package io.github.alelk.pws.domain.core.ids

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll

class SongIdTest : StringSpec({
  "create song id from valid long" {
    SongId(1).value shouldBe 1
  }

  "create song id from negative long fails" {
    shouldThrow<IllegalArgumentException> {
      SongId(-1)
    }
  }

  "convert song id to string and parse it back for random song id" {
    checkAll(Arb.Companion.songId()) { songId ->
      val string = songId.toString()
      val parsed = SongId.parse(string)
      parsed shouldBe songId
    }
  }
})