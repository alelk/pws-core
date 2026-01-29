# View History

## Description

Automatic tracking of viewed songs. Supports two types of history entries:
- **Booked songs**: Songs viewed in context of a specific book (with book ID and song number)
- **Standalone songs**: Songs viewed without book context (only song ID)

## Addition Rules

1. Song is opened on screen
2. **10 seconds** of viewing passed
3. Song is added to history
4. On reopening — timestamp is updated and view count is incremented

## Use Cases

### GetHistoryUseCase (for API/backend)
```kotlin
class GetHistoryUseCase(
    private val historyRepository: HistoryReadRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(limit: Int? = null, offset: Int = 0): List<HistoryEntry> =
        txRunner.inRoTransaction { historyRepository.getAll(limit, offset) }
}
```

### ObserveHistoryUseCase (for UI, reactive)
```kotlin
class ObserveHistoryUseCase(
    private val historyRepository: HistoryObserveRepository
) {
    operator fun invoke(limit: Int? = null, offset: Int = 0): Flow<List<HistoryEntry>> =
        historyRepository.observeAll(limit, offset)
}
```

### RecordSongViewUseCase
```kotlin
class RecordSongViewUseCase(
    private val historyRepository: HistoryWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(subject: HistorySubject): UpsertResourceResult<HistoryEntry> =
        txRunner.inRwTransaction { historyRepository.recordView(subject) }
}
```


### ClearHistoryUseCase
```kotlin
class ClearHistoryUseCase(
    private val historyRepository: HistoryWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(): ClearResourcesResult =
        txRunner.inRwTransaction { historyRepository.clearAll() }
}
```

### RemoveHistoryEntryUseCase
```kotlin
class RemoveHistoryEntryUseCase(
    private val historyRepository: HistoryWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(subject: HistorySubject): DeleteResourceResult<HistorySubject> =
        txRunner.inRwTransaction { historyRepository.remove(subject) }
}
```

## Models

### HistorySubject
```kotlin
sealed interface HistorySubject {
    val songId: SongId

    data class BookedSong(val songNumberId: SongNumberId) : HistorySubject {
        override val songId: SongId get() = songNumberId.songId
    }

    data class StandaloneSong(override val songId: SongId) : HistorySubject
}
```

### HistoryEntry
```kotlin
data class HistoryEntry(
    val id: Long,
    val subject: HistorySubject,
    val songName: String,
    val songNumber: Int?,           // null for standalone songs
    val bookDisplayName: String?,   // null for standalone songs
    val viewedAt: Instant,
    val viewCount: Int = 1
)
```

## Repositories

### HistoryReadRepository (domain)
```kotlin
interface HistoryReadRepository {
    suspend fun getAll(limit: Int? = null, offset: Int = 0): List<HistoryEntry>
    suspend fun getViewCount(subject: HistorySubject): Int
    suspend fun count(): Long
}
```

### HistoryWriteRepository (domain)
```kotlin
interface HistoryWriteRepository {
    suspend fun recordView(subject: HistorySubject): UpsertResourceResult<HistoryEntry>
    suspend fun remove(subject: HistorySubject): DeleteResourceResult<HistorySubject>
    suspend fun clearAll(): ClearResourcesResult
}
```

### HistoryObserveRepository (domain)
```kotlin
interface HistoryObserveRepository {
    fun observeAll(limit: Int? = null, offset: Int = 0): Flow<List<HistoryEntry>>
}
```

## Timer Implementation in ViewModel

```kotlin
class SongViewModel(
    private val recordSongView: RecordSongViewUseCase
) : ViewModel() {
    
    private var historyJob: Job? = null
    
    fun onSongOpened(subject: HistorySubject) {
        // Cancel previous timer
        historyJob?.cancel()
        
        // Start new 10 second timer
        historyJob = viewModelScope.launch {
            delay(10_000) // 10 seconds
            recordSongView(subject)
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
fun SongScreen(subject: HistorySubject) {
    val viewModel = koinViewModel<SongViewModel>()
    
    // History timer
    LaunchedEffect(subject) {
        delay(10_000)
        viewModel.addToHistory(subject)
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
│  ┌───────────────────────────────────────┐  │
│  │ Amazing Grace (standalone)            │  │
│  │ 12:00                                 │  │
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

### Domain
- `domain/history/model/HistoryEntry.kt`
- `domain/history/model/HistorySubject.kt`
- `domain/history/repository/HistoryReadRepository.kt`
- `domain/history/repository/HistoryObserveRepository.kt`
- `domain/history/repository/HistoryWriteRepository.kt`
- `domain/history/usecase/GetHistoryUseCase.kt`
- `domain/history/usecase/ObserveHistoryUseCase.kt`
- `domain/history/usecase/RecordSongViewUseCase.kt`
- `domain/history/usecase/RemoveHistoryEntryUseCase.kt`
- `domain/history/usecase/ClearHistoryUseCase.kt`

### API Contract
- `api/contract/history/HistoryEntryDto.kt`
- `api/contract/history/HistorySubjectDto.kt`
- `api/contract/history/UserHistory.kt`

### Features (UI)
- `features/history/HistoryScreen.kt`
- `features/history/HistoryScreenModel.kt`
- `features/history/HistoryUiState.kt`
