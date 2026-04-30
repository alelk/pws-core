package io.github.alelk.pws.features.song.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A pager component that adapts its implementation based on the platform.
 * - Mobile (Android/iOS): Uses [HorizontalPager] for native swipe support.
 * - Others (Desktop/Web): Uses [AnimatedContent] with manual navigation (swipe disabled).
 */
@Composable
expect fun SongPager(
  pageCount: Int,
  currentPage: Int,
  onPageChanged: (Int) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable (page: Int, onNavigatePrev: (() -> Unit)?, onNavigateNext: (() -> Unit)?) -> Unit
)
