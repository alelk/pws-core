# История просмотров

## Описание

Автоматическое отслеживание просмотренных песен.

## Правила добавления

1. Песня открыта на экране
2. Прошло **10 секунд** просмотра
3. Песня добавляется в историю
4. При повторном открытии — обновляется timestamp

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

## Модели

### HistoryEntry
```kotlin
data class HistoryEntry(
    val songNumberId: SongNumberId,
    val viewedAt: Instant
)
```

## Реализация таймера в ViewModel

```kotlin
class SongViewModel(
    private val recordSongView: RecordSongViewUseCase
) : ViewModel() {
    
    private var historyJob: Job? = null
    
    fun onSongOpened(songId: Long) {
        // Отменяем предыдущий таймер
        historyJob?.cancel()
        
        // Запускаем новый таймер на 10 секунд
        historyJob = viewModelScope.launch {
            delay(10_000) // 10 секунд
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

## Альтернатива: LaunchedEffect

```kotlin
@Composable
fun SongScreen(songId: Long) {
    val viewModel = koinViewModel<SongViewModel>()
    
    // Таймер истории
    LaunchedEffect(songId) {
        delay(10_000)
        viewModel.addToHistory(songId)
    }
    
    // ... остальной UI
}
```

## UI Flow

```
┌─────────────────────────────────────────────┐
│             HistoryScreen                   │
├─────────────────────────────────────────────┤
│  ← История                                  │
├─────────────────────────────────────────────┤
│  Сегодня                                    │
│  ┌───────────────────────────────────────┐  │
│  │ БП 45 - Благословен Господь           │  │
│  │ 14:30                                 │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ ПП 12 - Благодать                     │  │
│  │ 14:15                                 │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Вчера                                      │
│  ┌───────────────────────────────────────┐  │
│  │ ИП 7 - Великий Бог                    │  │
│  │ 20:45                                 │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

- Удаление происходит из контекстного меню или смахиванием влево.
- Так же в меню должен быть пункт "Удалить все".

## Связанные файлы

- `domain/history/model/HistoryEntry.kt`
- `domain/history/repository/HistoryObserveRepository.kt`
- `domain/history/repository/HistoryWriteRepository.kt`
- `domain/history/usecase/*.kt`
- `features/history/HistoryScreen.kt`
- `features/history/HistoryScreenModel.kt`
- `features/history/HistoryUiState.kt`

