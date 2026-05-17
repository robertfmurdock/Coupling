# Agent Context Pack

This directory is the canonical source for AI-agent execution context in this
repository.

## Layout
- `ARCHITECTURE_CANONICAL.md`: canonical architecture, module ownership, and norms.
- `PERSONA.md`: owner profile and collaboration preferences (brief version).
- `PERSONA_EXTENDED.md`: extended persona for higher-ambiguity sessions.
- `TASK_CHECKLIST.md`: required pre-flight and completion checks.
- `CONTEXT_CONTRACT.md`: required context categories every agent must consume.
- `PLAYBOOK_CODE_STYLE.md`: code style rules for source code modifications.
- `GRADLE_PLAYBOOK.md`: Gradle build-logic and automation conventions.
- `GITHUB_ACTIONS_PLAYBOOK.md`: workflow authoring conventions.
- `PLAYBOOK_GRAPHQL.md`: high-risk GraphQL change recipe.
- `context.json`: machine-readable manifest (see below).
- `generated/`: generated indexes (repo modules and workflows) — optional lookups.
- `adapters/`: short templates for agent-specific injection files.

## context.json — Dual Purpose

`context.json` serves two roles:

1. **Gradle tooling**: drives `agentBootstrap` (prints read order), `syncAiContext`
   (generates files), and `validateAiContextManifest` (checks file existence).
2. **Machine-readable read manifest**: consumed by programmatic agents (e.g. Codex
   via `AGENTS.md`) to determine required vs. conditional context reads.

The generated files in `generated/` are listed under `optional_reads` — they are
useful lookups (module list, workflow summary) but not required pre-flight reading.

## Agent Entry Points

| Agent       | Entry file                        | Template source                        |
|-------------|-----------------------------------|----------------------------------------|
| Claude Code | `CLAUDE.md` (repo root, generated)| `adapters/CLAUDE.md`                   |
| Codex       | `AGENTS.md` (repo root)           | `context.json` (not generated)         |
| Copilot     | `.github/copilot-instructions.md` (generated) | `adapters/copilot-instructions.md` |

All generated entry files are produced by `syncAiContext`. Playbook lists are
injected from `context.json` via the `{{PLAYBOOKS}}` placeholder in each adapter
template — add a playbook to `context.json` and it propagates automatically.

## Update Flow
1. Edit canonical docs in this folder (not the generated files).
2. Run `./gradlew syncAiContext` to regenerate derived outputs and sync `CLAUDE.md`.
3. Run `./gradlew agentBootstrap` to refresh and validate the full context.
