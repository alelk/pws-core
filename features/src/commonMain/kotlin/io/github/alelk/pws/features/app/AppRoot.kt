package io.github.alelk.pws.features.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.github.alelk.pws.features.books.BooksScreen
import io.github.alelk.pws.features.components.AppNavigationBar
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.favorites.FavoritesScreen
import io.github.alelk.pws.features.history.HistoryScreen
import io.github.alelk.pws.features.home.HomeScreen
import io.github.alelk.pws.features.search.SearchScreen
import io.github.alelk.pws.features.theme.AppTheme

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
    val holder = LocalTabNavigatorsHolder.currentOrThrow
    Navigator(HomeScreen()) { navigator ->
      holder.navigators[this] = navigator
      CurrentScreen()
    }
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
    val holder = LocalTabNavigatorsHolder.currentOrThrow
    Navigator(BooksScreen()) { navigator ->
      holder.navigators[this] = navigator
      CurrentScreen()
    }
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
    val holder = LocalTabNavigatorsHolder.currentOrThrow
    Navigator(SearchScreen()) { navigator ->
      holder.navigators[this] = navigator
      CurrentScreen()
    }
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
    val holder = LocalTabNavigatorsHolder.currentOrThrow
    Navigator(FavoritesScreen()) { navigator ->
      holder.navigators[this] = navigator
      CurrentScreen()
    }
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
    val holder = LocalTabNavigatorsHolder.currentOrThrow
    Navigator(HistoryScreen()) { navigator ->
      holder.navigators[this] = navigator
      CurrentScreen()
    }
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

private class TabNavigatorsHolder(
  val navigators: MutableMap<Tab, Navigator>
)

private val LocalTabNavigatorsHolder = staticCompositionLocalOf<TabNavigatorsHolder?> { null }

@Composable
private fun MainScreen() {
  // Holds navigators for each Tab's nested stack.
  val tabNavigators = remember { mutableStateMapOf<Tab, Navigator>() }
  val holder = remember { TabNavigatorsHolder(tabNavigators) }

  TabNavigator(HomeTab) { tabNavigator ->
    CompositionLocalProvider(LocalTabNavigatorsHolder provides holder) {
      Scaffold(
        bottomBar = {
          AppNavigationBar(
            tabs = mainTabs,
            currentTab = tabNavigator.current,
            onTabSelected = { tabNavigator.current = it },
            onReselectCurrentTab = {
              holder.navigators[tabNavigator.current]?.popUntilRoot()
            }
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
}
