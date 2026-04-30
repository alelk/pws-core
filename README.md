# PWS Core

[![GitHub release](https://img.shields.io/github/v/release/alelk/pws-core?include_prereleases&label=version)](https://github.com/alelk/pws-core/releases)
[![GitHub Packages](https://img.shields.io/badge/maven-GitHub%20Packages-blue)](https://github.com/alelk/pws-core/packages)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

Multiplatform library for PWS (Praise & Worship Songs): domain logic, Compose UI screens, API client, and local storage integration.

## What this repository contains

- Domain layer: entities, commands/queries, use cases, repository contracts.
- Feature layer: Compose Multiplatform screens on Voyager + Koin.
- Data/API implementations: Room-based local repositories and Ktor remote client.
- Shared core modules: navigation contracts and reusable UI components.

## Platforms

| Platform | UI | Data source | Offline |
|---|---|---|---|
| Android | Compose | Room DB | Yes |
| iOS | Compose | Room DB | Yes |
| Web (JS) | Compose | Remote API | No |
| Telegram Mini App | Compose | Remote API | No |

## Read this first

- For AI agents: `AGENTS.md`
- Project context: `docs/ai/CONTEXT.md`
- Engineering conventions: `docs/ai/CONTRIBUTING.md`
- Architecture overview: `docs/ARCHITECTURE.md`
- Module map: `docs/MODULES.md`
- Feature behavior: `docs/FEATURES.md` and `docs/features/`

## Module map

Top-level modules (source of truth: `settings.gradle.kts`):

```text
:domain
:domain:domain-test-fixtures
:domain:lyric-format

:api:contract
:api:mapping
:api:client
:api:client:di

:core:navigation
:core:ui

:features

:backup

:data:db-room
:data:db-room:db-room-test-fixtures
:data:repo-room
```

## Tech stack

- Kotlin Multiplatform
- Compose Multiplatform
- Voyager
- Koin
- Ktor
- Room
- kotlinx.serialization
- Kotest

## Development

### Publish local Maven artifacts

1. Update `app.version`.
2. Publish artifacts used by dependent projects:

```shell
./gradlew :domain:publishToMavenLocal :domain:domain-test-fixtures:publishToMavenLocal
```

### Run key tests

```shell
./gradlew :domain:jvmTest
./gradlew :backup:jvmTest
./gradlew :data:db-room:testDebugUnitTest :data:db-room:jvmTest
```

## Compatibility note

When changing API contracts, keep `:api:contract` and `:api:mapping` compatible with `pws-server`.

## License

This project is licensed under the **PolyForm Noncommercial License 1.0.0**. 

- **Non-commercial use only**: You are free to use, modify, and distribute this software for personal, educational, or other non-commercial purposes.
- **Commercial use**: Any commercial use requires a separate license from the author. If you are interested in using PWS Core for commercial purposes, please contact the maintainers.

See the [LICENSE](LICENSE) file for the full text.

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

Last reviewed: 2026-04-29
