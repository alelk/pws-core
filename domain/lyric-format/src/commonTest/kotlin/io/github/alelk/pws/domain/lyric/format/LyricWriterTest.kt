package io.github.alelk.pws.domain.lyric.format

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.song.lyric.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LyricWriterTest : StringSpec({
  val lyric = Lyric(listOf(
    Verse(setOf(1), "Verse 1 Line 1\nVerse 1 Line 2"),
    Chorus(setOf(2, 4), "Chorus 1 Line 1\nChorus 1 Line 2"),
    Verse(setOf(3, 5), "Verse 2 Line 1\nVerse 2 Line 2"),
    Chorus(setOf(6), "Chorus 2 Line 1\nChorus 2 Line 2"),
    Bridge(setOf(7), "Bridge Line 1\nBridge Line 2"),
  ))

  "write lyric in english" {
    lyric.toText(Locale.EN) shouldBe
      """|Verse 1.
         |Verse 1 Line 1
         |Verse 1 Line 2
         |
         |Chorus 1.
         |Chorus 1 Line 1
         |Chorus 1 Line 2
         |
         |Verse 2.
         |Verse 2 Line 1
         |Verse 2 Line 2
         |
         |[Chorus 1]
         |
         |[Verse 2]
         |
         |Chorus 2.
         |Chorus 2 Line 1
         |Chorus 2 Line 2
         |
         |Bridge.
         |Bridge Line 1
         |Bridge Line 2""".trimMargin()
  }

  "write lyric in russian" {
    lyric.toText(Locale.RU) shouldBe
      """|Куплет 1.
         |Verse 1 Line 1
         |Verse 1 Line 2
         |
         |Припев 1.
         |Chorus 1 Line 1
         |Chorus 1 Line 2
         |
         |Куплет 2.
         |Verse 2 Line 1
         |Verse 2 Line 2
         |
         |[Припев 1]
         |
         |[Куплет 2]
         |
         |Припев 2.
         |Chorus 2 Line 1
         |Chorus 2 Line 2
         |
         |Мост.
         |Bridge Line 1
         |Bridge Line 2""".trimMargin()
  }
})

