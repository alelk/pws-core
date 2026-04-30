package io.github.alelk.pws.domain.lyric.format

import de.comahe.i18n4k.forLocaleTag
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.lyric.format.i18n.LyricKeywords

fun getLyricKeywords(locale: Locale): List<String> {
  val i18nLocale = forLocaleTag(locale.value)
  return listOf(
    LyricKeywords.chorus(i18nLocale),
    LyricKeywords.verse(i18nLocale),
    LyricKeywords.bridge(i18nLocale)
  ).filter { it.isNotBlank() }
}
