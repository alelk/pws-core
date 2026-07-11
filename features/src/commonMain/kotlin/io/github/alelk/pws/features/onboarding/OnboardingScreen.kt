package io.github.alelk.pws.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import io.github.alelk.pws.domain.booklibrary.model.DownloadState
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.features.booklibrary.BookLibraryItem
import io.github.alelk.pws.features.booklibrary.BookLibraryScreenModel
import io.github.alelk.pws.features.booklibrary.BookLibraryUiState
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.book_library_error_message
import io.github.alelk.pws.features.resources.book_library_error_title
import io.github.alelk.pws.features.resources.book_library_loading
import io.github.alelk.pws.features.resources.book_library_recommended
import io.github.alelk.pws.features.resources.book_library_size
import io.github.alelk.pws.features.resources.book_library_songs_count
import io.github.alelk.pws.features.resources.onboarding_continue
import io.github.alelk.pws.features.resources.onboarding_install_selected
import io.github.alelk.pws.features.resources.onboarding_skip
import io.github.alelk.pws.features.resources.onboarding_subtitle
import io.github.alelk.pws.features.resources.onboarding_title
import io.github.alelk.pws.features.theme.spacing
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.stringResource

class OnboardingScreen(private val onSkip: () -> Unit) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<BookLibraryScreenModel>()
        val state by viewModel.state.collectAsState()

        val selectedIds: SnapshotStateSet<BookId> = remember { mutableStateSetOf() }
        var preselected by remember { mutableStateOf(false) }

        // Pre-select locale-recommended book on first successful load
        LaunchedEffect(state) {
            if (!preselected && state is BookLibraryUiState.Content) {
                (state as BookLibraryUiState.Content).recommendedBookId?.let { selectedIds.add(it) }
                preselected = true
            }
        }

        OnboardingContent(
            state = state,
            selectedIds = selectedIds,
            onToggle = { id -> if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id) },
            onInstallSelected = {
                if (state is BookLibraryUiState.Content) {
                    (state as BookLibraryUiState.Content).items
                        .filter { it.bookId in selectedIds && !it.isInstalled && it.downloadState !is DownloadState.Downloading }
                        .forEach { viewModel.install(it.entry) }
                }
            },
            onRetry = viewModel::retry,
            onSkip = onSkip,
        )
    }
}

@Composable
private fun OnboardingContent(
    state: BookLibraryUiState,
    selectedIds: Set<BookId>,
    onToggle: (BookId) -> Unit,
    onInstallSelected: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
) {
    val installableCount = if (state is BookLibraryUiState.Content) {
        state.items.count {
            it.bookId in selectedIds && !it.isInstalled && it.downloadState !is DownloadState.Downloading
        }
    } else 0

    val anyInstalled = state is BookLibraryUiState.Content && state.items.any { it.isInstalled }
    val anyDownloading = state is BookLibraryUiState.Content && state.items.any { it.downloadState is DownloadState.Downloading }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = state !is BookLibraryUiState.Loading, enter = fadeIn()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (installableCount > 0 || anyDownloading) {
                        Button(
                            onClick = onInstallSelected,
                            enabled = installableCount > 0 && !anyDownloading,
                            modifier = Modifier.fillMaxWidth().testTag("action:install-selected-books"),
                        ) {
                            Text(stringResource(Res.string.onboarding_install_selected, installableCount))
                        }
                    }
                    TextButton(onClick = onSkip, modifier = Modifier.testTag("action:skip-onboarding")) {
                        Text(
                            if (anyInstalled && !anyDownloading) stringResource(Res.string.onboarding_continue)
                            else stringResource(Res.string.onboarding_skip)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.screenHorizontal,
                vertical = MaterialTheme.spacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.onboarding_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Text(
                        text = stringResource(Res.string.onboarding_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            when (state) {
                is BookLibraryUiState.Loading -> item {
                    LoadingContent(message = stringResource(Res.string.book_library_loading))
                }
                is BookLibraryUiState.Error -> item {
                    ErrorContent(
                        title = stringResource(Res.string.book_library_error_title),
                        message = stringResource(Res.string.book_library_error_message),
                        onRetry = onRetry,
                    )
                }
                is BookLibraryUiState.Content -> items(
                    items = state.items,
                    key = { it.bookId.toString() },
                ) { item ->
                    OnboardingBookCard(
                        item = item,
                        isRecommended = item.bookId == state.recommendedBookId,
                        isSelected = item.bookId in selectedIds,
                        onToggle = { onToggle(item.bookId) },
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun OnboardingBookCard(
    item: BookLibraryItem,
    isRecommended: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val selectable = !item.isInstalled && item.downloadState !is DownloadState.Downloading
    val downloading = item.downloadState as? DownloadState.Downloading
    val error = item.downloadState as? DownloadState.Error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (selectable) Modifier.clickable(onClick = onToggle) else Modifier),
        colors = if (error != null)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
        else
            CardDefaults.cardColors(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                when {
                    item.isInstalled -> Icon(
                        imageVector = Lucide.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    downloading != null -> {
                        if (downloading.total > 0) {
                            LinearProgressIndicator(
                                progress = { downloading.downloaded.toFloat() / downloading.total },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            )
                        }
                    }
                    error != null -> Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.error,
                            uncheckedColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                    else -> Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggle() },
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.entry.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isRecommended) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = stringResource(Res.string.book_library_recommended),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
                val tenths = (item.entry.fileSizeBytes / 100_000.0).roundToLong()
                val sizeMb = "${tenths / 10}.${tenths % 10}"
                Text(
                    text = "${stringResource(Res.string.book_library_songs_count, item.entry.songCount)}  ·  ${stringResource(Res.string.book_library_size, sizeMb)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (error != null) {
                    Text(
                        text = error.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
