package io.github.alelk.pws.features.tags.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.features.components.EmptyContent
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.SongListItem
import io.github.alelk.pws.features.theme.spacing
import org.koin.core.parameter.parametersOf

class TagSongsScreen(val tagId: TagId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<TagSongsScreenModel>(parameters = { parametersOf(tagId) })
    val state by viewModel.state.collectAsState()

    TagSongsContent(state = state)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSongsContent(state: TagSongsUiState) {
  val navigator = LocalNavigator.currentOrThrow
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          when (state) {
            is TagSongsUiState.Content -> {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(state.tag.color)
                )
                Spacer(Modifier.width(MaterialTheme.spacing.sm))
                Text(
                  text = state.tag.name,
                  style = MaterialTheme.typography.headlineSmall
                )
              }
            }
            else -> Text("Песни по тегу")
          }
        },
        navigationIcon = {
          IconButton(onClick = { navigator.pop() }) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Назад"
            )
          }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
      )
    }
  ) { innerPadding ->
    when (state) {
      TagSongsUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = "Загрузка песен..."
        )
      }

      TagSongsUiState.Empty -> {
        EmptyContent(
          modifier = Modifier.padding(innerPadding),
          icon = Icons.Outlined.MusicOff,
          title = "Нет песен",
          subtitle = "К этому тегу пока не добавлено ни одной песни"
        )
      }

      is TagSongsUiState.Content -> {
        TagSongsList(
          songs = state.songs,
          modifier = Modifier.padding(innerPadding)
        )
      }

      is TagSongsUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = "Ошибка",
          message = state.message
        )
      }
    }
  }
}

@Composable
private fun TagSongsList(
  songs: List<TagSongUi>,
  modifier: Modifier = Modifier
) {
  val navigator = LocalNavigator.currentOrThrow

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = MaterialTheme.spacing.sm)
  ) {
    items(
      items = songs,
      key = { "${it.songNumberId.bookId}-${it.songNumberId.songId}" }
    ) { song ->
      val songScreen = rememberScreen(SharedScreens.Song(song.songNumberId))

      SongListItem(
        number = song.songNumber,
        title = song.songName,
        onClick = { navigator.push(songScreen) }
      )

      if (song != songs.last()) {
        HorizontalDivider(
          modifier = Modifier.padding(start = 72.dp),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
      }
    }

    item {
      Spacer(Modifier.height(80.dp))
    }
  }
}

