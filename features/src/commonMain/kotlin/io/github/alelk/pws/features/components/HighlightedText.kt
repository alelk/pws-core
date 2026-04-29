package io.github.alelk.pws.features.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

private val LegacyOpenHighlightTags = listOf(
  Regex("(?i)<b>\\s*<font[^>]*>"),
  Regex("(?i)<font[^>]*>\\s*<b>"),
  Regex("(?i)<(b|strong|em)>")
)

private val LegacyCloseHighlightTags = listOf(
  Regex("(?i)</font>\\s*</b>"),
  Regex("(?i)</b>\\s*</font>"),
  Regex("(?i)</(b|strong|em)>")
)

private val UnsupportedMarkupTags = Regex("(?i)</?(?!mark\\b)[a-z][^>]*>")

private fun normalizeSnippetMarkup(raw: String): String {
  var normalized = raw

  LegacyOpenHighlightTags.forEach { regex ->
    normalized = normalized.replace(regex, "<mark>")
  }

  LegacyCloseHighlightTags.forEach { regex ->
    normalized = normalized.replace(regex, "</mark>")
  }

  // Drop any remaining unsupported tags from new/legacy snippet formats.
  return normalized.replace(UnsupportedMarkupTags, "")
}

/**
 * Parses text with <mark> tags and returns AnnotatedString with highlighted spans.
 */
fun parseHighlightedText(
  text: String,
  highlightStyle: SpanStyle
): AnnotatedString {
  val normalizedText = normalizeSnippetMarkup(text)

  return buildAnnotatedString {
    var currentIndex = 0
    val markPattern = Regex(
      pattern = "(?is)<mark\\b[^>]*>(.*?)</mark>",
      options = setOf(RegexOption.IGNORE_CASE)
    )

    markPattern.findAll(normalizedText).forEach { match ->
      // Append text before the match
      if (match.range.first > currentIndex) {
        append(normalizedText.substring(currentIndex, match.range.first))
      }

      // Append highlighted text
      withStyle(highlightStyle) {
        append(match.groupValues[1])
      }

      currentIndex = match.range.last + 1
    }

    // Append remaining text
    if (currentIndex < normalizedText.length) {
      append(normalizedText.substring(currentIndex))
    }
  }
}

/**
 * Text component that renders <mark> tags as highlighted text.
 *
 * @param text Text containing optional <mark>highlighted</mark> sections
 * @param modifier Modifier for the Text component
 * @param style Text style (defaults to bodySmall)
 * @param color Base text color
 * @param highlightColor Background color for highlighted text (defaults to primary with alpha)
 * @param highlightTextColor Text color for highlighted portions
 * @param maxLines Maximum number of lines
 * @param overflow Text overflow behavior
 */
@Composable
fun HighlightedText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.bodySmall,
  color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  highlightColor: Color = MaterialTheme.colorScheme.primaryContainer,
  highlightTextColor: Color = MaterialTheme.colorScheme.primary,
  maxLines: Int = Int.MAX_VALUE,
  overflow: TextOverflow = TextOverflow.Clip
) {
  val highlightStyle = SpanStyle(
    color = highlightTextColor,
    fontWeight = FontWeight.SemiBold,
    background = highlightColor
  )

  val annotatedString = parseHighlightedText(text, highlightStyle)

  Text(
    text = annotatedString,
    modifier = modifier,
    style = style,
    color = color,
    maxLines = maxLines,
    overflow = overflow
  )
}
