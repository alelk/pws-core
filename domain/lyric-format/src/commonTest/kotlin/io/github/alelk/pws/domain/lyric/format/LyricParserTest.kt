package io.github.alelk.pws.domain.lyric.format

import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.ParserException
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.song.lyric.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class LyricParserTest : FeatureSpec({
  feature(::chorusOrBridgeLabelParser.name) {
    scenario("parse chorus label without number") {
      val (partType, partNum) = chorusOrBridgeLabelParser().parse(ParserContext.fromString("Chorus.")).first.value
      partType shouldBe LyricPartType.CHORUS
      partNum shouldBe null
    }

    scenario("parse bridge label without number") {
      val (partType, partNum) = chorusOrBridgeLabelParser().parse(ParserContext.fromString("Bridge .")).first.value
      partType shouldBe LyricPartType.BRIDGE
      partNum shouldBe null
    }

    scenario("parse chorus label with number") {
      val (partType, partNum) = chorusOrBridgeLabelParser().parse(ParserContext.fromString("Chorus  9.")).first.value
      partType shouldBe LyricPartType.CHORUS
      partNum shouldBe 9
    }

    scenario("parse bridge label with number") {
      val (partType, partNum) = chorusOrBridgeLabelParser().parse(ParserContext.fromString("Bridge9 .")).first.value
      partType shouldBe LyricPartType.BRIDGE
      partNum shouldBe 9
    }

    scenario("parse label with number (multiple digits)") {
      val (partType, partNum) = chorusOrBridgeLabelParser().parse(ParserContext.fromString("Bridge 901 .")).first.value
      partType shouldBe LyricPartType.BRIDGE
      partNum shouldBe 901
    }

    scenario("throw exception on wrong label") {
      shouldThrow<ParserException> {
        chorusOrBridgeLabelParser().parse(ParserContext.fromString("Chorus ABC.")).first.value
      }
    }

    scenario("do not parse label with line break") {
      shouldThrow<ParserException> {
        chorusOrBridgeLabelParser().parse(ParserContext.fromString("Bridge \n7.")).first.value
      }
    }

    scenario("predict parsing chorus label") {
      val text = ParserContext.fromString(
        """|Chorus 5.
           |Chorus 5 Line 1
           |Chorus 5 Line 2
        """.trimMargin()
      )
      chorusOrBridgeLabelParser().predict(text) shouldBe true
      val chorus = chorusOrBridgeLabelParser().parse(text)
      chorus.first.value.first shouldBe LyricPartType.CHORUS
      chorus.first.value.second shouldBe 5
      chorus.second.isEmpty() shouldBe false
      chorus.second.sourcePosition.lineNumber shouldBe 1
      chorus.second.sourcePosition.lineColumn shouldBe 10
    }
  }

  feature(::verseLabelParser.name) {
    scenario("parse verse label") {
      val partNum = verseLabelParser().parse(ParserContext.fromString("5.")).first.value
      partNum shouldBe 5
    }
  }

  feature(::lyricPartLabelParser.name) {
    scenario("parse chorus label") {
      val (partType, partNum) = lyricPartLabelParser().parse(ParserContext.fromString("Chorus.")).first.value
      partType shouldBe LyricPartType.CHORUS
      partNum shouldBe null
    }

    scenario("parse bridge label") {
      val (partType, partNum) = lyricPartLabelParser().parse(ParserContext.fromString("Bridge 7 .")).first.value
      partType shouldBe LyricPartType.BRIDGE
      partNum shouldBe 7
    }

    scenario("parse verse label with whitespace") {
      val (partType, partNum) = lyricPartLabelParser().parse(ParserContext.fromString("4 .")).first.value
      partType shouldBe LyricPartType.VERSE
      partNum shouldBe 4
    }

    scenario("parse verse label when multiple digits") {
      val (partType, partNum) = lyricPartLabelParser().parse(ParserContext.fromString("445 .")).first.value
      partType shouldBe LyricPartType.VERSE
      partNum shouldBe 445
    }
  }

  feature(::lyricPartParser.name) {
    scenario("parse verse") {
      val text =
        """|4  .
           |  
           |
           |line 1
           |line 2
           |3
           |line 4.
           |5. line 5""".trimMargin()
      val lyricPart = lyricPartParser().parse(ParserContext.fromString(text)).first.value
      lyricPart shouldBe LyricPartInfo(LyricPartType.VERSE, 4, "  \n\nline 1\nline 2\n3\nline 4.\n5. line 5")
    }

    scenario("parse chorus") {
      val lyricPart = lyricPartParser().parse(ParserContext.fromString("Chorus.\nThis is a chorus.")).first.value
      lyricPart shouldBe LyricPartInfo(LyricPartType.CHORUS, null, "This is a chorus.")
    }

    scenario("parse bridge") {
      val lyricPart = lyricPartParser().parse(ParserContext.fromString("Bridge 7.\nThis is a bridge.")).first.value
      lyricPart shouldBe LyricPartInfo(LyricPartType.BRIDGE, 7, "This is a bridge.")
    }
  }

  feature(::lyricPartInfoParser.name) {
    scenario("parse verse info") {
      val text =
        """|1.
           |Verse - Line 1
           |Verse - Line 2
           |""".trimMargin()
      val verse = lyricPartInfoParser().parse(ParserContext.fromString(text)).first.node.value
      verse shouldBe LyricPartInfo(type = LyricPartType.VERSE, partNum = 1, text = "Verse - Line 1\nVerse - Line 2\n")
    }
    scenario("parse chorus info") {
      val text =
        """|Chorus 5.
           |Chorus - Line 1
           |Chorus - Line 2
           |""".trimMargin()
      val verse = lyricPartInfoParser().parse(ParserContext.fromString(text)).first.node.value
      verse shouldBe LyricPartInfo(type = LyricPartType.CHORUS, partNum = 5, text = "Chorus - Line 1\nChorus - Line 2\n")
    }
    scenario("parse repeat label") {
      val text = """[Chorus 9]""".trimMargin()
      val verse = lyricPartInfoParser().parse(ParserContext.fromString(text)).first.node.value
      verse shouldBe LyricRepeatInfo(type = LyricPartType.CHORUS, partNum = 9)
    }
  }

  feature(::lyricInfoParser.name) {
    scenario("parse two verses") {
      val text =
        """|1.
           |Verse 1 Line 1
           |Verse 1 Line 2
           |2.
           |Verse 2 Line 1
           |Verse 2 Line 2
           |""".trimMargin()
      val lyricInfo = lyricInfoParser().parse(ParserContext.fromString(text)).first.value
      lyricInfo shouldBe listOf(
        LyricPartInfo(LyricPartType.VERSE, 1, "Verse 1 Line 1\nVerse 1 Line 2"),
        LyricPartInfo(LyricPartType.VERSE, 2, "Verse 2 Line 1\nVerse 2 Line 2\n"),
      )
    }

    scenario("parse verse, chorus, repeat labels") {
      val text =
        """|1.
           |Verse 1 Line 1
           |Verse 1 Line 2
           |
           |Chorus.
           |Chorus Line 1
           |Chorus Line 2
           |
           |[Verse 1]
           |
           |[Chorus]
           |""".trimMargin()
      val lyricInfo = lyricInfoParser().parse(ParserContext.fromString(text)).first.value
      lyricInfo shouldBe listOf(
        LyricPartInfo(LyricPartType.VERSE, 1, "Verse 1 Line 1\nVerse 1 Line 2\n"),
        LyricPartInfo(LyricPartType.CHORUS, null, "Chorus Line 1\nChorus Line 2\n"),
        LyricRepeatInfo(LyricPartType.VERSE, 1),
        LyricRepeatInfo(LyricPartType.CHORUS),
      )
    }
  }

  feature(::lyricParser.name) {
    scenario("parse simple lyric text (two verses)") {
      val text = """|
        |1.
        |Verse 1 Line 1
        |Verse 1 Line 2
        |
        |2.
        |Verse 2 Line 1
        |Verse 2 Line 2
      """.trimMargin()
      val lyric = lyricParser().parse(ParserContext.fromString(text)).first.value
      lyric shouldBe Lyric(listOf(
        Verse(setOf(1), "Verse 1 Line 1\nVerse 1 Line 2"),
        Verse(setOf(2), "Verse 2 Line 1\nVerse 2 Line 2"),
      ))
    }

    scenario("parse simple lyric text (verse and chorus)") {
      val text = """|
        |1.
        |Verse 1 Line 1
        |Verse 1 Line 2
        |
        |Chorus.
        |Chorus Line 1
        |Chorus Line 2
      """.trimMargin()
      val lyric = lyricParser().parse(ParserContext.fromString(text)).first.value
      lyric shouldBe Lyric(listOf(
        Verse(setOf(1), "Verse 1 Line 1\nVerse 1 Line 2"),
        Chorus(setOf(2), "Chorus Line 1\nChorus Line 2"),
      ))
    }

    scenario("parse complex lyric text (multiple part types + repeat)") {
      val text = """|
        |1.
        |Verse 1 Line 1
        |Verse 1 Line 2
        |
        |Chorus 1.
        |Chorus 1 Line 1
        |Chorus 1 Line 2
        |
        |[Verse 1]
        |
        |Bridge.
        |Bridge Line 1
        |Bridge Line 2
        |
        |[Chorus 1]
        |
        |2.
        |Verse 2 Line 1
        |Verse 2 Line 2
        |
        |[Bridge]
        |[Verse 1]
        |""".trimMargin()
      val lyric = lyricParser().parse(ParserContext.fromString(text)).first.value
      lyric shouldBe Lyric(listOf(
        Verse(setOf(1, 3, 8), "Verse 1 Line 1\nVerse 1 Line 2"),
        Chorus(setOf(2, 5), "Chorus 1 Line 1\nChorus 1 Line 2"),
        Bridge(setOf(4, 7), "Bridge Line 1\nBridge Line 2"),
        Verse(setOf(6), "Verse 2 Line 1\nVerse 2 Line 2"),
      ))
    }
  }

  feature("parse different languages") {
    scenario("ru") {
      val text = """|
        |1.
        |Verse 1 Line 1
        |Verse 1 Line 2
        |
        |Припев 1.
        |Chorus 1 Line 1
        |Chorus 1 Line 2
        |
        |[Куплет 1]
        |
        |Мост.
        |Bridge Line 1
        |Bridge Line 2
        |
        |[Припев 1]
        |
        |2.
        |Verse 2 Line 1
        |Verse 2 Line 2
        |
        |[Мост]
        |[Куплет 1]
        |""".trimMargin()
      val lyric = getLyricParser(Locale.RU).parse(ParserContext.fromString(text)).first.value
      lyric shouldBe Lyric(listOf(
        Verse(setOf(1, 3, 8), "Verse 1 Line 1\nVerse 1 Line 2"),
        Chorus(setOf(2, 5), "Chorus 1 Line 1\nChorus 1 Line 2"),
        Bridge(setOf(4, 7), "Bridge Line 1\nBridge Line 2"),
        Verse(setOf(6), "Verse 2 Line 1\nVerse 2 Line 2"),
      ))
    }
  }
})

