package io.github.alelk.pws.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
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

/**
 * Navigation destinations for the app.
 */
enum class NavDestination(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  Books(
    route = "books",
    label = "Сборники",
    selectedIcon = Icons.AutoMirrored.Filled.LibraryBooks,
    unselectedIcon = Icons.AutoMirrored.Outlined.LibraryBooks
  ),
  Tags(
    route = "tags",
    label = "Теги",
    selectedIcon = Icons.Filled.Tag,
    unselectedIcon = Icons.Outlined.Tag
  ),
  Search(
    route = "search",
    label = "Поиск",
    selectedIcon = Icons.Filled.Search,
    unselectedIcon = Icons.Outlined.Search
  ),
  Favorites(
    route = "favorites",
    label = "Избранное",
    selectedIcon = Icons.Filled.Favorite,
    unselectedIcon = Icons.Outlined.FavoriteBorder
  ),
  History(
    route = "history",
    label = "История",
    selectedIcon = Icons.Filled.History,
    unselectedIcon = Icons.Outlined.History
  )
}

/**
 * Bottom navigation bar for main app navigation.
 */
@Composable
fun AppNavigationBar(
  currentDestination: NavDestination,
  onDestinationSelected: (NavDestination) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.surfaceContainer
  ) {
    NavDestination.entries.forEach { destination ->
      val selected = currentDestination == destination
      NavigationBarItem(
        selected = selected,
        onClick = { onDestinationSelected(destination) },
        icon = {
          Icon(
            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = destination.label
          )
        },
        label = {
          Text(
            text = destination.label,
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

