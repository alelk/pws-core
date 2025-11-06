package io.github.alelk.pws.features.song.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.song.lyric.Chorus
import io.github.alelk.pws.domain.song.model.SongDetail
import org.koin.core.parameter.parametersOf

class SongDetailScreen(val songNumberId: SongNumberId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SongDetailScreenModel>(parameters = { parametersOf(songNumberId) })
    val state by viewModel.state.collectAsState()
    SongDetail(state = state)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetail(state: SongDetailUiState) {
  val navigator = LocalNavigator.currentOrThrow
  Scaffold(topBar = {
    TopAppBar(
      title = { Text(if (state is SongDetailUiState.Content) state.song.name.value else "Песня") },
      navigationIcon = { IconButton(onClick = { navigator.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") } }
    )
  }) { innerPadding ->
    when (state) {
      SongDetailUiState.Loading -> Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
      is SongDetailUiState.Content -> SongDetailContent(state.song, Modifier.padding(innerPadding).fillMaxSize())
      SongDetailUiState.Error -> Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Не найдено") }
    }
  }
}

@Composable
private fun SongDetailContent(song: SongDetail, modifier: Modifier = Modifier) {
  val scroll = rememberScrollState()
  Column(
    modifier = modifier.verticalScroll(scroll).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    SongMeta(song)
    HorizontalDivider()
    SongLyric(song)
  }
}

@Composable
private fun SongMeta(song: SongDetail) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    song.author?.let { Text("Автор: ${it.name}") }
    song.composer?.let { Text("Композитор: ${it.name}") }
    song.translator?.let { Text("Перевод: ${it.name}") }
    song.tonalities?.takeIf { it.isNotEmpty() }?.let { list ->
      Text("Тональности: ${list.joinToString { it.identifier }}")
    }
  }
}

@Composable
private fun SongLyric(song: SongDetail) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    song.lyric.forEach { part ->
      Text(
        part.text.trim(),
        fontWeight = if (part is Chorus) FontWeight.SemiBold else FontWeight.Normal
      )
    }
  }
}
