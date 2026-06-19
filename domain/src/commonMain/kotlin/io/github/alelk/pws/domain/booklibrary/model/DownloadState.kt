package io.github.alelk.pws.domain.booklibrary.model

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val downloaded: Long, val total: Long) : DownloadState() {
        val progress: Float get() = if (total > 0) downloaded.toFloat() / total else 0f
    }
    data object Done : DownloadState()
    data class Error(val message: String) : DownloadState()
}
