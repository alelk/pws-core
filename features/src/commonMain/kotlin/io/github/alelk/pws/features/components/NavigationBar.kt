package io.github.alelk.pws.features.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.navigator.tab.Tab
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LibraryBig
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.nav_books
import io.github.alelk.pws.features.resources.nav_home
import io.github.alelk.pws.features.resources.nav_library
import io.github.alelk.pws.features.resources.nav_search
import io.github.alelk.pws.features.theme.Motion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Navigation destinations for the app.
 */
enum class NavDestination(
  val route: String,
  val labelRes: StringResource,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  Home(
    route = "home",
    labelRes = Res.string.nav_home,
    selectedIcon = Lucide.House,
    unselectedIcon = Lucide.House
  ),
  Books(
    route = "books",
    labelRes = Res.string.nav_books,
    selectedIcon = Lucide.LibraryBig,
    unselectedIcon = Lucide.LibraryBig
  ),
  Search(
    route = "search",
    labelRes = Res.string.nav_search,
    selectedIcon = Lucide.Search,
    unselectedIcon = Lucide.Search
  ),
  Library(
    route = "library",
    labelRes = Res.string.nav_library,
    selectedIcon = Lucide.Bookmark,
    unselectedIcon = Lucide.Bookmark
  )
}

/**
 * Cross-screen bus: a repeated tap on the already-active tab.
 *
 * Root screens subscribe to their [NavDestination], scroll their
 * LazyList back to the top and expand the LargeTopAppBar.
 */
class TabReselectEvents {
  private val _events = MutableSharedFlow<NavDestination>(extraBufferCapacity = 1)

  /** Full event stream for subscribers. */
  fun events(): Flow<NavDestination> = _events.asSharedFlow()

  /** Stream for a single tab — simpler than filtering in every screen. */
  fun forDestination(destination: NavDestination): Flow<Unit> =
    _events.asSharedFlow().filter { it == destination }.map { }

  suspend fun emit(destination: NavDestination) {
    _events.emit(destination)
  }
}

/** Non-null when the screen runs inside [MainScreen]; null otherwise. */
val LocalTabReselectEvents = compositionLocalOf<TabReselectEvents?> { null }

/**
 * Subscribes to the reselect event of a single tab.
 * iOS analog: tapping the active tab again scrolls to top and expands the large title.
 */
@Composable
fun OnTabReselected(destination: NavDestination, onReselect: suspend () -> Unit) {
  val events = LocalTabReselectEvents.current
  LaunchedEffect(events, destination) {
    events?.forDestination(destination)?.collect { onReselect() }
  }
}

/**
 * Bottom navigation bar for main app navigation.
 *
 * Important: we pass the explicit list of tabs so the component can map UI items -> concrete Tab instances.
 */
@Composable
fun AppNavigationBar(
  tabs: List<Tab>,
  currentTab: Tab,
  onTabSelected: (Tab) -> Unit,
  onReselectCurrentTab: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  Column(modifier = modifier) {
    // Hairline instead of a tonal block: the bar is the page, separated by a line.
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    NavigationBar(
      containerColor = MaterialTheme.colorScheme.background
    ) {
      tabs.forEach { tab ->
        val destination = NavDestination.entries.firstOrNull { it.route == tab.options.title }
        val label = destination?.let { stringResource(it.labelRes) } ?: tab.options.title

        val selected = currentTab.options.index == tab.options.index

        // Spring-based selection feedback: icon scales slightly + tint fades.
        // No pill indicator — the accent tint and label weight carry the state.
        val iconScale by animateFloatAsState(
          targetValue = if (selected) 1.08f else 1f,
          animationSpec = Motion.emphasized(),
          label = "nav-icon-scale",
        )
        val selectedIconColor = MaterialTheme.colorScheme.primary
        val unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        val iconTint by animateColorAsState(
          targetValue = if (selected) selectedIconColor else unselectedIconColor,
          animationSpec = Motion.standard(),
          label = "nav-icon-tint",
        )

        NavigationBarItem(
          selected = selected,
          modifier = Modifier.testTag(destination?.route ?: label),
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (selected) {
              onReselectCurrentTab()
            } else {
              onTabSelected(tab)
            }
          },
          icon = {
            val icons = destination
            Icon(
              imageVector = if (selected) (icons?.selectedIcon ?: NavDestination.Home.selectedIcon)
              else (icons?.unselectedIcon ?: NavDestination.Home.unselectedIcon),
              contentDescription = label,
              tint = iconTint,
              modifier = Modifier.scale(iconScale),
            )
          },
          label = {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (selected) FontWeight.W600 else FontWeight.W500
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedIconColor,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = Color.Transparent,
            unselectedIconColor = unselectedIconColor,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
    }
  }
}
