# Глоссарий

Ключевые термины и понятия проекта PWS Core.

## Бизнес-термины

| Термин | Описание |
|--------|----------|
| **Song** | Песня с текстом (lyric), метаданными и тональностью |
| **Book** | Сборник песен (например "Божья Песнь", "Песни Победы") |
| **SongNumber** | Номер песни в сборнике (одна песня может быть в нескольких сборниках) |
| **Tag** | Категория/тег песни ("Рождество", "Пасха", "Прославление") |
| **Favorite** | Избранная песня пользователя |
| **History** | Запись о просмотре песни |
| **Cross** | Связь между похожими песнями |
| **User Override** | Пользовательское переопределение песни (своя версия текста) |
| **Lyric** | Текст песни с разметкой (куплеты, припевы, бриджи) |

## Технические термины

| Термин | Описание |
|--------|----------|
| **Use Case** | Класс, инкапсулирующий бизнес-операцию |
| **Repository** | Интерфейс для доступа к данным (абстракция над БД/API) |
| **DTO** | Data Transfer Object — сериализуемая модель для API |
| **Domain Model** | Бизнес-модель, независимая от источника данных |
| **ViewModel** | Компонент, связывающий UI с Use Cases |
| **Screen** | Voyager Screen — экран навигации с Composable |
| **Flow** | Kotlin Flow — реактивный поток данных |

## Модули

| Термин | Описание |
|--------|----------|
| `:domain` | Бизнес-логика (модели, use cases, repository interfaces) |
| `:domain:lyric-format` | Парсинг и форматирование текстов песен |
| `:api:contract` | DTO для HTTP API |
| `:api:client` | Ktor HTTP клиент |
| `:api:mapping` | Маппинг DTO ↔ Domain |
| `:features` | UI экраны и компоненты |
| `:core:navigation` | Навигационные определения |
| `:core:ui` | Общие UI утилиты |
| `:data:db-room` | Room база данных |
| `:data:repo-room` | Room репозитории |
| `:backup` | Бэкап и восстановление |

## Naming Conventions

| Паттерн | Пример | Описание |
|---------|--------|----------|
| `{Action}{Entity}UseCase` | `GetSongDetailUseCase` | Use Case для операции |
| `{Entity}ReadRepository` | `SongReadRepository` | Repository для чтения |
| `{Entity}WriteRepository` | `SongWriteRepository` | Repository для записи |
| `Remote{Entity}Repository` | `RemoteSongRepository` | Remote реализация |
| `{Feature}Screen` | `SongScreen` | Voyager Screen |
| `{Feature}ViewModel` | `SongViewModel` | ViewModel |
| `{Feature}UiState` | `SongUiState` | Sealed class состояний |
| `{Entity}Dto` | `SongDto` | API DTO |

## Пакетные имена

| Модуль | Базовый пакет |
|--------|---------------|
| `:domain` | `io.github.alelk.pws.domain` |
| `:features` | `io.github.alelk.pws.features` |
| `:api:*` | `io.github.alelk.pws.api` |
| `:core:navigation` | `io.github.alelk.pws.core.navigation` |
| `:core:ui` | `io.github.alelk.pws.core.ui` |

