package io.github.alelk.pws.features.song.detail

/**
 * Default visibility of the prev/next buttons in the song header.
 *
 * Native mobile has swipe paging, so the buttons are hidden by default there.
 * Skiko targets (desktop, web incl. the Telegram Mini App) page via
 * AnimatedContent without swipe, so the buttons stay on.
 * The "Aa" sheet toggle overrides this in either direction.
 */
expect val PlatformDefaultShowSongNavButtons: Boolean
