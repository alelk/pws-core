# PWS Core

[![GitHub release](https://img.shields.io/github/v/release/alelk/pws-core?include_prereleases&label=version)](https://github.com/alelk/pws-core/releases)
[![GitHub Packages](https://img.shields.io/badge/maven-GitHub%20Packages-blue)](https://github.com/alelk/pws-core/packages)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

## О проекте

**PWS Core** — мультиплатформенная библиотека для приложения "Христианский песенник" (Praise & Worship Songs).

Предоставляет:
- 📱 **Domain логику** — модели, use cases, repository interfaces
- 🎨 **UI компоненты** — Compose Multiplatform screens и components
- 🌐 **API клиент** — HTTP клиент для работы с [pws-server](https://github.com/alelk/pws-server)
- 💾 **Локальное хранение** — Room database для оффлайн работы

### Поддерживаемые платформы

| Платформа | UI | Data Source | Оффлайн |
|-----------|---|-------------|---------|
| Android | ✅ | Room DB | ✅ |
| iOS | ✅ | Room DB | ✅ |
| Web (JS) | ✅ | Remote API | ❌ |
| Telegram Mini App | ✅ | Remote API | ❌ |

## Документация

> **Для AI-агентов**: начните с [AGENTS.md](AGENTS.md)

| Документ | Описание |
|----------|----------|
| [AGENTS.md](AGENTS.md) | Быстрый старт для AI-агентов |
| [docs/ai/CONTEXT.md](docs/ai/CONTEXT.md) | Контекст проекта |
| [docs/ai/CONTRIBUTING.md](docs/ai/CONTRIBUTING.md) | Инструкции для разработки |
| [docs/GLOSSARY.md](docs/GLOSSARY.md) | Глоссарий терминов |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Архитектура приложения |
| [docs/MODULES.md](docs/MODULES.md) | Описание модулей |
| [docs/DATA_FLOW.md](docs/DATA_FLOW.md) | Потоки данных и API |
| [docs/FEATURES.md](docs/FEATURES.md) | Описание функционала |
| [docs/features/](docs/features/) | Детальное описание фич |

## Структура модулей

```
pws-core/
├── domain/              # 🎯 Ядро: модели, use cases
│   ├── lyric-format/    #    Парсинг текстов песен
│   └── test-fixtures/   #    Тестовые данные
├── api/
│   ├── contract/        # 📝 DTO для API
│   ├── client/          # 🌐 Ktor HTTP клиент
│   └── mapping/         # 🔄 DTO ↔ Domain маппинг
├── features/            # 🎨 UI экраны (Compose)
├── core/
│   ├── navigation/      # 🧭 Навигация (Voyager)
│   └── ui/              # 🎨 Общие UI компоненты
├── data/
│   ├── db-room/         # 💾 Room Database
│   └── repo-room/       # 💾 Локальные репозитории
└── backup/              # 📦 Бэкап/восстановление
```

## Технологии

- **Kotlin Multiplatform** 2.x
- **Compose Multiplatform** (UI)
- **Voyager** (навигация)
- **Koin** (DI)
- **Ktor** (HTTP)
- **Room** (локальная БД)
- **kotlinx.serialization** (сериализация)
- **Kotest** (тесты)

## Development

#### Local maven publication

1. Specify [app version](app.version).

2. Publish local maven artifacts:
   ```shell
   ./gradlew :domain:publishToMavenLocal :domain:domain-test-fixtures:publishToMavenLocal
   ```

#### Run tests

*domain* module tests:

```shell
./gradlew :domain:jvmTest
```

*backup* module tests:

```shell
./gradlew :backup:jvmTest
```

*data:db-room* module tests:

```shell
./gradlew :data:db-room:testDebugUnitTest :data:db-room:jvmTest
```


## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!