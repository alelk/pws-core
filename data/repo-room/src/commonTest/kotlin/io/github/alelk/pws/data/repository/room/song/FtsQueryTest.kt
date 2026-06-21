package io.github.alelk.pws.data.repository.room.song

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FtsQueryTest : StringSpec({

  "single word becomes a prefix term" {
    buildFtsPrefixQuery("бог") shouldBe "бог*"
  }

  "each word becomes a prefix term" {
    buildFtsPrefixQuery("свят бог") shouldBe "свят* бог*"
  }

  "collapses repeated and surrounding whitespace" {
    buildFtsPrefixQuery("  свят   бог  ") shouldBe "свят* бог*"
  }

  "blank input yields an empty query" {
    buildFtsPrefixQuery("   ") shouldBe ""
    buildFtsPrefixQuery("") shouldBe ""
  }

  "handles tabs and newlines as separators" {
    buildFtsPrefixQuery("свят\tбог\nгосподь") shouldBe "свят* бог* господь*"
  }
})
