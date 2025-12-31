# Синхронизация данных

## Обзор

Мобильные приложения (Android/iOS) работают в режиме **offline-first**:
- Основной источник данных — локальная Room DB
- Синхронизация с backend происходит при наличии сети
- Пользователь может работать без интернета

## Типы данных по синхронизации

| Тип | Данные | Направление | Приоритет |
|-----|--------|-------------|-----------|
| **Read-only** | Песни, Сборники, Глобальные теги | Server → Client | Низкий |
| **User data** | Избранное, История, Пользовательские теги | Bidirectional | Высокий |
| **User overrides** | Переопределения песен | Bidirectional | Высокий |

## Архитектура синхронизации

```
┌─────────────────────────────────────────────────────────────────┐
│                        Mobile App                                │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │  UI Layer   │───▶│  Use Cases  │───▶│ Local Repository    │  │
│  └─────────────┘    └─────────────┘    │ (Room)              │  │
│                                         └──────────┬──────────┘  │
│                                                    │             │
│                                         ┌──────────▼──────────┐  │
│  ┌─────────────────────────────────────▶│   Sync Manager      │  │
│  │ Network State                        │                     │  │
│  │ Observer                             │ - Pending Changes   │  │
│  │                                      │ - Conflict Resolver │  │
│  └──────────────────────────────────────│ - Sync Queue        │  │
│                                         └──────────┬──────────┘  │
│                                                    │             │
│                                         ┌──────────▼──────────┐  │
│                                         │  Remote Repository  │  │
│                                         │  (API Client)       │  │
│                                         └──────────┬──────────┘  │
└────────────────────────────────────────────────────┼─────────────┘
                                                     │
                                                     ▼
                                          ┌─────────────────────┐
                                          │   Backend Server    │
                                          └─────────────────────┘
```

## Sync Manager

Центральный компонент синхронизации:

```kotlin
interface SyncManager {
    // Состояние синхронизации
    val syncState: StateFlow<SyncState>
    
    // Запуск полной синхронизации
    suspend fun syncAll(): SyncResult
    
    // Синхронизация конкретной сущности
    suspend fun sync(entity: SyncableEntity): SyncResult
    
    // Отслеживание pending changes
    val pendingChangesCount: StateFlow<Int>
    
    // Принудительная отправка pending changes
    suspend fun pushPendingChanges(): SyncResult
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Error(val error: SyncError) : SyncState()
    data class Success(val timestamp: Instant) : SyncState()
}

enum class SyncableEntity {
    FAVORITES,
    HISTORY,
    USER_TAGS,
    SONG_TAGS,
    USER_OVERRIDES
}
```

## Pending Changes (Очередь изменений)

### Структура

```kotlin
@Entity(tableName = "pending_changes")
data class PendingChange(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: SyncableEntity,
    val entityId: Long,
    val operation: ChangeOperation,
    val payload: String,           // JSON serialized data
    val createdAt: Instant,
    val retryCount: Int = 0,
    val lastError: String? = null
)

enum class ChangeOperation {
    CREATE,
    UPDATE,
    DELETE
}
```

### Workflow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Offline Change Flow                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. User Action (offline)                                        │
│     │                                                            │
│     ▼                                                            │
│  2. Save to Local DB                                             │
│     │                                                            │
│     ▼                                                            │
│  3. Create PendingChange record ──────────────────┐              │
│     │                                             │              │
│     ▼                                             │              │
│  4. UI updated immediately                        │              │
│                                                   │              │
│  ═══════════════════════════════════════════════════════════════ │
│                    Network becomes available                     │
│  ═══════════════════════════════════════════════════════════════ │
│                                                   │              │
│  5. SyncManager detects connectivity ◀────────────┘              │
│     │                                                            │
│     ▼                                                            │
│  6. Process PendingChanges queue                                 │
│     │                                                            │
│     ├──▶ Success: Delete PendingChange                           │
│     │                                                            │
│     └──▶ Conflict: Resolve (see Conflict Resolution)             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Conflict Resolution

### Стратегии разрешения конфликтов

