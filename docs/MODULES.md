# Модули PWS Core

## :domain

**Группа**: `io.github.alelk.pws.domain`

Центральный модуль с бизнес-логикой. Не зависит от платформы.

### Структура

```
domain/src/commonMain/kotlin/io/github/alelk/pws/domain/
├── song/           # Песни
├── book/           # Сборники
├── songnumber/     # Номера песен в сборниках
├── tag/            # Теги/категории
├── songtag/        # Связь песен с тегами
├── favorite/       # Избранное
├── history/        # История просмотров
├── search/         # Поиск
├── cross/          # Связи между песнями
├── songreference/  # Ссылки на похожие песни
├── auth/           # Авторизация
├── payment/        # Платежи (future)
└── core/           # Общие утилиты (Locale, etc.)
```

### Организация пакета сущности

Каждый пакет организован одинаково:

```
{entity}/
├── model/          # Domain модели (data classes)
├── repository/     # Repository interfaces
├── usecase/        # Use cases
├── command/        # Command объекты для записи
└── query/          # Query объекты для чтения
```

### Зависимости

- `kotlinx.serialization.core`
- `kotlinx.coroutines.core`

---

## :domain:domain-test-fixtures

Тестовые фикстуры для domain моделей.

### Содержимое

- Генераторы для property-based тестирования
- Моки репозиториев

---

## :domain:lyric-format

**Группа**: `io.github.alelk.pws.domain`

Парсинг и форматирование текстов песен (лирики).

### Назначение

- Парсинг структурированного текста песни (куплеты, припевы, бриджи)
- Форматирование лирики для отображения
- Интернационализация (i18n4k) — поддержка EN, UK, RU

### Содержимое

```
lyric-format/src/commonMain/kotlin/io/github/alelk/pws/domain/lyric/format/
├── LyricParser.kt    # Парсер текста (Kudzu parser combinators)
└── LyricWriter.kt    # Форматирование для вывода
```

### Зависимости

- `:domain`
- Kudzu (parser combinators)
- i18n4k (internationalization)

---

## :api:contract

**Группа**: `io.github.alelk.pws.api`

DTO (Data Transfer Objects) для API.

### Назначение

- Сериализуемые модели для HTTP запросов/ответов
- Аннотированы `@Serializable`
- Соответствуют контракту backend API

---

## :api:client

**Группа**: `io.github.alelk.pws.api`

HTTP клиент для backend API.

### Структура

```
api/client/src/commonMain/kotlin/
├── api/            # API endpoints
├── client/         # Ktor клиент конфигурация
├── config/         # Настройки
├── error/          # Обработка ошибок
├── http/           # HTTP утилиты
└── repository/     # Remote репозитории
```

### Remote репозитории

Реализуют domain repository interfaces:

| Репозиторий | Интерфейс |
|-------------|-----------|
| `RemoteSongReadRepository` | `SongReadRepository` |
| `RemoteSongWriteRepository` | `SongWriteRepository` |
| `RemoteBookReadRepository` | `BookReadRepository` |
| `RemoteBookWriteRepository` | `BookWriteRepository` |

### Зависимости

- `:domain`
- `:api:contract`
- `:api:mapping`
- Ktor Client (core, auth, content-negotiation)

---

## :api:mapping

Маппинг между API DTO и Domain моделями.

### Содержимое

- Extension functions для конвертации
- `toDto()` и `toDomain()` функции

---

## :features

**Группа**: `io.github.alelk.pws.features`

UI компоненты на Compose Multiplatform.

### Структура

```
features/src/commonMain/kotlin/io/github/alelk/pws/features/
├── app/            # App-wide компоненты (AppBar, etc.)
├── book/           # Экран сборника
├── books/          # Список сборников
├── search/         # Экран поиска
├── song/           # Экран песни
├── favorites/      # Избранное
├── history/        # История
├── tags/           # Теги
├── components/     # Переиспользуемые UI компоненты
├── theme/          # Тема (цвета, типографика)
└── di/             # Koin модули
```

