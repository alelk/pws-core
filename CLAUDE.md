# Claude Code — pws-core

> Kotlin Multiplatform library: domain · features (Compose MP) · API client · local data.
> This file is **auto-loaded every session**. Read what's here, then load deeper docs on demand.

## Repo at a glance

- **Role:** shared multiplatform core for the PWS songbook ecosystem.
- **Consumers:** `pws-android` (Android app), `pws-server` (backend), web/Telegram shells (planned).
- **Hard contract:** `:api:contract` + `:api:mapping` stay aligned with `pws-server`.
- **JDK:** 21. **Kotlin:** 2.3.21. **Gradle:** wrapper (`./gradlew`).

## Read on demand (do NOT preload)

| When you need…                       | Open                                      |
|--------------------------------------|-------------------------------------------|
| Project context, modules, tech stack | `docs/ai/CONTEXT.md`                      |
| Coding conventions and forbiddens    | `docs/ai/CONTRIBUTING.md`                 |
| Feature catalog & behavior           | `docs/FEATURES.md`, `docs/features/*.md`  |
| Architecture & module dependency map | `docs/ARCHITECTURE.md`, `docs/MODULES.md` |
| Data/sync flows                      | `docs/DATA_FLOW.md`, `docs/SYNC.md`       |
| Domain terminology                   | `docs/GLOSSARY.md`                        |
| In-flight initiatives (read first!)  | `docs/ai/plans/*.md`                      |
| Plan template for new work           | `docs/ai/TASK_PLAN_TEMPLATE.md`           |
| Generic agent guide (non-Claude)     | `AGENTS.md`                               |

## Hot paths

### Build & test commands

| Goal                          | Command                                            |
|-------------------------------|----------------------------------------------------|
| Fast compile (touched module) | `./gradlew :<module>:compileKotlinJvm`             |
| Run module tests              | `./gradlew :<module>:jvmTest`                      |
| Full multiplatform assemble   | `./gradlew :features:assemble`                     |
| Common smoke                  | `./gradlew :domain:jvmTest :portable-data:jvmTest` |

Rule of thumb: **module-scoped tasks first**; only run `:assemble` or repo-wide checks before declaring work done.

### Source layout shortcuts

```
domain/src/commonMain/kotlin/io/github/alelk/pws/domain/{entity}/
  ├ model/         entities, value objects
  ├ repository/    Read | Write | Observe interfaces
  ├ usecase/       {Action}{Entity}UseCase
  ├ command/       UpdateXxxCommand + OptionalField patches
  └ query/         filter/sort objects

features/src/commonMain/kotlin/io/github/alelk/pws/features/{feature}/
  ├ {Feature}Screen.kt          Voyager Screen
  ├ {Feature}ScreenModel.kt     StateScreenModel<UiState>
  ├ {Feature}UiState.kt         sealed Loading | Content | Error
  └ {feature}ScreenModule.kt    Koin module

api/client/src/commonMain/kotlin/repository/   remote repo impls
data/repo-room/src/commonMain/kotlin/.../room/ Room repo impls
```

## Hard rules (saves turns — don't make me correct twice)

### Architecture
- ❌ **No `java.*` / Android / iOS APIs in `:domain`'s `commonMain`** — domain stays platform-agnostic.
- ❌ **UI never calls repositories** — always through use cases.
- ❌ **No cross-feature ScreenModel imports** — communicate via Koin-provided interfaces.
- ❌ **Don't rename DTOs in `:api:contract` / `:api:mapping`** without coordinating with `pws-server`.

### ScreenModel / UI state (enforced in 2026-06 refactor — see `docs/ai/plans/2026-06-16_features-ui-refactoring-plan.md`)
- ✅ **One composite `XxxUiState`** per screen, shape `Loading | Content | Error`.
- ✅ **One-shot signals via `Channel<Effect>(BUFFERED)`** — never `MutableSharedFlow<Effect>()` for new code.
- ✅ **Typed errors via `UiMessage`** (in `features/src/commonMain/.../app/UiMessage.kt`) — never raw `e.message`, never sentinel strings.
- ✅ **`koinScreenModel<T>()` for state acquisition** — never `remember { ScreenModel() }`.
- ✅ **Surface failures**: `catch (_: Exception) {}` swallows are banned in ScreenModels. Emit `Effect.ShowError(UiMessage.Failure(...))` or update state.
- ✅ **Parameterized `Screen` is `data class`** OR has `override val key = "stable/$param"`.

### Compose
- ❌ **No `Modifier.composed { }`** (deprecated). Use Composable factory pattern (`@Composable fun Modifier.xxx(...)`).
- ❌ **No hardcoded UI strings** — always `stringResource(Res.string.*)`.
- ❌ **No localized text in `ScreenModel` state or effects** — that's the i18n boundary.
- ✅ **One `ActiveSheet?` sealed bucket** when a screen owns multiple bottom sheets; not N booleans.

## Skills (project-local, in `.claude/skills/`)

Invoke via the Skill tool when the user asks for one or when the task matches a skill's `description`.

| Skill                       | Apply when                                                   |
|-----------------------------|--------------------------------------------------------------|
| `kotlin-project-layout`     | Bootstrapping Gradle layout, `app.version`, version catalog  |
| `kmp-architecture`          | Adding KMP targets, expect/actual, JS browser config         |
| `kotlin-clean-architecture` | Auditing layer boundaries before backend skills              |
| `kotlin-domain-modeling`    | Entities, use cases, repos, `OptionalField`, `Either` errors |
| `ktor-api-contract`         | DTOs, `@Resource` routes, sealed DTOs with `@SerialName`     |
| `compose-multiplatform-ui`  | Features module, Voyager + StateScreenModel + Koin           |
| `voyager-navigation`        | Screen/Tab/Navigator, `parametersOf`, transitions            |

Each skill's `SKILL.md` is the entrypoint. Load `references/` files only if explicitly relevant.

## Workflow shortcuts

### When the user asks for "a new feature"

1. Find the right module via the table in `docs/ai/CONTRIBUTING.md`.
2. Domain first (models → repository interfaces → use cases), then UI.
3. Use existing patterns in nearby files — don't invent.
4. Compile the touched module before claiming success.

### When the user says "look at the plan"

In-flight initiatives live in `docs/ai/plans/`. Recent (read these first when relevant):
- `2026-06-16_features-ui-refactoring-plan.md` — features UI consolidation
- `2026-06-15_data-protection-consolidated_plan.md` — data security
- `2026-05-06_book-bundle-and-data-module_plan.md` — content packaging
- `2026-05-01_domain_best_practices_alignment_plan.md` — domain refactor

### When the user says "explore" or "what is X"

For 1–2 targeted lookups use Bash `grep`/`find`. For 3+ open-ended queries spawn `Explore` subagent — protects main context.

## Don't waste tokens on

These directories are noise — never grep or list them unless the user explicitly references them:

- `build/`, `*/build/`, `.gradle/`, `.kotlin/`
- `local-repo/`, `kotlin-js-store/`
- `*.iml`, `.idea/`
- generated Compose resource accessors under `features/build/generated/`

## Cross-repo composite build

When running inside `pws-android`, Gradle automatically links `../pws-core` as composite. Changes here propagate without `publishToMavenLocal`. If the user mentions an Android-only symptom, check both repos.