| Сущность | Стратегия | Описание |
|----------|-----------|----------|
| Favorites | Last-Write-Wins | Последнее изменение побеждает |
| History | Merge | Объединяем записи, дедупликация по songId |
| User Tags | Last-Write-Wins + Merge | Новые теги добавляются, изменённые — LWW |
| User Overrides | Manual / Last-Write-Wins | Опционально спросить пользователя |

### Реализация

```kotlin
interface ConflictResolver<T> {
    suspend fun resolve(
        local: T,
        remote: T,
        base: T?  // последняя синхронизированная версия
    ): ConflictResolution<T>
}

sealed class ConflictResolution<T> {
    data class UseLocal<T>(val data: T) : ConflictResolution<T>()
    data class UseRemote<T>(val data: T) : ConflictResolution<T>()
    data class Merge<T>(val data: T) : ConflictResolution<T>()
    data class AskUser<T>(val local: T, val remote: T) : ConflictResolution<T>()
}
```

### Пример: Favorites Conflict Resolver

```kotlin
class FavoritesConflictResolver : ConflictResolver<List<Favorite>> {
    override suspend fun resolve(
        local: List<Favorite>,
        remote: List<Favorite>,
        base: List<Favorite>?
    ): ConflictResolution<List<Favorite>> {
        // Находим добавленные локально (есть в local, нет в base)
        val localAdded = local.filter { fav -> 
            base?.none { it.songId == fav.songId } ?: true 
        }
        
        // Находим удалённые локально (есть в base, нет в local)
        val localRemoved = base?.filter { baseFav ->
            local.none { it.songId == baseFav.songId }
        } ?: emptyList()
        
        // Merge: remote + localAdded - localRemoved
        val merged = remote
            .filter { remoteFav -> localRemoved.none { it.songId == remoteFav.songId } }
            .plus(localAdded.filter { added -> remote.none { it.songId == added.songId } })
        
        return ConflictResolution.Merge(merged)
    }
}
```

## Sync Timestamps

Каждая синхронизируемая сущность имеет метаданные:

```kotlin
@Entity(tableName = "sync_metadata")
data class SyncMetadata(
    @PrimaryKey
    val entityType: SyncableEntity,
    val lastSyncedAt: Instant?,
    val lastServerVersion: Long?,  // для optimistic locking
    val syncStatus: SyncStatus
)

enum class SyncStatus {
    SYNCED,
    PENDING_PUSH,
    PENDING_PULL,
    CONFLICT
}
```

## Network Connectivity Observer

```kotlin
interface ConnectivityObserver {
    val isConnected: StateFlow<Boolean>
    val connectionType: StateFlow<ConnectionType>
}

enum class ConnectionType {
    NONE,
    WIFI,
    CELLULAR,
    UNKNOWN
}

// Реализация для Android
class AndroidConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {
    
    private val connectivityManager = 
        context.getSystemService<ConnectivityManager>()
    
    override val isConnected: StateFlow<Boolean> = 
        callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(true)
                }
                override fun onLost(network: Network) {
                    trySend(false)
                }
            }
            connectivityManager?.registerDefaultNetworkCallback(callback)
            awaitClose { 
                connectivityManager?.unregisterNetworkCallback(callback) 
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(), false)
}
```

## Trigger синхронизации

### Автоматические триггеры

| Триггер | Действие |
|---------|----------|
| Сеть появилась | `pushPendingChanges()` |
| App foreground | `syncAll()` если прошло > 5 мин |
| Pull-to-refresh | `syncAll()` |
| Background (periodic) | `syncAll()` каждые 15 мин (WorkManager) |

### Реализация Auto-Sync

```kotlin
class AutoSyncManager(
    private val connectivityObserver: ConnectivityObserver,
    private val syncManager: SyncManager,
    private val scope: CoroutineScope
) {
    init {
        // Sync when network becomes available
        connectivityObserver.isConnected
            .filter { it }
            .onEach { 
                syncManager.pushPendingChanges()
            }
            .launchIn(scope)
    }
}
```

## Sync для конкретных сущностей

### Favorites Sync

