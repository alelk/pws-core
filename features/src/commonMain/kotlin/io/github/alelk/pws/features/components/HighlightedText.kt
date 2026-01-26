package io.github.alelk.pws.features.components

import androidx.compose.material3.LocalTextStyle
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

/**
 * Parses text with <mark> tags and returns AnnotatedString with highlighted spans.
 */
fun parseHighlightedText(
  text: String,
  highlightStyle: SpanStyle
): AnnotatedString {
  return buildAnnotatedString {
    var currentIndex = 0
    val markPattern = Regex("<mark>(.*?)</mark>", RegexOption.IGNORE_CASE)

    markPattern.findAll(text).forEach { match ->
      // Append text before the match
      if (match.range.first > currentIndex) {
        append(text.substring(currentIndex, match.range.first))
      }

      // Append highlighted text
      withStyle(highlightStyle) {
        append(match.groupValues[1])
      }

      currentIndex = match.range.last + 1
    }

    // Append remaining text
    if (currentIndex < text.length) {
      append(text.substring(currentIndex))
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
