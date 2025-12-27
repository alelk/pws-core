package io.github.alelk.pws.domain.lyric.format

import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.ParserException
import com.copperleaf.kudzu.parser.chars.DigitParser
import com.copperleaf.kudzu.parser.chars.EndOfInputParser
import com.copperleaf.kudzu.parser.chars.NewlineCharParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.choice.PredictiveChoiceParser
import com.copperleaf.kudzu.parser.many.AtLeastParser
import com.copperleaf.kudzu.parser.many.ManyParser
import com.copperleaf.kudzu.parser.mapped.FlatMappedParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.maybe.MaybeParser
import com.copperleaf.kudzu.parser.named.NamedParser
import com.copperleaf.kudzu.parser.predict.PredictionParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser
import com.copperleaf.kudzu.parser.text.BaseTextParser
import com.copperleaf.kudzu.parser.text.LiteralTokenParser
import com.copperleaf.kudzu.parser.text.ScanParser
import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.forLocaleTag
import io.github.alelk.pws.domain.lyric.format.i18n.LyricKeywords
import io.github.alelk.pws.domain.song.lyric.*
import io.github.alelk.pws.domain.core.Locale as DomainLocale

private val defaultLocale: Locale = forLocaleTag("en")
internal val DEFAULT_LYRIC_PART_TYPE_BY_TEXT =
  mapOf(
    LyricKeywords.chorus(defaultLocale) to LyricPartType.CHORUS,
    LyricKeywords.bridge(defaultLocale) to LyricPartType.BRIDGE,
    LyricKeywords.verse(defaultLocale) to LyricPartType.VERSE
  )

internal class OptWhitespaceParser : BaseTextParser(
  isValidChar = { _, char -> char in setOf(' ', '\t') }, isValidText = { true }, invalidTextErrorMessage = { "" }, allowEmptyInput = true
)

internal enum class LyricPartType {
  VERSE,
  CHORUS,
  BRIDGE
}

internal sealed interface BaseLyricPartInfo {
  val type: LyricPartType
  val partNum: Int?
}

internal data class LyricPartInfo(override val type: LyricPartType, override val partNum: Int?, val text: String) : BaseLyricPartInfo {
  init {
    if (type == LyricPartType.VERSE) require(partNum != null) { "lyric part number required for verse" }
  }
}

internal data class LyricRepeatInfo(override val type: LyricPartType, override val partNum: Int? = null) : BaseLyricPartInfo

internal fun lyricPartTypeParser(typeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): NamedParser<ValueNode<LyricPartType>> =
  NamedParser(
    MappedParser(
      ExactChoiceParser(typeByText.keys.map { LiteralTokenParser(it) })
    ) {
      typeByText[it.text] ?: throw IllegalArgumentException("Invalid lyric part type: '${it.text}'")
    },
    name = "lyric part type",
  )

internal val lyricPartNumberParser: NamedParser<ValueNode<Int>> = NamedParser(
  MappedParser(
    MappedParser(ManyParser(DigitParser())) { d -> d.children.joinToString("") { it.text }.toInt() },
  ) { it.value },
  name = "lyric part number"
)

internal fun chorusOrBridgeLabelParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<Pair<LyricPartType, Int?>>> =
  MappedParser(
    SequenceParser(
      lyricPartTypeParser(lyricPartTypeByText.filterValues { it in setOf(LyricPartType.BRIDGE, LyricPartType.CHORUS) }),
      OptWhitespaceParser(),
      MaybeParser(lyricPartNumberParser),
      OptWhitespaceParser(),
      LiteralTokenParser(".")
    )
  ) {
    val partType: LyricPartType = it.node1.node.value
    val partNum: Int? = it.node3.node?.node?.value
    Pair(partType, partNum)
  }

internal fun verseLabelParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<Int>> =
  MappedParser(
    NamedParser(
      SequenceParser(
        OptWhitespaceParser(),
        MaybeParser(SequenceParser(LiteralTokenParser(lyricPartTypeByText.filterValues { it == LyricPartType.VERSE }.keys.first()), OptWhitespaceParser())),
        lyricPartNumberParser,
        OptWhitespaceParser(),
        LiteralTokenParser(".")
      ), name = "verse label"
    )
  ) { it.node.node3.node.value }

