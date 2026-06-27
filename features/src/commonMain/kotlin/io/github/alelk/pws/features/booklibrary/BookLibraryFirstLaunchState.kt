package io.github.alelk.pws.features.booklibrary

interface BookLibraryFirstLaunchState {
    suspend fun shouldShow(): Boolean
    fun markShown()
}
