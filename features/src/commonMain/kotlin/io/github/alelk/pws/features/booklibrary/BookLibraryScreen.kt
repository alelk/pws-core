package io.github.alelk.pws.features.booklibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.model.DownloadState
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.book_library_action_install
import io.github.alelk.pws.features.resources.book_library_action_uninstall
import io.github.alelk.pws.features.resources.book_library_downloading
import io.github.alelk.pws.features.resources.book_library_error_message
import io.github.alelk.pws.features.resources.book_library_error_title
import io.github.alelk.pws.features.resources.book_library_loading
import io.github.alelk.pws.features.resources.book_library_retry
import io.github.alelk.pws.features.resources.book_library_size
import io.github.alelk.pws.features.resources.book_library_songs_count
import io.github.alelk.pws.features.resources.book_library_status_built_in
import io.github.alelk.pws.features.resources.book_library_status_installed
import io.github.alelk.pws.features.resources.book_library_title
import io.github.alelk.pws.features.resources.book_library_uninstall_confirm_cancel
import io.github.alelk.pws.features.resources.book_library_uninstall_confirm_message
import io.github.alelk.pws.features.resources.book_library_uninstall_confirm_ok
import io.github.alelk.pws.features.resources.book_library_uninstall_confirm_title
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

class BookLibraryScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<BookLibraryScreenModel>()
        val state by viewModel.state.collectAsState()
        BookLibraryContent(
            state = state,
            onRetry = viewModel::retry,
            onInstall = viewModel::install,
            onUninstall = viewModel::uninstall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLibraryContent(
    state: BookLibraryUiState,
    onRetry: () -> Unit = {},
    onInstall: (BookCatalogEntry) -> Unit = {},
    onUninstall: (BookId) -> Unit = {},
) {
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.book_library_title)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        when (state) {
            BookLibraryUiState.Loading -> LoadingContent(
                message = stringResource(Res.string.book_library_loading),
                modifier = Modifier.padding(innerPadding),
            )
            is BookLibraryUiState.Error -> ErrorContent(
                title = stringResource(Res.string.book_library_error_title),
                message = stringResource(Res.string.book_library_error_message),
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding),
            )
            is BookLibraryUiState.Content -> BookLibraryList(
                items = state.items,
                onInstall = onInstall,
                onUninstall = onUninstall,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun BookLibraryList(
    items: List<BookLibraryItem>,
    onInstall: (BookCatalogEntry) -> Unit,
    onUninstall: (BookId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var uninstallTarget by remember { mutableStateOf<BookId?>(null) }

    if (uninstallTarget != null) {
        AlertDialog(
            onDismissRequest = { uninstallTarget = null },
            title = { Text(stringResource(Res.string.book_library_uninstall_confirm_title)) },
            text = { Text(stringResource(Res.string.book_library_uninstall_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    uninstallTarget?.let { onUninstall(it) }
                    uninstallTarget = null
                }) { Text(stringResource(Res.string.book_library_uninstall_confirm_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { uninstallTarget = null }) {
                    Text(stringResource(Res.string.book_library_uninstall_confirm_cancel))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.screenHorizontal,
            vertical = MaterialTheme.spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        items(items, key = { it.bookId.toString() }) { item ->
            BookLibraryCard(
                item = item,
                onInstall = { onInstall(item.entry) },
                onUninstall = { uninstallTarget = item.bookId },
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun BookLibraryCard(
    item: BookLibraryItem,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.entry.displayName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    text = stringResource(Res.string.book_library_songs_count, item.entry.songCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                val bytes = item.entry.fileSizeBytes
                val sizeMb = "${bytes / 1_048_576}.${(bytes % 1_048_576) * 10 / 1_048_576}"
                Text(
                    text = stringResource(Res.string.book_library_size, sizeMb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            when (val ds = item.downloadState) {
                is DownloadState.Downloading -> {
                    Text(
                        text = stringResource(Res.string.book_library_downloading),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ds.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is DownloadState.Error -> {
                    Text(
                        text = ds.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(4.dp))
                    BookLibraryActions(item, onInstall, onUninstall)
                }
                else -> BookLibraryActions(item, onInstall, onUninstall)
            }
        }
    }
}

@Composable
private fun BookLibraryActions(
    item: BookLibraryItem,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        when {
            item.isBuiltIn -> Text(
                text = stringResource(Res.string.book_library_status_built_in),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            item.isInstalled -> {
                Text(
                    text = stringResource(Res.string.book_library_status_installed),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onUninstall,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(Res.string.book_library_action_uninstall))
                }
            }
            else -> Button(onClick = onInstall) {
                Text(stringResource(Res.string.book_library_action_install))
            }
        }
    }
}
