package io.github.alelk.pws.features.song.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.theme.spacing
import org.koin.core.parameter.parametersOf

class SongDetailScreen(val songNumberId: SongNumberId) : Screen {
  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = koinScreenModel<SongDetailScreenModel>(parameters = { parametersOf(songNumberId) })
    val state by viewModel.state.collectAsState()
    val searchScreen = cafe.adriel.voyager.core.registry.rememberScreen(io.github.alelk.pws.core.navigation.SharedScreens.Search)

    SongDetailContent(
      state = state,
      onSearchClick = {
        navigator.push(searchScreen)
      },
      onNumberInputClick = {
        // TODO: Show number input modal in context of current book
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailContent(
  state: SongDetailUiState,
  onSearchClick: () -> Unit = {},
  onNumberInputClick: () -> Unit = {},
  onFavoriteClick: () -> Unit = {}
) {
  val navigator = LocalNavigator.currentOrThrow
  var fontScale by remember { mutableFloatStateOf(1f) }
  var isFavorite by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { },
        navigationIcon = {
          IconButton(onClick = { navigator.pop() }) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Назад"
            )
          }
        },
        actions = {
          // Search button
          IconButton(onClick = onSearchClick) {
            Icon(
              Icons.Default.Search,
              contentDescription = "Поиск"
            )
          }
          // Number input button (keyboard)
          IconButton(onClick = onNumberInputClick) {
            Icon(
              Icons.Default.Keyboard,
              contentDescription = "Перейти по номеру"
            )
          }
          // Font size controls
          IconButton(
            onClick = { fontScale = (fontScale - 0.1f).coerceAtLeast(0.7f) }
          ) {
            Icon(
              Icons.Default.TextDecrease,
              contentDescription = "Уменьшить шрифт"
            )
          }
          IconButton(
            onClick = { fontScale = (fontScale + 0.1f).coerceAtMost(1.5f) }
          ) {
            Icon(
              Icons.Default.TextIncrease,
              contentDescription = "Увеличить шрифт"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      if (state is SongDetailUiState.Content) {
        FloatingActionButton(
          onClick = {
            isFavorite = !isFavorite
            onFavoriteClick()
          },
          containerColor = if (isFavorite)
            MaterialTheme.colorScheme.primaryContainer
          else
            MaterialTheme.colorScheme.secondaryContainer
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
            tint = if (isFavorite)
              MaterialTheme.colorScheme.primary
            else
              MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }
    }
  ) { innerPadding ->
    when (state) {
      SongDetailUiState.Loading -> {
        LoadingContent(modifier = Modifier.padding(innerPadding))
      }
      is SongDetailUiState.Content -> {
        SongContent(
          song = state.song,
          fontScale = fontScale,
          modifier = Modifier.padding(innerPadding)
        )
      }
      SongDetailUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = "Песня не найдена",
          message = "Возможно, она была удалена или перемещена"
        )
      }
    }
  }
}

@Composable
private fun SongContent(
  song: SongDetail,
  fontScale: Float,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 600.dp)
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Song header
      SongHeader(song = song)

      Spacer(Modifier.height(MaterialTheme.spacing.xl))

      // Lyric content - centered
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        LyricContent(
          lyric = song.lyric,
          fontScale = fontScale
        )
      }

      // Metadata footer
      if (song.author != null || song.composer != null || song.translator != null) {
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        SongMetadata(song = song)
      }

      // Bottom padding for FAB and scrolling
      Spacer(Modifier.height(100.dp))
    }
  }
}

@Composable
private fun SongHeader(song: SongDetail) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = song.name.value,
      style = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold
      ),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )

    song.tonalities?.takeIf { it.isNotEmpty() }?.let { tonalities ->
      Spacer(Modifier.height(MaterialTheme.spacing.sm))
      Text(
        text = tonalities.joinToString(" • ") { it.identifier },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun LyricContent(
  lyric: List<LyricPart>,
  fontScale: Float
) {
  val baseTextSize = 18.sp * fontScale
  val chorusTextSize = 17.sp * fontScale

  Column(
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
  ) {
    lyric.forEach { part ->
      LyricPartView(
        part = part,
        baseTextSize = baseTextSize,
        chorusTextSize = chorusTextSize
      )
    }
  }
}

@Composable
private fun LyricPartView(
  part: LyricPart,
  baseTextSize: androidx.compose.ui.unit.TextUnit,
  chorusTextSize: androidx.compose.ui.unit.TextUnit
) {
  when (part) {
    is Verse -> {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Verse number
        if (part.numbers.isNotEmpty()) {
          Text(
            text = part.numbers.joinToString(", "),
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(Modifier.height(MaterialTheme.spacing.xs))
        }
        Text(
          text = part.text.trim(),
          style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = baseTextSize,
            lineHeight = baseTextSize * 1.5f
          ),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
      }
    }

    is Chorus -> {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
      ) {
        Column(
          modifier = Modifier.padding(MaterialTheme.spacing.md),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(4.dp, 20.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Text(
              text = "Припев",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
              ),
              color = MaterialTheme.colorScheme.primary
            )
          }
          Spacer(Modifier.height(MaterialTheme.spacing.sm))
          Text(
            text = part.text.trim(),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = chorusTextSize,
              lineHeight = chorusTextSize * 1.5f,
              fontStyle = FontStyle.Italic
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    is Bridge -> {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
      ) {
        Column(
          modifier = Modifier.padding(MaterialTheme.spacing.md),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Бридж",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.tertiary
          )
          Spacer(Modifier.height(MaterialTheme.spacing.sm))
          Text(
            text = part.text.trim(),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = baseTextSize,
              lineHeight = baseTextSize * 1.5f
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

@Composable
private fun SongMetadata(song: SongDetail) {
  Column(
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    song.author?.let { author ->
      MetadataRow(label = "Автор", value = author.name)
    }
    song.composer?.let { composer ->
      MetadataRow(label = "Композитор", value = composer.name)
    }
    song.translator?.let { translator ->
      MetadataRow(label = "Перевод", value = translator.name)
    }
    song.year?.let { year ->
      MetadataRow(label = "Год", value = year.toString())
    }
  }
}

@Composable
private fun MetadataRow(label: String, value: String) {
  Row {
    Text(
      text = "$label: ",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Medium
      ),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