internal fun lyricPartRepeatLabel(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<LyricRepeatInfo>> =
  MappedParser(
    SequenceParser(
      LiteralTokenParser("["),
      OptWhitespaceParser(),
      lyricPartTypeParser(lyricPartTypeByText),
      OptWhitespaceParser(),
      MaybeParser(lyricPartNumberParser),
      OptWhitespaceParser(),
      LiteralTokenParser("]")
    )
  ) {
    val partType: LyricPartType = it.node3.node.value
    val partNum: Int? = it.node5.node?.node?.value
    LyricRepeatInfo(partType, partNum)
  }

internal fun lyricPartLabelParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<Pair<LyricPartType, Int?>>> =
  FlatMappedParser(
    ExactChoiceParser(
      chorusOrBridgeLabelParser(lyricPartTypeByText),
      MappedParser(verseLabelParser(lyricPartTypeByText)) { Pair(LyricPartType.VERSE, it.value) }
    )
  ) {
    @Suppress("UNCHECKED_CAST")
    it.node as ValueNode<Pair<LyricPartType, Int?>>
  }

internal fun lyricPartTextParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<String>> =
  MappedParser(
    NamedParser(
      ScanParser(
        stoppingCondition =
        PredictionParser(
          SequenceParser(
            NewlineCharParser(),
            ExactChoiceParser(lyricPartLabelParser(lyricPartTypeByText), lyricPartRepeatLabel(lyricPartTypeByText)),
            PredictiveChoiceParser(NewlineCharParser(), EndOfInputParser())
          )
        )
      ), name = "lyric part text"
    )
  ) { it.node.text }

internal fun lyricPartParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<LyricPartInfo>> =
  MappedParser(
    NamedParser(
      SequenceParser(
        NamedParser(lyricPartLabelParser(lyricPartTypeByText), name = "lyric part label"),
        AtLeastParser(NewlineCharParser(), minSize = 1),
        NamedParser(lyricPartTextParser(lyricPartTypeByText), name = "lyric part text")
      ), name = "lyric part"
    )
  ) {
    val (partType, partNum) = it.node.node1.node.value
    val partText = it.node.node3.node.value
    LyricPartInfo(partType, partNum, partText)
  }

internal fun lyricPartInfoParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): NamedParser<ValueNode<BaseLyricPartInfo>> =
  NamedParser(
    FlatMappedParser(ExactChoiceParser(lyricPartParser(lyricPartTypeByText), lyricPartRepeatLabel(lyricPartTypeByText))) {
      @Suppress("UNCHECKED_CAST")
      it.node as ValueNode<BaseLyricPartInfo>
    },
    name = "lyric part info"
  )

internal fun lyricInfoParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<List<BaseLyricPartInfo>>> =
  MappedParser(
    AtLeastParser(
      MappedParser(
        SequenceParser(MaybeParser(NewlineCharParser()), lyricPartInfoParser(lyricPartTypeByText), MaybeParser(NewlineCharParser()))
      ) { it.node2.node.value }, minSize = 1
    )
  ) { it.nodeList.map { n -> n.value } }

internal val List<LyricPart>.lastPartNum: Int? get() = this.flatMap { it.numbers }.maxOrNull()

internal val LyricPart.type: LyricPartType
  get() =
    when (this) {
      is Verse -> LyricPartType.VERSE
      is Chorus -> LyricPartType.CHORUS
      is Bridge -> LyricPartType.BRIDGE
    }

internal fun lyricParser(lyricPartTypeByText: Map<String, LyricPartType> = DEFAULT_LYRIC_PART_TYPE_BY_TEXT): Parser<ValueNode<Lyric>> {
  val lyricInfoParser = lyricInfoParser(lyricPartTypeByText)
  return MappedParser(lyricInfoParser) { p ->
    val parts = p.value.fold(listOf<LyricPart>()) { acc, partInfo ->
      when (partInfo) {
        // add new lyric part
        is LyricPartInfo -> {
          val partNum = partInfo.partNum ?: 1
          val prevNumber = acc.count { it.type == partInfo.type }
          if (prevNumber + 1 != partNum)
            throw ParserException(
              "invalid ${partInfo.type} part number $partNum, expected number: ${prevNumber + 1}",
              lyricInfoParser,
              this@MappedParser
            )
          require(prevNumber + 1 == partNum) { "invalid ${partInfo.type} part number $partNum, expected number: ${prevNumber + 1}" }
          val currentPartNum = (acc.lastPartNum ?: 0) + 1
          val lyricPartText = if (partInfo.text.endsWith('\n')) partInfo.text.dropLast(1) else partInfo.text
          val part = when (partInfo.type) {
            LyricPartType.VERSE -> Verse(setOf(currentPartNum), lyricPartText)
            LyricPartType.CHORUS -> Chorus(setOf(currentPartNum), lyricPartText)
            LyricPartType.BRIDGE -> Bridge(setOf(currentPartNum), lyricPartText)
          }
          acc + part
        }
        // repeat existing lyric part
        is LyricRepeatInfo -> {
          val lyricPartToRepeat =
            acc.filter { it.type == partInfo.type }
              .getOrNull((partInfo.partNum ?: 1) - 1)
              ?: IllegalArgumentException("not found lyric part '${partInfo.type}' #${partInfo.partNum ?: 1}")
          acc.map { if (it == lyricPartToRepeat) it.withNumbers(setOf((acc.lastPartNum ?: 0) + 1)) else it }
        }
      }
    }
    Lyric(parts)
  }
}

internal val defaultLyricParser = lyricParser()

internal fun getLyricKeywords(locale: Locale): Map<String, LyricPartType> {
  require(LyricKeywords.locales.contains(locale)) { "no lyric keywords of locale $locale found" }
  return mapOf(
    LyricKeywords.verse(locale) to LyricPartType.VERSE,
    LyricKeywords.chorus(locale) to LyricPartType.CHORUS,
    LyricKeywords.bridge(locale) to LyricPartType.BRIDGE
  )
}

internal val lyricParserByLocale = mutableMapOf<Locale, Parser<ValueNode<Lyric>>>()

fun getLyricParser(locale: DomainLocale): Parser<ValueNode<Lyric>> {
  val i18nLocale = forLocaleTag(locale.value)
  return lyricParserByLocale.getOrPut(i18nLocale) { lyricParser(getLyricKeywords(i18nLocale)) }
}

fun parseLyric(lyric: String, locale: DomainLocale? = null): Lyric =
  (if (locale == null) defaultLyricParser else getLyricParser(locale))
    .parse(ParserContext.fromString(lyric))
    .also { require(it.second.isEmpty()) { "error parsing whole lyric" } }
    .first.value

