package io.github.alelk.pws.features.tags

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val tagsScreenModule = screenModule {
  register<SharedScreens.Tags> { TagsScreen() }
}
