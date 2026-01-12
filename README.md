# PWS Core

[![GitHub release](https://img.shields.io/github/v/release/alelk/pws-core?include_prereleases&label=version)](https://github.com/alelk/pws-core/releases)
[![GitHub Packages](https://img.shields.io/badge/maven-GitHub%20Packages-blue)](https://github.com/alelk/pws-core/packages)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

## About

**PWS Core** is a multiplatform library for the "Christian Songbook" application (Praise & Worship Songs).

Provides:
- 📱 **Domain logic** — models, use cases, repository interfaces
- 🎨 **UI components** — Compose Multiplatform screens and components
- 🌐 **API client** — HTTP client for [pws-server](https://github.com/alelk/pws-server)
- 💾 **Local storage** — Room database for offline support

### Supported Platforms

| Platform | UI | Data Source | Offline |
|----------|---|-------------|---------|
| Android | ✅ | Room DB | ✅ |
| iOS | ✅ | Room DB | ✅ |
| Web (JS) | ✅ | Remote API | ❌ |
| Telegram Mini App | ✅ | Remote API | ❌ |

## Documentation

> **For AI agents**: start with [AGENTS.md](AGENTS.md)

| Document | Description |
|----------|-------------|
| [AGENTS.md](AGENTS.md) | Quick start for AI agents |
| [docs/ai/CONTEXT.md](docs/ai/CONTEXT.md) | Project context |
| [docs/ai/CONTRIBUTING.md](docs/ai/CONTRIBUTING.md) | Development guidelines |
| [docs/GLOSSARY.md](docs/GLOSSARY.md) | Glossary of terms |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Application architecture |
| [docs/MODULES.md](docs/MODULES.md) | Module descriptions |
| [docs/DATA_FLOW.md](docs/DATA_FLOW.md) | Data flows and API |
| [docs/FEATURES.md](docs/FEATURES.md) | Feature descriptions |
| [docs/features/](docs/features/) | Detailed feature documentation |

## Module Structure

```
pws-core/
├── domain/              # 🎯 Core: models, use cases
│   ├── lyric-format/    #    Song lyrics parsing
│   └── test-fixtures/   #    Test data
├── api/
│   ├── contract/        # 📝 API DTOs
│   ├── client/          # 🌐 Ktor HTTP client
│   └── mapping/         # 🔄 DTO ↔ Domain mapping
├── features/            # 🎨 UI screens (Compose)
├── core/
│   ├── navigation/      # 🧭 Navigation (Voyager)
│   └── ui/              # 🎨 Shared UI components
├── data/
│   ├── db-room/         # 💾 Room Database
│   └── repo-room/       # 💾 Local repositories
└── backup/              # 📦 Backup/restore
```

## Technologies

- **Kotlin Multiplatform** 2.x
- **Compose Multiplatform** (UI)
- **Voyager** (navigation)
- **Koin** (DI)
- **Ktor** (HTTP)
- **Room** (local DB)
- **kotlinx.serialization** (serialization)
- **Kotest** (testing)

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