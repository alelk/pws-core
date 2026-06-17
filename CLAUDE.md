# CLAUDE.md — pws-core

> **Canonical agent guide is [`AGENTS.md`](AGENTS.md).** Read it first — everything below is
> Claude-Code-specific glue.
> Auto-loaded every session by Claude Code. Keep tight.

---

## Claude-specific reminders

### Skills (project-local in `.claude/skills/`)

Invoke via the `Skill` tool when the user asks for one or when the task description matches a
skill's `description` field.

| Skill                       | Apply when                                                   |
|-----------------------------|--------------------------------------------------------------|
| `kotlin-project-layout`     | Bootstrapping Gradle layout, `app.version`, version catalog  |
| `kmp-architecture`          | Adding KMP targets, `expect`/`actual`, JS browser config     |
| `kotlin-clean-architecture` | Auditing layer boundaries before applying backend skills     |
| `kotlin-domain-modeling`    | Entities, use cases, repos, `OptionalField`, `Either` errors |
| `ktor-api-contract`         | DTOs, `@Resource` routes, sealed DTOs with `@SerialName`     |
| `compose-multiplatform-ui`  | Features module, Voyager + StateScreenModel + Koin           |
| `voyager-navigation`        | `Screen` / `Tab` / `Navigator`, `parametersOf`, transitions  |

Each skill's `SKILL.md` is the entrypoint. Load `references/` files only if explicitly relevant.

### Subagents

- **1–2 targeted lookups** → use `Bash` with `grep`/`find` directly.
- **3+ open-ended queries** → spawn the `Explore` subagent. It protects the main context.

### Active plans (read first when relevant)

Plans live in [`docs/ai/plans/`](docs/ai/plans/). Recent:

- `2026-06-16_features-ui-refactoring-plan.md` — features UI consolidation
- `2026-06-15_data-protection-consolidated_plan.md` — data security
- `2026-05-06_book-bundle-and-data-module_plan.md` — content packaging
- `2026-05-01_domain_best_practices_alignment_plan.md` — domain refactor

When the user says "look at the plan" without a name, **ask which one**.

### Don't waste tokens on

`build/` · `*/build/` · `.gradle/` · `.kotlin/` · `local-repo/` · `kotlin-js-store/` · `*.iml` ·
`.idea/` · `features/build/generated/`.

### Cross-repo

This repo is composite-built from `pws-android`. Changes here affect the Android app automatically
when both repos are checked out side-by-side. If the user mentions an Android-only symptom, check
both repos.

---

## Everything else

Build commands · module map · hard rules · canonical patterns · workflows · docs index → *
*[`AGENTS.md`](AGENTS.md)**.
