# История просмотров

## Описание

Автоматическое отслеживание просмотренных песен.

## Правила добавления

1. Песня открыта на экране
2. Прошло **10 секунд** просмотра
3. Песня добавляется в историю
4. При повторном открытии — обновляется timestamp

## Use Cases

### AddHistoryUseCase
```kotlin
class AddHistoryUseCase(
    private val historyRepository: HistoryWriteRepository
) {
    suspend operator fun invoke(songId: Long)
}
```

### GetHistoryUseCase
```kotlin
class GetHistoryUseCase(
    private val historyRepository: HistoryReadRepository
) {
    operator fun invoke(
        page: Int = 0,
        size: Int = 50
    ): Flow<List<HistoryEntry>>
}
```

### ClearHistoryUseCase
```kotlin
class ClearHistoryUseCase(
    private val historyRepository: HistoryWriteRepository
) {
    suspend operator fun invoke()
}
```

## Модели

### HistoryEntry
```kotlin
data class HistoryEntry(
    val id: Long,
    val songId: Long,
    val viewedAt: Instant,
    val song: SongSummary  // краткая информация
)
```

## Реализация таймера в ViewModel

```kotlin
class SongViewModel(
    private val addHistoryUseCase: AddHistoryUseCase
) : ViewModel() {
    
    private var historyJob: Job? = null
    
    fun onSongOpened(songId: Long) {
        // Отменяем предыдущий таймер
        historyJob?.cancel()
        
        // Запускаем новый таймер на 10 секунд
        historyJob = viewModelScope.launch {
            delay(10_000) // 10 секунд
            addHistoryUseCase(songId)
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
│             HistoryScreen                    │
├─────────────────────────────────────────────┤
│  ← История                          🗑️ Все  │
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

## Группировка по дате

```kotlin
data class GroupedHistory(
    val date: LocalDate,
    val label: String,  // "Сегодня", "Вчера", "30 декабря"
    val entries: List<HistoryEntry>
)

fun List<HistoryEntry>.groupByDate(): List<GroupedHistory> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    
    return groupBy { entry ->
        entry.viewedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }.map { (date, entries) ->
        GroupedHistory(
            date = date,
            label = when (date) {
                today -> "Сегодня"
                yesterday -> "Вчера"
                else -> date.format(...)
            },
            entries = entries.sortedByDescending { it.viewedAt }
        )
    }.sortedByDescending { it.date }
}
```

## Связанные файлы

- `domain/history/model/HistoryEntry.kt`
- `domain/history/repository/HistoryReadRepository.kt`
- `domain/history/repository/HistoryWriteRepository.kt`
- `domain/history/usecase/*.kt`
- `features/history/HistoryScreen.kt`
- `features/history/HistoryViewModel.kt`