### Организация feature

```
{feature}/
├── {Feature}Screen.kt      # Voyager Screen
├── {Feature}ViewModel.kt   # ViewModel
├── {Feature}UiState.kt     # UI State sealed class
└── components/             # Feature-specific компоненты
```

### Зависимости

- `:domain`
- `:core:navigation`
- Compose Multiplatform
- Voyager (navigator, koin)
- Koin
- Lifecycle ViewModel

### TODO

1. Разбить на модули:

```
features/
├── feature-books/          # :features:books
├── feature-songs/          # :features:songs  
├── feature-favorites/      # :features:favorites
├── feature-search/         # :features:search
├── feature-tags/           # :features:tags
└── common/ 
```

2. Перенести theme/ в :core:ui
3. Для каждого Screen добавить @Preview

---

## :core:navigation

Общие навигационные компоненты.

### Содержимое

```kotlin
// Navigation.kt - навигационные утилиты

// SharedScreens.kt - определения экранов
sealed interface SharedScreen {
    // Определены все возможные экраны приложения
}
```

### Использование

Features модуль создает реализации Screen для каждого SharedScreen.

---

## :core:ui

Общие UI компоненты и утилиты.

### Назначение

- Переиспользуемые Compose компоненты низкого уровня
- UI утилиты и extensions
- Общие модификаторы

---

## :data:db-room

Room база данных для Android/iOS.

### Содержимое

- Entity классы (таблицы)
- DAO interfaces
- Database class
- Migrations
- Type converters

---

## :data:repo-room

Локальные репозитории на основе Room.

### Назначение

- Реализуют domain repository interfaces
- Работают с Room DAO
- Используются в Android/iOS приложениях

---

## :backup

Функционал резервного копирования.

### Содержимое

- Сериализация/десериализация данных
- Экспорт/импорт в файл
- Миграция данных между версиями

---

## :sync

Синхронизация данных между локальной БД и сервером.

Подробнее см. [SYNC.md](SYNC.md)

### Структура

```
sync/
├── core/                    # Базовые интерфейсы и SyncManager
│   ├── SyncManager.kt
│   ├── ConflictResolver.kt
│   ├── PendingChange.kt
│   └── ConnectivityObserver.kt
├── favorites/               # Синхронизация избранного
├── history/                 # Синхронизация истории
├── tags/                    # Синхронизация тегов
├── overrides/               # Синхронизация user overrides
└── di/                      # Koin модули
```

### Назначение

- Offline-first архитектура для мобильных приложений
- Очередь pending changes для работы без сети
- Разрешение конфликтов при синхронизации
- Background sync через WorkManager (Android)

### Зависимости

- `:domain` — интерфейсы репозиториев
- `:data:repo-room` — локальные репозитории
- `:api:client` — remote репозитории

## Граф зависимостей

```mermaid
graph TD
    subgraph "UI Layer"
        features[":features"]
        navigation[":core:navigation"]
        coreui[":core:ui"]
    end
    
    subgraph "Domain Layer"
        domain[":domain"]
        lyricformat[":domain:lyric-format"]
        fixtures[":domain:domain-test-fixtures"]
    end
    
    subgraph "Data Layer - Remote"
        client[":api:client"]
        contract[":api:contract"]
        mapping[":api:mapping"]
    end
    
    subgraph "Data Layer - Local"
        room[":data:db-room"]
        reporoom[":data:repo-room"]
    end
    
    subgraph "Sync Layer"
        sync[":sync"]
    end
    
    backup[":backup"]
    
    features --> domain
    
    sync --> domain
    sync --> reporoom
    sync --> client
    features --> lyricformat
    features --> navigation
    features --> coreui
    
    lyricformat --> domain
    
    client --> domain
    client --> contract
    client --> mapping
    mapping --> contract
    mapping --> domain
    
    reporoom --> domain
    reporoom --> room
    
    backup --> domain
    
    fixtures --> domain
```

