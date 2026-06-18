package io.github.alelk.pws.features.app

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import io.github.alelk.pws.features.books.BooksScreen
import io.github.alelk.pws.features.components.AppNavigationBar
import io.github.alelk.pws.features.components.LocalTabReselectEvents
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.components.TabReselectEvents
import kotlinx.coroutines.launch
import io.github.alelk.pws.features.favorites.FavoritesScreen
import io.github.alelk.pws.features.history.HistoryScreen
import io.github.alelk.pws.features.home.HomeScreen
import io.github.alelk.pws.features.search.SearchScreen
import io.github.alelk.pws.features.settings.LocalSettingsExternalActions
import io.github.alelk.pws.features.settings.SettingsExternalActions
import io.github.alelk.pws.features.song.detail.FavoritesDisplaySettings
import io.github.alelk.pws.features.song.detail.LocalFavoritesDisplaySettings
import io.github.alelk.pws.features.song.detail.LocalSongDetailDisplaySettings
import io.github.alelk.pws.features.song.detail.LocalSongDetailExternalActions
import io.github.alelk.pws.features.song.detail.SongDetailDisplaySettings
import io.github.alelk.pws.features.song.detail.SongDetailExternalActions
import io.github.alelk.pws.features.tags.TagsScreen
import io.github.alelk.pws.features.theme.AppTheme
import io.github.alelk.pws.features.theme.LocalThemeSettings
import io.github.alelk.pws.features.theme.ThemeMode
import io.github.alelk.pws.features.theme.ThemeSettings

/**
 * Root composable for the app with theme and main navigation.
 */
@Composable
fun AppRoot(
  themeMode: ThemeMode = ThemeMode.DEFAULT,
  appVersion: String? = null,
  onThemeModeChange: (ThemeMode) -> Unit = {},
  useDynamicColor: Boolean = false,
  onUseDynamicColorChange: (Boolean) -> Unit = {},
  keepScreenOn: Boolean = false,
  onKeepScreenOnChange: (Boolean) -> Unit = {},
  settingsExternalActions: SettingsExternalActions? = null,
  songDetailExternalActions: SongDetailExternalActions? = null,
  songDetailDisplaySettings: SongDetailDisplaySettings? = null,
  favoritesDisplaySettings: FavoritesDisplaySettings? = null,
) {
  CompositionLocalProvider(
    LocalThemeSettings provides ThemeSettings(
      themeMode = themeMode,
      onThemeModeChange = onThemeModeChange,
      useDynamicColor = useDynamicColor,
      onUseDynamicColorChange = onUseDynamicColorChange,
      keepScreenOn = keepScreenOn,
      onKeepScreenOnChange = onKeepScreenOnChange,
    ),
    LocalPwsAppInfo provides appVersion?.let { PwsAppInfo(it) },
    LocalSettingsExternalActions provides settingsExternalActions,
    LocalSongDetailExternalActions provides songDetailExternalActions,
    LocalSongDetailDisplaySettings provides songDetailDisplaySettings,
    LocalFavoritesDisplaySettings provides favoritesDisplaySettings,
  ) {
    AppTheme(themeMode = themeMode, useDynamicColor = useDynamicColor) {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
      ) {
        MainScreen()
      }
    }
  }
}

/**
 * One Tab implementation parameterised by [NavDestination]. Each tab owns its own
 * nested back-stack [Navigator]; the [LocalTabNavigatorsHolder] lets the bottom-nav
 * "reselect current tab" handler pop that nested stack to root.
 */
private class DestinationTab(
  val destination: NavDestination,
  private val tabIndex: UShort,
) : Tab {
  override val key: String = "tab/${destination.route}"

  override val options: TabOptions
    @Composable get() = TabOptions(
      index = tabIndex,
      title = destination.route,
      icon = rememberVectorPainter(destination.unselectedIcon),
    )

  @Composable
  override fun Content() {
    val holder = LocalTabNavigatorsHolder.currentOrThrow
    Navigator(initialScreen()) { navigator ->
      holder.navigators[this] = navigator
      SlideTransition(navigator)
    }
  }

  private fun initialScreen(): Screen = when (destination) {
    NavDestination.Home -> HomeScreen()
    NavDestination.Books -> BooksScreen()
    NavDestination.Search -> SearchScreen()
    NavDestination.Tags -> TagsScreen()
    NavDestination.Favorites -> FavoritesScreen()
    NavDestination.History -> HistoryScreen()
  }
}

private val mainTabs: List<Tab> = listOf(
  DestinationTab(NavDestination.Home, 0u),
  DestinationTab(NavDestination.Books, 1u),
  DestinationTab(NavDestination.Search, 2u),
  DestinationTab(NavDestination.Tags, 3u),
  DestinationTab(NavDestination.Favorites, 4u),
  DestinationTab(NavDestination.History, 5u),
)

private class TabNavigatorsHolder(
  val navigators: MutableMap<Tab, Navigator>,
)

private val LocalTabNavigatorsHolder = staticCompositionLocalOf<TabNavigatorsHolder?> { null }

@Composable
private fun MainScreen() {
  val holder = remember { TabNavigatorsHolder(mutableMapOf()) }
  val reselectEvents = remember { TabReselectEvents() }
  val scope = rememberCoroutineScope()

  TabNavigator(mainTabs.first()) { tabNavigator ->
    CompositionLocalProvider(
      LocalTabNavigatorsHolder provides holder,
      LocalTabReselectEvents provides reselectEvents,
    ) {
      Scaffold(
        bottomBar = {
          AppNavigationBar(
            tabs = mainTabs,
            currentTab = tabNavigator.current,
            onTabSelected = { tabNavigator.current = it },
            onReselectCurrentTab = {
              val current = tabNavigator.current
              val nav = holder.navigators[current]
              // If there's a pushed stack — first pop to root. Otherwise emit reselect so
              // root screen can scroll-to-top + expand large top bar (iOS-like).
              if (nav != null && nav.canPop) {
                nav.popUntilRoot()
              } else {
                val destination = (current as? DestinationTab)?.destination
                if (destination != null) {
                  scope.launch { reselectEvents.emit(destination) }
                }
              }
            }
          )
        }
      ) { innerPadding ->
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
        ) {
          CurrentTab()
        }
      }
    }
  }
}
