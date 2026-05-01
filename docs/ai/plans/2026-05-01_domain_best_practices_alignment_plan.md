# 2026-05-01 Domain Best Practices Alignment Plan

## Meta

- Initiative: Domain best-practices alignment
- Date: 2026-05-01
- Owner/Agent: Copilot
- Repositories in scope: pws-core (`:domain`), pws-server (consumer alignment)
- Related initiative: `docs/ai/plans/2026-05-01_domain_refactoring_plan.md`
- Objective: довести архитектуру до strict-greenfield качества: единый typed error-flow на `Either`, чистые транзакционные границы, удаление legacy API, и жестко зафиксированные контракты домена/сервера без компромиссов по обратной совместимости.

## Definition of Done

- [x] В expected-flow доменных use case не используются `error(...)`/исключения как механизм бизнес-ветвления.
- [x] Критичные use case возвращают typed-ошибки (`Either<ErrorADT, Result>`), без смешения nullable/throw для одних и тех же сценариев.
- [x] Транзакционные границы не вложены в рамках одного orchestration-сценария.
- [x] Legacy wrappers в `domain/core/result/*` полностью удалены (без compat-слоя).
- [x] Добавлены тесты для high-risk use case и negative-path mapping (валидация, not-found, optimistic lock/conflicts).
- [x] Доменные breaking-изменения отражены и собраны в `pws-server` (route + infra + app), без legacy-веток.

## Scope

### In scope

- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/**/usecase/*`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/**/repository/*` (только контракты)
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/core/result/*`
- `domain/src/commonTest/kotlin/io/github/alelk/pws/domain/**/usecase/*`
- `pws-server/infra/src/main/kotlin/**`
- `pws-server/transport/src/main/kotlin/routes/**`
- `pws-server/app/src/main/kotlin/**`

### Out of scope

- UI (`features`)
- Кросс-продуктовые фичи вне доменной модели (новый функционал, не связанный с рефакторингом)

## Findings Snapshot (что еще не идеально)

1. **Exception-driven expected flow в replace-all сценариях**
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/songnumber/usecase/ReplaceAllBookSongNumbersUseCase.kt`
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/songreference/usecase/ReplaceSongReferencesUseCase.kt`
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/songtag/usecase/ReplaceAllSongTagsUseCase.kt`
2. **Неоднородные контракты ошибок**
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/bookstatistic/usecase/UpdateBookStatisticUseCase.kt`
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/bookstatistic/repository/BookStatisticRepository.kt`
3. **Потенциально вложенные транзакции в orchestration**
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/usecase/OverrideSongUseCase.kt`
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/usecase/GetMergedSongDetailUseCase.kt`
4. **Legacy API debt (deprecated wrappers живут в domain API)**
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/core/result/CreateResourceResult.kt`
   - и соседние файлы в `domain/core/result`
5. **Потенциально неэффективная детализация ссылок на песни**
   - `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/songreference/usecase/GetSongReferencesWithDetailsUseCase.kt`

## Task Board

| Checkbox | ID | Priority | Status | Task | Target files/modules | Acceptance criteria |
|---|---|---|---|---|---|---|
| [x] | D-001 | must | DONE | Убрать `error(...)`/rollback-exception из expected business flow | `songnumber/usecase`, `songreference/usecase`, `songtag/usecase` | Все expected ошибки возвращаются через `Either.Left`, rollback обеспечивается tx-границей без throw в бизнес-ветках |
| [x] | D-002 | must | DONE | Унифицировать контракт `UpdateBookStatisticUseCase` на typed error ADT | `bookstatistic/usecase`, `bookstatistic/repository` | Use case возвращает `Either<UpdateError, BookStatisticDetail>` (или профильный ADT), ошибки маппятся предсказуемо |
| [x] | D-003 | must | DONE | Нормализовать транзакционные границы override-сценария | `OverrideSongUseCase`, `GetMergedSongDetailUseCase`, `TransactionRunner` usage | Один orchestration-level transaction; нет вложенного `inRoTransaction` внутри `inRwTransaction` |
| [x] | D-004 | must | DONE | Полностью удалить `domain/core/result/*` и legacy-переходники | `domain/core/result/*`, все use case/consumers | В кодовой базе нет ссылок на `CreateResourceResult`/`UpdateResourceResult`/др.; только `Either<ErrorADT, *>` |
| [x] | D-005 | should | DONE | Оптимизировать получение деталей song references | `GetSongReferencesWithDetailsUseCase`, `SongReadRepository` | Нет повторных полных чтений внутри map; добавлен таргетный read-метод/проекция |
| [x] | D-006 | should | DONE | Ввести safe constructor/parse API для boundary-валидации | `domain/core/ids`, команды с `require(...)` | Для внешнего ввода есть безопасные фабрики (`parseValidated`/`validated`) с typed ошибкой |
| [x] | D-007 | must | DONE | Расширить покрытие use case тестами | `domain/src/commonTest/.../usecase` | Добавлены тесты для replace-all/override/bookstatistic + negative paths, без регрессий |
| [x] | D-008 | must | DONE | Жестко выровнять consumer-контракты в `pws-server` | `infra`, `transport/routes`, `app` | `pws-server` использует только новые доменные контракты, без fallback-адаптеров и deprecated API |
| [x] | D-009 | must | DONE | API hardening: детерминированный mapping доменных ошибок в transport | `pws-server/transport/routes/**` | Для ErrorADT-веток используется явный mapping в routes; generic fallback оставлен только для auth-level результатов |

## Execution Order

1. **Wave 1 (contract core):** D-001, D-002
2. **Wave 2 (transaction semantics):** D-003
3. **Wave 3 (hard cleanup):** D-004
4. **Wave 4 (consumer alignment):** D-008, D-009
5. **Wave 5 (quality/perf):** D-007, D-005, D-006

## Verification Matrix

- `./gradlew :domain:compileKotlinJvm`
- `./gradlew :domain:jvmTest`
- `cd /Users/alexelkin/Projects/software-development/pws-server && ./gradlew :infra:compileKotlin :transport:compileKotlin :app:compileKotlin`
- `cd /Users/alexelkin/Projects/software-development/pws-server && ./gradlew :infra:test :transport:test :app:test`
- `grep`-gate: в `domain/src/commonMain` и `domain/src/commonTest` нет `CreateResourceResult|UpdateResourceResult|DeleteResourceResult|ReplaceAllResourcesResult|UpsertResourceResult|ToggleResourceResult|ClearResourcesResult`
- `grep`-gate: в `domain/**/usecase/*.kt` нет `error(` для expected-flow бизнес-веток

## Risks and Mitigations

- Риск: массовый compile-break после удаления legacy wrappers.
  - Mitigation: удалять в начале отдельной волной, затем синхронно фиксить все consumers до зеленых сборок.
- Риск: изменение transactional semantics в override/replace-all.
  - Mitigation: зафиксировать контракт тестами на атомарность и поведение при частичных ошибках.
- Риск: расхождение HTTP-контрактов после API hardening.
  - Mitigation: обновить route tests и явно документировать mapping ErrorADT -> HTTP status.

## Context Updates (append-only)

2026-05-01 18:25 | Copilot | Создан план выравнивания `:domain` до best-practices после завершения базового рефакторинга | Выявлены архитектурные остатки: exception-driven flow, неоднородные контракты ошибок, legacy wrappers, транзакционные и perf риски | `docs/ai/plans/2026-05-01_domain_best_practices_alignment_plan.md`
2026-05-01 18:40 | Copilot | План переведен в strict-breaking режим по запросу пользователя | Убраны compat-компромиссы, добавлены обязательное удаление legacy wrappers, consumer alignment в `pws-server`, API hardening и жесткие verification gates | `docs/ai/plans/2026-05-01_domain_best_practices_alignment_plan.md`
2026-05-01 20:05 | Copilot | Выполнены D-001..D-009 end-to-end | Удалены legacy wrappers, выровнены error/tx контракты, добавлены safe parse/validated API, расширены use case тесты, пройдены compile/test gates в `pws-core` и `pws-server` | `docs/ai/plans/2026-05-01_domain_best_practices_alignment_plan.md`



