package io.github.alelk.pws.features.song.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.song.lyric.Bridge
import io.github.alelk.pws.domain.song.lyric.Chorus
import io.github.alelk.pws.domain.song.lyric.LyricPart
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.features.components.AppModalBottomSheet
import io.github.alelk.pws.features.components.AppTopBar
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.theme.spacing
import kotlinx.coroutines.delay
import org.koin.core.parameter.parametersOf

class SongDetailScreen(val songNumberId: SongNumberId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SongDetailScreenModel>(parameters = { parametersOf(songNumberId) })
    val state by viewModel.state.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    LaunchedEffect(state) {
      if (state is SongDetailUiState.Content) {
        delay(5000)
        viewModel.onSongViewed()
      }
    }

    SongDetailContent(
      state = state,
      songNumber = songNumberId.identifier,
      isFavorite = isFavorite,
      onFavoriteClick = {
        viewModel.onToggleFavorite()
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailContent(
  state: SongDetailUiState,
  songNumber: String? = null,
  isFavorite: Boolean = false,
  onFavoriteClick: () -> Unit = {}
) {
  val navigator = LocalNavigator.currentOrThrow
  var fontScale by remember { mutableFloatStateOf(1f) }
  var showSettingsSheet by remember { mutableStateOf(false) }

  // Sheet State
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  Scaffold(
    topBar = {
      val title = if (state is SongDetailUiState.Content && !songNumber.isNullOrBlank()) {
        "№ $songNumber"
      } else {
        "Песня"
      }

      AppTopBar(
        title = title,
        canNavigateBack = navigator.canPop,
        onNavigateBack = { navigator.pop() },
        actions = {
          IconButton(onClick = { showSettingsSheet = true }) {
            Icon(Icons.Filled.FormatSize, contentDescription = "Настройки текста")
          }
          IconButton(onClick = { /* TODO: Implement share */ }) {
            Icon(Icons.Filled.Share, contentDescription = "Поделиться")
          }
        }
      )
    },
    floatingActionButton = {
      if (state is SongDetailUiState.Content) {
        FloatingActionButton(
          onClick = onFavoriteClick,
          containerColor = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
          contentColor = if (isFavorite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
          elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
          modifier = Modifier.padding(bottom = 60.dp) // Lift FAB above global bottom bar if visible
        ) {
           Icon(
             imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
             contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное"
           )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      when (state) {
        SongDetailUiState.Loading -> LoadingContent()
        is SongDetailUiState.Content -> {
          SongContent(
            song = state.song,
            fontScale = fontScale
          )
        }
        SongDetailUiState.Error -> ErrorContent(
          title = "Песня не найдена",
          message = "Возможно, она была удалена или перемещена"
        )
      }
    }

    // Settings Sheet
    if (showSettingsSheet) {
      AppModalBottomSheet(
        onDismissRequest = { showSettingsSheet = false },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
      ) {
        TextSettingsSheet(
          fontScale = fontScale,
          onFontScaleChange = { fontScale = it }
        )
      }
    }
  }
}

@Composable
private fun TextSettingsSheet(
  fontScale: Float,
  onFontScaleChange: (Float) -> Unit
) {
  val spacing = MaterialTheme.spacing
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(spacing.lg)
      // Add standard bottom padding for navigation bars
      .padding(WindowInsets.navigationBars.asPaddingValues())
  ) {
    Text(
      text = "Размер текста",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(spacing.lg))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
      Icon(
        Icons.Filled.FormatSize,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Slider(
        value = fontScale,
        onValueChange = onFontScaleChange,
        valueRange = 0.7f..1.6f,
        steps = 9, // Steps: 0.7, 0.8 ... 1.6
        modifier = Modifier.weight(1f),
        colors = SliderDefaults.colors(
          thumbColor = MaterialTheme.colorScheme.primary,
          activeTrackColor = MaterialTheme.colorScheme.primary,
          inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      )
      Icon(
        Icons.Filled.FormatSize,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
    Spacer(Modifier.height(spacing.lg))
  }
}

@Composable
private fun SongContent(
  song: SongDetail,
  fontScale: Float,
  modifier: Modifier = Modifier
) {
  val spacing = MaterialTheme.spacing
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(
      top = spacing.lg,
      bottom = spacing.xxl,
      start = spacing.screenHorizontal,
      end = spacing.screenHorizontal
    ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header
    item {
      SongHeader(song)
      Spacer(Modifier.height(spacing.xl))
    }

    // Lyrics
    items(song.lyric) { part ->
      LyricPartView(
        part = part,
        fontScale = fontScale
      )
      Spacer(Modifier.height(spacing.lg))
    }

    // Metadata
    item {
      SongMetadata(song)
    }
  }
}

@Composable
private fun SongHeader(song: SongDetail) {
  val spacing = MaterialTheme.spacing
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
  ) {
    Text(
      text = song.name.value,
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      ),
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center
    )

    val tonalities = song.tonalities
    if (!tonalities.isNullOrEmpty()) {
      Spacer(Modifier.height(spacing.md))
      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = CircleShape,
      ) {
        Text(
          text = tonalities.joinToString(" • ") { it.identifier },
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun LyricPartView(
  part: LyricPart,
  fontScale: Float
) {
  val baseFontSize = 18.sp * fontScale
  val lineHeight = baseFontSize * 1.6f

  Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
    when (part) {
      is Verse -> {
        Row(modifier = Modifier.fillMaxWidth()) {
          // Subtle Verse Number
          if (part.numbers.isNotEmpty()) {
            Text(
              text = part.numbers.first().toString(),
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = baseFontSize * 0.8f,
                fontWeight = FontWeight.SemiBold
              ),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
              modifier = Modifier.width(32.dp).padding(top = 4.dp)
            )
          }

          Text(
            text = part.text.trim(),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = baseFontSize,
              lineHeight = lineHeight,
              color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.weight(1f)
          )
        }
      }

      is Chorus -> {
        IntrinsicChorusView(
          text = part.text,
          label = "Припев",
          fontSize = baseFontSize,
          lineHeight = lineHeight,
          accentColor = MaterialTheme.colorScheme.primary
        )
      }

      is Bridge -> {
        IntrinsicChorusView(
          text = part.text,
          label = "Бридж",
          fontSize = baseFontSize,
          lineHeight = lineHeight,
          accentColor = MaterialTheme.colorScheme.tertiary
        )
      }
    }
  }
}

@Composable
private fun IntrinsicChorusView(
  text: String,
  label: String,
  fontSize: androidx.compose.ui.unit.TextUnit,
  lineHeight: androidx.compose.ui.unit.TextUnit,
  accentColor: Color
) {
  val spacing = MaterialTheme.spacing
  // Use IntrinsicSize.Min to match the height of the accent line to the text content
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .height(IntrinsicSize.Min)
  ) {
    // Accent Line
    Spacer(Modifier.width(8.dp))
    Box(
      modifier = Modifier
        .width(4.dp)
        .fillMaxHeight()
        .clip(RoundedCornerShape(2.dp))
        .background(accentColor)
    )
    Spacer(Modifier.width(spacing.md))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        ),
        color = accentColor
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = text.trim(),
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = fontSize,
          lineHeight = lineHeight,
          fontStyle = FontStyle.Italic
        ),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
      )
    }
  }
}

@Composable
private fun SongMetadata(song: SongDetail) {
  val bibleRefText = song.bibleRef?.toString()
  val hasBibleRef = !bibleRefText.isNullOrBlank() && bibleRefText != "-"
  val hasMetadata = song.author != null ||
    song.composer != null ||
    song.translator != null ||
    song.year != null ||
    hasBibleRef

  if (!hasMetadata) return

  val spacing = MaterialTheme.spacing
  Column(
    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
  ) {
    Spacer(Modifier.height(spacing.xl))
    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
      modifier = Modifier.padding(vertical = spacing.lg)
    )

    Column(
      verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
      // Info Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Outlined.Info,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(spacing.sm))
        Text(
          text = "Информация о песне",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(Modifier.height(spacing.sm))

      if (song.author != null) MetadataItem("Автор", song.author!!.name)
      if (song.composer != null) MetadataItem("Композитор", song.composer!!.name)
      if (song.translator != null) MetadataItem("Перевод", song.translator!!.name)
      if (song.year != null) MetadataItem("Год", song.year.toString())
      if (hasBibleRef) {
        MetadataItem("Библия", bibleRefText)
      }
    }
  }
}

@Composable
private fun MetadataItem(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.End,
      modifier = Modifier.padding(start = 16.dp)
    )
  }
}
