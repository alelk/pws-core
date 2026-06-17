# pws-core docs

Documentation index. Agents should start with the root [`../AGENTS.md`](../AGENTS.md) and only open files here when a task needs deeper context than the runbook provides.

## Map

| File                                          | Open when you need…                                                       |
|-----------------------------------------------|---------------------------------------------------------------------------|
| [`ARCHITECTURE.md`](ARCHITECTURE.md)          | Layer responsibilities, dependency rules, reactive model, runtime data source selection |
| [`MODULES.md`](MODULES.md)                    | Module dependency graph + code-location navigation map                    |
| [`FEATURES.md`](FEATURES.md)                  | Catalog of features with links to per-feature deep-dives                  |
| [`features/`](features/)                      | Per-feature behavior + edge cases — `favorites.md`, `history.md`, `search.md`, `tags.md`, `user-overrides.md` |
| [`DATA_FLOW.md`](DATA_FLOW.md)                | Local vs. remote routing, search/book-visibility rules, asset content delivery (`CollectionBundle`, `BookBundle`) |
| [`SYNC.md`](SYNC.md)                          | Sync design notes — **NOT IMPLEMENTED** today                             |
| [`GLOSSARY.md`](GLOSSARY.md)                  | Business + technical terms, key distinctions (`SongNumber` vs `SongNumberId`, repository suffixes…) |
| [`ai/plans/`](ai/plans/)                      | **In-flight execution plans** — read first when a task references current work |
| [`ai/TASK_PLAN_TEMPLATE.md`](ai/TASK_PLAN_TEMPLATE.md) | Template for new initiative plans                                 |
| [`ai/skills/`](ai/skills/)                    | Source catalog of agent skills (synced to `.claude/skills/` for Claude Code) |

## Single source of truth — do not duplicate

`AGENTS.md` owns: build commands, module table, where-code-belongs, naming conventions, canonical code patterns, hard rules, workflows. **Don't re-state these here.** Each file in `docs/` adds depth (rationale, dep graph, code locations, feature behavior, glossary terms, sync design).

## Conventions

- **Cross-references** instead of duplication. When a file would repeat something in `AGENTS.md` or a sibling doc, link to it.
- **Each file ends with `Last reviewed: YYYY-MM-DD`** — update when content meaningfully changes.
- **Markdown tables for inventories**, prose for rationale, fenced blocks for diagrams.
- **Code samples** illustrate a pattern, not full implementations — point to the real file path instead.
- **Plans are dated** (`YYYY-MM-DD_short-slug_plan.md`). After landing, fold durable knowledge into the relevant `docs/*.md` and remove the plan.

## For new initiatives

1. Copy [`ai/TASK_PLAN_TEMPLATE.md`](ai/TASK_PLAN_TEMPLATE.md) into `ai/plans/`.
2. Reference the plan from `CLAUDE.md` "Active plans" if it should be on the hot list.
3. After landing, merge durable knowledge into the appropriate `docs/*.md` and delete the plan.
