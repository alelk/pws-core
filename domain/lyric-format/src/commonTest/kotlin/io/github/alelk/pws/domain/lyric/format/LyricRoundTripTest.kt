package io.github.alelk.pws.domain.lyric.format

import io.github.alelk.pws.domain.song.lyric.lyric
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll

/**
 * Property-based round-trip: writing a [Lyric] to text and parsing it back must reproduce the
 * original structure (verses/choruses/bridges and their numbering, including repeated-part
 * references). This guards the format that user-edited and bundled song lyrics travel through.
 */
class LyricRoundTripTest : StringSpec({

  "parseLyric(lyric.toText(locale)) reproduces the original lyric" {
    checkAll(Arb.lyric()) { (lyric, locale) ->
      parseLyric(lyric.toText(locale), locale) shouldBe lyric
    }
  }
})
