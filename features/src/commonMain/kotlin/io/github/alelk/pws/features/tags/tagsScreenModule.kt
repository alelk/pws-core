package io.github.alelk.pws.features.tags

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.tags.songs.TagSongsScreen

val tagsScreenModule = screenModule {
  register<SharedScreens.Tags> { TagsScreen() }
  register<SharedScreens.TagSongs> { provider -> TagSongsScreen(provider.tagId) }
}
