package io.github.alelk.pws.domain.lyric.format

import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.forLocaleTag
import io.github.alelk.pws.domain.lyric.format.i18n.LyricKeywords
import io.github.alelk.pws.domain.song.lyric.*
import io.github.alelk.pws.domain.core.Locale as DomainLocale

fun Lyric.toText(locale: DomainLocale): String {
  require(this.isNotEmpty()) { "lyric is empty" }
  val partNumbers = this.flatMap { it.numbers }
  val duplicatedNumbers = partNumbers.groupBy { it }.filter { it.value.size > 1 }.keys
  require(duplicatedNumbers.isEmpty()) { "lyric contains duplicated part numbers: ${duplicatedNumbers.joinToString(", ")}" }
  val missedNumbers = (partNumbers.min()..partNumbers.max()) - partNumbers.toSet()
  require(missedNumbers.isEmpty()) { "lyric contains missed part numbers: ${missedNumbers.joinToString(", ")}" }
  val lyricParts = this.sortedBy { it.numbers.min() }
  val verses = lyricParts.filterIsInstance<Verse>().withIndex()
  val choruses = lyricParts.filterIsInstance<Chorus>().withIndex()
  val bridges = lyricParts.filterIsInstance<Bridge>().withIndex()
  val versesRepeated = verses.any { it.value.numbers.size > 1 }
  val singleChorus = choruses.count() == 1
  val singleBridge = bridges.count() == 1

  val i18nLocale: Locale = forLocaleTag(locale.value)
  require(LyricKeywords.locales.contains(i18nLocale)) { "no lyric keywords of locale $locale found" }
  val verseKeyword = LyricKeywords.verse(i18nLocale)
  val chorusKeyword = LyricKeywords.chorus(i18nLocale)
  val bridgeKeyword = LyricKeywords.bridge(i18nLocale)

  data class LyricPartInfo(val number: Int, val partNumber: Int, val part: LyricPart, val isFirstOccurrence: Boolean)

  val lyricText = (verses + choruses + bridges)
    .flatMap { (n, p) -> p.numbers.withIndex().map { LyricPartInfo(it.value, n, p, it.index == 0) } }
    // sort by lyric part number
    .sortedBy { it.number }
    .joinToString("\n\n") { lyricPartInfo ->
      when {
        // if at least one verse is repeated in lyric, then add "Verse" prefix for each verse
        lyricPartInfo.part is Verse && lyricPartInfo.isFirstOccurrence && versesRepeated ->
          "$verseKeyword ${lyricPartInfo.partNumber + 1}.\n${lyricPartInfo.part.text}"

        // when verses never repeated, then print only lyric part number (skip "Verse" prefix)
        lyricPartInfo.part is Verse && lyricPartInfo.isFirstOccurrence && !versesRepeated ->
          "${lyricPartInfo.partNumber + 1}.\n${lyricPartInfo.part.text}"

        // when second occurrence of psalm verse
        lyricPartInfo.part is Verse ->
          "[$verseKeyword ${lyricPartInfo.partNumber + 1}]"

        // if only one chorus in lyric, then skip chorus number
        lyricPartInfo.part is Chorus && singleChorus && lyricPartInfo.isFirstOccurrence ->
          "$chorusKeyword.\n${lyricPartInfo.part.text}"

        // if more than one chorus in lyric, then print chorus number
        lyricPartInfo.part is Chorus && lyricPartInfo.isFirstOccurrence ->
          "$chorusKeyword ${lyricPartInfo.partNumber + 1}.\n${lyricPartInfo.part.text}"

        // when second occurrence of chorus
        lyricPartInfo.part is Chorus && singleChorus ->
          "[$chorusKeyword]"

        lyricPartInfo.part is Chorus ->
          "[$chorusKeyword ${lyricPartInfo.partNumber + 1}]"

        // if only one bridge in lyric, then skip bridge number
        lyricPartInfo.part is Bridge && singleBridge && lyricPartInfo.isFirstOccurrence ->
          "$bridgeKeyword.\n${lyricPartInfo.part.text}"

        // if more than one bridge in lyric, then print bridge number
        lyricPartInfo.part is Bridge && lyricPartInfo.isFirstOccurrence ->
          "$bridgeKeyword ${lyricPartInfo.partNumber + 1}.\n${lyricPartInfo.part.text}"

        // when second occurrence of bridge
        lyricPartInfo.part is Bridge && singleBridge ->
          "[$bridgeKeyword]"

        lyricPartInfo.part is Bridge ->
          "[$bridgeKeyword ${lyricPartInfo.partNumber + 1}]"

        else -> throw IllegalArgumentException("Unknown lyric part type: ${lyricPartInfo.part}")
      }
    }
  return lyricText
}

