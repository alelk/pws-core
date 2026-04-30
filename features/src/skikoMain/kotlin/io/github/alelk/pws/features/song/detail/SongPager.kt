package io.github.alelk.pws.features.song.detail

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
actual fun SongPager(
  pageCount: Int,
  currentPage: Int,
  onPageChanged: (Int) -> Unit,
  modifier: Modifier,
  content: @Composable (page: Int, onNavigatePrev: (() -> Unit)?, onNavigateNext: (() -> Unit)?) -> Unit
) {
  var lastPage by remember { mutableIntStateOf(currentPage) }
  val navigatingForward = currentPage >= lastPage

  LaunchedEffect(currentPage) {
    lastPage = currentPage
  }

  fun goToPrev() {
    if (currentPage > 0) onPageChanged(currentPage - 1)
  }

  fun goToNext() {
    if (currentPage < pageCount - 1) onPageChanged(currentPage + 1)
  }

  AnimatedContent(
    targetState = currentPage,
    transitionSpec = {
      if (navigatingForward) {
        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
      } else {
        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
      }
    },
    modifier = modifier
  ) { page ->
    content(
      page,
      if (page > 0) ::goToPrev else null,
      if (page < pageCount - 1) ::goToNext else null
    )
  }
}
