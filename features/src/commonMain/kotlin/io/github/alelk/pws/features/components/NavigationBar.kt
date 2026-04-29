package io.github.alelk.pws.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.navigator.tab.Tab
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.nav_books
import io.github.alelk.pws.features.resources.nav_favorites
import io.github.alelk.pws.features.resources.nav_history
import io.github.alelk.pws.features.resources.nav_home
import io.github.alelk.pws.features.resources.nav_search
import io.github.alelk.pws.features.resources.nav_tags
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
    selectedIcon = Icons.Filled.Home,
    unselectedIcon = Icons.Outlined.Home
  ),
  Books(
    route = "books",
    labelRes = Res.string.nav_books,
    selectedIcon = Icons.AutoMirrored.Filled.LibraryBooks,
    unselectedIcon = Icons.AutoMirrored.Outlined.LibraryBooks
  ),
  Tags(
    route = "tags",
    labelRes = Res.string.nav_tags,
    selectedIcon = Icons.Filled.Tag,
    unselectedIcon = Icons.Outlined.Tag
  ),
  Search(
    route = "search",
    labelRes = Res.string.nav_search,
    selectedIcon = Icons.Filled.Search,
    unselectedIcon = Icons.Outlined.Search
  ),
  Favorites(
    route = "favorites",
    labelRes = Res.string.nav_favorites,
    selectedIcon = Icons.Filled.Favorite,
    unselectedIcon = Icons.Outlined.FavoriteBorder
  ),
  History(
    route = "history",
    labelRes = Res.string.nav_history,
    selectedIcon = Icons.Filled.History,
    unselectedIcon = Icons.Outlined.History
  )
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
  NavigationBar(
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.surfaceContainer
  ) {
    tabs.forEach { tab ->
      val destination = NavDestination.entries.firstOrNull { it.route == tab.options.title }
      val label = destination?.let { stringResource(it.labelRes) } ?: tab.options.title

      val selected = currentTab.options.index == tab.options.index

      NavigationBarItem(
        selected = selected,
        onClick = {
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
            contentDescription = label
          )
        },
        label = {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
    }
  }
}
