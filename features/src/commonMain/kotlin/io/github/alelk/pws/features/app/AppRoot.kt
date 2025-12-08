package io.github.alelk.pws.features.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import io.github.alelk.pws.features.books.BooksScreen
import io.github.alelk.pws.features.components.AppNavigationBar
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.favorites.FavoritesScreen
import io.github.alelk.pws.features.history.HistoryScreen
import io.github.alelk.pws.features.search.SearchScreen
import io.github.alelk.pws.features.tags.TagsScreen
import io.github.alelk.pws.features.theme.AppTheme

/**
 * Root composable for the app with theme and main navigation.
 */
@Composable
fun AppRoot(
  useDarkTheme: Boolean? = null // null = follow system
) {
  AppTheme(
    useDarkTheme = useDarkTheme ?: false // isSystemInDarkTheme() will be used if null
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      MainScreen()
    }
  }
}

@Composable
private fun MainScreen() {
  var currentDestination by remember { mutableStateOf(NavDestination.Books) }

  Scaffold(
    bottomBar = {
      AppNavigationBar(
        currentDestination = currentDestination,
        onDestinationSelected = { destination ->
          currentDestination = destination
        }
      )
    }
  ) { innerPadding ->
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentDestination) {
        NavDestination.Books -> Navigator(BooksScreen())
        NavDestination.Tags -> Navigator(TagsScreen())
        NavDestination.Search -> Navigator(SearchScreen())
        NavDestination.Favorites -> Navigator(FavoritesScreen())
        NavDestination.History -> Navigator(HistoryScreen())
      }
    }
  }
}

