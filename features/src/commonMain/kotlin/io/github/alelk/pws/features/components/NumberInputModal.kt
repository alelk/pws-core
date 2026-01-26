package io.github.alelk.pws.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.features.theme.spacing

/**
 * Modal for entering song number with custom numpad.
 *
 * @param books List of available books
 * @param selectedBook Initially selected book (or first from list)
 * @param onDismiss Called when modal should be dismissed
 * @param onConfirm Called with selected BookId and song number when user confirms
 */
@Composable
fun NumberInputModal(
  books: List<BookSummary>,
  selectedBook: BookSummary? = null,
  onDismiss: () -> Unit,
  onConfirm: (bookId: BookId, songNumber: Int) -> Unit,
  modifier: Modifier = Modifier
) {
  var currentBook by remember { mutableStateOf(selectedBook ?: books.firstOrNull()) }
  var inputValue by remember { mutableStateOf("") }

  val maxNumber by remember(currentBook) {
    derivedStateOf { currentBook?.countSongs ?: 999 }
  }

  val isValid by remember(inputValue, maxNumber) {
    derivedStateOf {
      inputValue.isNotEmpty() &&
        inputValue.toIntOrNull()?.let { it in 1..maxNumber } == true
    }
  }

  // Full screen overlay
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.5f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Bottom sheet content
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = false) { }, // Prevent clicks through
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(MaterialTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Handle bar
        Box(
          modifier = Modifier
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        // Header with close button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Введите номер песни",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Закрыть",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Book selector
        if (books.size > 1) {
          Spacer(Modifier.height(MaterialTheme.spacing.md))
          BookSelector(
            books = books,
            selectedBook = currentBook,
            onBookSelected = {
              currentBook = it
              // Reset input if exceeds new max
              inputValue.toIntOrNull()?.let { num ->
                if (num > (it.countSongs)) {
                  inputValue = ""
                }
              }
            }
          )
        }

        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        // Range hint
        Text(
          text = "от 1 до $maxNumber",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(MaterialTheme.spacing.sm))

        // Number display
        NumberDisplay(
          value = inputValue,
          isValid = isValid || inputValue.isEmpty()
        )

        Spacer(Modifier.height(MaterialTheme.spacing.xl))

        // Numpad
        NumberPad(
          onDigit = { digit ->
            val newValue = inputValue + digit
            // Limit to reasonable length and max value
            if (newValue.length <= 4) {
              inputValue = newValue
            }
          },
          onBackspace = {
            if (inputValue.isNotEmpty()) {
              inputValue = inputValue.dropLast(1)
            }
          },
          onConfirm = {
            if (isValid && currentBook != null) {
              val number = inputValue.toInt()
              onConfirm(currentBook!!.id, number)
            }
          },
          isConfirmEnabled = isValid
        )

        Spacer(Modifier.height(MaterialTheme.spacing.lg))
      }
    }
  }
}

@Composable
private fun BookSelector(
  books: List<BookSummary>,
  selectedBook: BookSummary?,
  onBookSelected: (BookSummary) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
  ) {
    items(books, key = { it.id.toString() }) { book ->
      val isSelected = book.id == selectedBook?.id
      FilterChip(
        selected = isSelected,
        onClick = { onBookSelected(book) },
        label = {
          Text(
            text = book.displayShortName.value,
            style = MaterialTheme.typography.labelMedium
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
    }
  }
}

@Composable
private fun NumberDisplay(
  value: String,
  isValid: Boolean,
  modifier: Modifier = Modifier
) {
  val displayColor = when {
    value.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    isValid -> MaterialTheme.colorScheme.onSurface
    else -> MaterialTheme.colorScheme.error
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = MaterialTheme.spacing.xl),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh
  ) {
    Text(
      text = value.ifEmpty { "—" },
      style = MaterialTheme.typography.displayMedium.copy(
        fontWeight = FontWeight.Light,
        letterSpacing = 8.sp
      ),
      color = displayColor,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = MaterialTheme.spacing.lg)
    )
  }
}

@Composable
private fun NumberPad(
  onDigit: (Char) -> Unit,
  onBackspace: () -> Unit,
  onConfirm: () -> Unit,
  isConfirmEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
  ) {
    // Row 1: 1, 2, 3
    NumberPadRow(
      buttons = listOf(
        NumPadButton.Digit('1'),
        NumPadButton.Digit('2'),
        NumPadButton.Digit('3')
      ),
      onDigit = onDigit,
      onBackspace = onBackspace,
      onConfirm = onConfirm,
      isConfirmEnabled = isConfirmEnabled
    )

    // Row 2: 4, 5, 6
    NumberPadRow(
      buttons = listOf(
        NumPadButton.Digit('4'),
        NumPadButton.Digit('5'),
        NumPadButton.Digit('6')
      ),
      onDigit = onDigit,
      onBackspace = onBackspace,
      onConfirm = onConfirm,
      isConfirmEnabled = isConfirmEnabled
    )

    // Row 3: 7, 8, 9
    NumberPadRow(
      buttons = listOf(
        NumPadButton.Digit('7'),
        NumPadButton.Digit('8'),
        NumPadButton.Digit('9')
      ),
      onDigit = onDigit,
      onBackspace = onBackspace,
      onConfirm = onConfirm,
      isConfirmEnabled = isConfirmEnabled
    )

    // Row 4: Backspace, 0, Confirm
    NumberPadRow(
      buttons = listOf(
        NumPadButton.Backspace,
        NumPadButton.Digit('0'),
        NumPadButton.Confirm
      ),
      onDigit = onDigit,
      onBackspace = onBackspace,
      onConfirm = onConfirm,
      isConfirmEnabled = isConfirmEnabled
    )
  }
}

private sealed interface NumPadButton {
  data class Digit(val char: Char) : NumPadButton
  data object Backspace : NumPadButton
  data object Confirm : NumPadButton
}

@Composable
private fun NumberPadRow(
  buttons: List<NumPadButton>,
  onDigit: (Char) -> Unit,
  onBackspace: () -> Unit,
  onConfirm: () -> Unit,
  isConfirmEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
  ) {
    buttons.forEach { button ->
      when (button) {
        is NumPadButton.Digit -> {
          NumPadDigitButton(
            digit = button.char,
            onClick = { onDigit(button.char) }
          )
        }
        NumPadButton.Backspace -> {
          NumPadActionButton(
            icon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Удалить",
                modifier = Modifier.size(24.dp)
              )
            },
            onClick = onBackspace,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
          )
        }
        NumPadButton.Confirm -> {
          NumPadActionButton(
            icon = {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Подтвердить",
                modifier = Modifier.size(28.dp)
              )
            },
            onClick = onConfirm,
            enabled = isConfirmEnabled,
            containerColor = if (isConfirmEnabled)
              MaterialTheme.colorScheme.primary
            else
              MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (isConfirmEnabled)
              MaterialTheme.colorScheme.onPrimary
            else
              MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
          )
        }
      }
    }
  }
}

@Composable
private fun NumPadDigitButton(
  digit: Char,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .size(72.dp)
      .clip(CircleShape)
      .clickable(onClick = onClick),
    shape = CircleShape,
    color = MaterialTheme.colorScheme.surfaceContainerHigh
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = digit.toString(),
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun NumPadActionButton(
  icon: @Composable () -> Unit,
  onClick: () -> Unit,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  Surface(
    modifier = modifier
      .size(72.dp)
      .clip(CircleShape)
      .clickable(enabled = enabled, onClick = onClick),
    shape = CircleShape,
    color = containerColor,
    contentColor = contentColor
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      icon()
    }
  }
}
