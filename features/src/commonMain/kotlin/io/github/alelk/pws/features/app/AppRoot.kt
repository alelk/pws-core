package io.github.alelk.pws.features.app

import androidx.compose.foundation.isSystemInDarkTheme
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
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import io.github.alelk.pws.features.books.BooksScreen
import io.github.alelk.pws.features.components.AppNavigationBar
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.favorites.FavoritesScreen
import io.github.alelk.pws.features.history.HistoryScreen
import io.github.alelk.pws.features.search.SearchScreen
import io.github.alelk.pws.features.tags.TagsScreen
import io.github.alelk.pws.features.theme.AppTheme
import io.github.alelk.pws.features.home.HomeScreen

/**
 * Root composable for the app with theme and main navigation.
 */
@Composable
fun AppRoot(
  useDarkTheme: Boolean? = null // null = follow system
) {
  val resolvedDark = useDarkTheme ?: isSystemInDarkTheme()
  AppTheme(
    useDarkTheme = resolvedDark
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      MainScreen()
    }
  }
}

private object HomeTab : Tab {
  override val options: TabOptions
    @Composable get() {
      return TabOptions(
        index = 0u,
        title = "Главная",
        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Outlined.Home)
      )
    }

  @Composable
  override fun Content() {
    Navigator(HomeScreen())
  }
}

private object BooksTab : Tab {
  override val options: TabOptions
    @Composable get() {
      val icon = NavDestination.Books.unselectedIcon
      return TabOptions(
        index = 1u,
        title = NavDestination.Books.label,
        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon)
      )
    }

  @Composable
  override fun Content() {
    Navigator(BooksScreen())
  }
}

private object SearchTab : Tab {
  override val options: TabOptions
    @Composable get() {
      val icon = NavDestination.Search.unselectedIcon
      return TabOptions(
        index = 2u,
        title = NavDestination.Search.label,
        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon)
      )
    }

  @Composable
  override fun Content() {
    Navigator(SearchScreen())
  }
}

private object FavoritesTab : Tab {
  override val options: TabOptions
    @Composable get() {
      val icon = NavDestination.Favorites.unselectedIcon
      return TabOptions(
        index = 3u,
        title = NavDestination.Favorites.label,
        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon)
      )
    }

  @Composable
  override fun Content() {
    Navigator(FavoritesScreen())
  }
}

private object HistoryTab : Tab {
  override val options: TabOptions
    @Composable get() {
      val icon = NavDestination.History.unselectedIcon
      return TabOptions(
        index = 4u,
        title = NavDestination.History.label,
        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon)
      )
    }

  @Composable
  override fun Content() {
    Navigator(HistoryScreen())
  }
}

@Suppress("unused")
private val mainTabs: List<Tab> = listOf(
  HomeTab,
  BooksTab,
  SearchTab,
  FavoritesTab,
  HistoryTab
)

@Composable
private fun MainScreen() {
  TabNavigator(HomeTab) { tabNavigator ->
    Scaffold(
      bottomBar = {
        AppNavigationBar(
          tabs = mainTabs,
          currentTab = tabNavigator.current,
          onTabSelected = { tabNavigator.current = it }
        )
      }
    ) { innerPadding ->
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        CurrentTab()
      }
    }
  }
}