```kotlin
class FavoritesSyncService(
    private val localRepo: FavoriteLocalRepository,
    private val remoteRepo: FavoriteRemoteRepository,
    private val pendingChangesDao: PendingChangesDao,
    private val conflictResolver: FavoritesConflictResolver
) {
    suspend fun sync(): SyncResult {
        // 1. Push pending changes
        val pending = pendingChangesDao.getByEntity(SyncableEntity.FAVORITES)
        for (change in pending) {
            try {
                when (change.operation) {
                    CREATE -> remoteRepo.addFavorite(change.entityId)
                    DELETE -> remoteRepo.removeFavorite(change.entityId)
                    else -> {}
                }
                pendingChangesDao.delete(change)
            } catch (e: ConflictException) {
                // Handle conflict
            }
        }
        
        // 2. Pull remote changes
        val remote = remoteRepo.getFavorites()
        val local = localRepo.getFavorites()
        
        // 3. Resolve conflicts
        val resolution = conflictResolver.resolve(local, remote, getBase())
        
        // 4. Apply resolution
        when (resolution) {
            is ConflictResolution.Merge -> {
                localRepo.replaceFavorites(resolution.data)
            }
            // ...
        }
        
        return SyncResult.Success
    }
}
```

### History Sync

```kotlin
class HistorySyncService(
    private val localRepo: HistoryLocalRepository,
    private val remoteRepo: HistoryRemoteRepository
) {
    suspend fun sync(): SyncResult {
        // History: append-only, simple merge
        
        // 1. Push local entries not yet synced
        val unsynced = localRepo.getUnsynced()
        if (unsynced.isNotEmpty()) {
            remoteRepo.addHistoryBatch(unsynced)
            localRepo.markAsSynced(unsynced.map { it.id })
        }
        
        // 2. Pull remote entries we don't have
        val lastSyncedAt = localRepo.getLastSyncTimestamp()
        val remoteNew = remoteRepo.getHistorySince(lastSyncedAt)
        localRepo.insertAll(remoteNew)
        
        return SyncResult.Success
    }
}
```

## UI индикация

### Sync Status в UI

```kotlin
@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    pendingCount: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (syncState) {
            is SyncState.Syncing -> {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Text("Синхронизация...")
            }
            is SyncState.Error -> {
                Icon(Icons.Default.CloudOff, tint = Color.Red)
                Text("Ошибка синхронизации")
            }
            is SyncState.Idle -> {
                if (pendingCount > 0) {
                    Icon(Icons.Default.CloudUpload)
                    Text("$pendingCount изменений ожидают отправки")
                }
            }
            is SyncState.Success -> {
                Icon(Icons.Default.CloudDone, tint = Color.Green)
            }
        }
    }
}
```

### Offline Banner

```kotlin
@Composable
fun OfflineBanner(isConnected: Boolean) {
    AnimatedVisibility(visible = !isConnected) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Нет подключения к сети. Изменения сохранены локально.",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
```

## Модули и зависимости

```
:sync/
├── core/                    # SyncManager, interfaces
├── favorites/               # FavoritesSyncService
├── history/                 # HistorySyncService
├── tags/                    # TagsSyncService
└── di/                      # Koin modules
```

### Граф зависимостей

```mermaid
graph TD
    SyncManager --> ConnectivityObserver
    SyncManager --> FavoritesSyncService
    SyncManager --> HistorySyncService
    SyncManager --> TagsSyncService
    
    FavoritesSyncService --> FavoriteLocalRepository
    FavoritesSyncService --> FavoriteRemoteRepository
    FavoritesSyncService --> PendingChangesDao
    FavoritesSyncService --> FavoritesConflictResolver
```

## Background Sync (Android)

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return when (syncManager.syncAll()) {
            is SyncResult.Success -> Result.success()
            is SyncResult.Error -> Result.retry()
        }
    }
    
    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "pws_sync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
```

## Связанные файлы

- `sync/core/SyncManager.kt`
- `sync/core/ConflictResolver.kt`
- `sync/core/PendingChange.kt`
- `sync/favorites/FavoritesSyncService.kt`
- `sync/history/HistorySyncService.kt`
- `data/db-room/dao/PendingChangesDao.kt`
- `data/db-room/dao/SyncMetadataDao.kt`

