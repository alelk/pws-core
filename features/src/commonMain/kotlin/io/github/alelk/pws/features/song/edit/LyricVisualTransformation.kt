package io.github.alelk.pws.features.song.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.lyric.format.getLyricKeywords

class LyricVisualTransformation(
  private val locale: Locale,
  private val highlightColor: Color
) : VisualTransformation {

  private val keywords = getLyricKeywords(locale)

  private val labelRegex = Regex(
    pattern = """^\s*((\d+\.)|(${keywords.joinToString("|") { Regex.escape(it) }}\s*\d*\.))""",
    options = setOf(RegexOption.MULTILINE)
  )

  private val repeatRegex = Regex(
    pattern = """\[(${keywords.joinToString("|") { Regex.escape(it) }}\s*\d*)\]""",
    options = setOf(RegexOption.IGNORE_CASE)
  )

  override fun filter(text: AnnotatedString): TransformedText {
    val builder = AnnotatedString.Builder()
    val rawText = text.text
    
    // This is a simple implementation. For better performance with very large texts,
    // we might want a more optimized approach, but for songs it should be fine.
    
    var lastIndex = 0
    
    // Find all matches for labels and repeats
    val matches = (labelRegex.findAll(rawText) + repeatRegex.findAll(rawText))
      .sortedBy { it.range.first }
    
    for (match in matches) {
      if (match.range.first < lastIndex) continue // Skip overlapping (shouldn't happen with these regexes)
      
      // Append text before match
      builder.append(rawText.substring(lastIndex, match.range.first))
      
      // Append highlighted match
      builder.withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
        append(match.value)
      }
      
      lastIndex = match.range.last + 1
    }
    
    if (lastIndex < rawText.length) {
      builder.append(rawText.substring(lastIndex))
    }
    
    return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
  }
}
