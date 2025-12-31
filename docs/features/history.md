# View History

## Description

Automatic tracking of viewed songs.

## Addition Rules

1. Song is opened on screen
2. **10 seconds** of viewing passed
3. Song is added to history
4. On reopening — timestamp is updated

## Use Cases

### RecordSongViewUseCase
```kotlin
class RecordSongViewUseCase(
    private val historyRepository: HistoryWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(songNumberId: SongNumberId): Long =
        txRunner.inRwTransaction { historyRepository.recordView(songNumberId) }
}
```

### ObserveHistoryUseCase
```kotlin
class ObserveHistoryUseCase(
    private val historyRepository: HistoryObserveRepository
) {
    operator fun invoke(limit: Int? = null): Flow<List<HistoryEntryWithSongInfo>> =
        historyRepository.observeAll(limit)
}
```

### ClearHistoryUseCase
```kotlin
class ClearHistoryUseCase(
    private val historyRepository: HistoryWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(): Int =
        txRunner.inRwTransaction { historyRepository.clearAll() }
}
```

## Models

### HistoryEntry
```kotlin
data class HistoryEntry(
    val songNumberId: SongNumberId,
    val viewedAt: Instant
)
```

## Timer Implementation in ViewModel

```kotlin
class SongViewModel(
    private val recordSongView: RecordSongViewUseCase
) : ViewModel() {
    
    private var historyJob: Job? = null
    
    fun onSongOpened(songId: Long) {
        // Cancel previous timer
        historyJob?.cancel()
        
        // Start new 10 second timer
        historyJob = viewModelScope.launch {
            delay(10_000) // 10 seconds
            recordSongView(songId)
        }
    }
    
    fun onSongClosed() {
        historyJob?.cancel()
    }
    
    override fun onCleared() {
        historyJob?.cancel()
        super.onCleared()
    }
}
```

## Alternative: LaunchedEffect

```kotlin
@Composable
fun SongScreen(songId: Long) {
    val viewModel = koinViewModel<SongViewModel>()
    
    // History timer
    LaunchedEffect(songId) {
        delay(10_000)
        viewModel.addToHistory(songId)
    }
    
    // ... rest of UI
}
```

## UI Flow

```
┌─────────────────────────────────────────────┐
│             HistoryScreen                   │
├─────────────────────────────────────────────┤
│  ← History                                  │
├─────────────────────────────────────────────┤
│  Today                                      │
│  ┌───────────────────────────────────────┐  │
│  │ GS 45 - Blessed Be the Lord           │  │
│  │ 14:30                                 │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ SV 12 - Grace                         │  │
│  │ 14:15                                 │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Yesterday                                  │
│  ┌───────────────────────────────────────┐  │
│  │ HP 7 - Great God                      │  │
│  │ 20:45                                 │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

- Deletion is done via context menu or swipe left.
- Menu should also have "Delete all" option.

## Related Files

- `domain/history/model/HistoryEntry.kt`
- `domain/history/repository/HistoryObserveRepository.kt`
- `domain/history/repository/HistoryWriteRepository.kt`
- `domain/history/usecase/*.kt`
- `features/history/HistoryScreen.kt`
- `features/history/HistoryScreenModel.kt`
- `features/history/HistoryUiState.kt`
