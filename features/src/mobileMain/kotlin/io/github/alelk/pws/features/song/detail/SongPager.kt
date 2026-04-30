package io.github.alelk.pws.features.song.detail

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
actual fun SongPager(
  pageCount: Int,
  currentPage: Int,
  onPageChanged: (Int) -> Unit,
  modifier: Modifier,
  content: @Composable (page: Int, onNavigatePrev: (() -> Unit)?, onNavigateNext: (() -> Unit)?) -> Unit
) {
  val pagerState = rememberPagerState(initialPage = currentPage) { pageCount }
  val scope = rememberCoroutineScope()

  LaunchedEffect(currentPage) {
    if (pagerState.currentPage != currentPage) {
      pagerState.scrollToPage(currentPage)
    }
  }

  LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
    if (!pagerState.isScrollInProgress && pagerState.currentPage != currentPage) {
      onPageChanged(pagerState.currentPage)
    }
  }

  HorizontalPager(
    state = pagerState,
    modifier = modifier,
    userScrollEnabled = true
  ) { page ->
    content(
      page,
      if (page > 0) { { scope.launch { pagerState.animateScrollToPage(page - 1) } } } else null,
      if (page < pageCount - 1) { { scope.launch { pagerState.animateScrollToPage(page + 1) } } } else null
    )
  }
}
