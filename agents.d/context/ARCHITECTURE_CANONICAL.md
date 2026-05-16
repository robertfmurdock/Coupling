# Coupling Architecture Canonical

This file is the canonical architecture reference for AI agents. Agent-specific
instruction files should be short and link to this file, not duplicate it.

## Project Model
- Multi-module Gradle project.
- Kotlin/JVM and Kotlin/JS modules.
- Primary product surfaces: web client + backend services.

## Module Map
- `client/`: frontend (Kotlin/JS), Vite config at
  `client/vite.config.mjs`.
- `server/`: backend services and GraphQL schema/resolvers.
- `libraries/`: shared code and reusable infrastructure.
- `sdk/`: shared GraphQL documents and dispatchers.
- `cli/`, `e2e/`, `scripts/`, `deploy/`: tooling and operations.

## Build and Test Norms
- Use `./gradlew` for all build and test tasks.
- Start agent sessions with `./gradlew agentBootstrap` before editing.
- Generated artifacts are not version-controlled. This follows the standard Gradle
  model: generated outputs belong in ignored output paths (primarily `build/`) or
  explicitly gitignored generated paths when a non-`build/` location is required.
- Baseline checks:
  - `./gradlew test`
  - `./gradlew build`
  - `./gradlew check`
- For scoped validation use `./gradlew :module:task`.

## GraphQL Architecture
- Schema: `server/src/jsMain/resources/schema.graphqls`.
- Resolvers: `server/src/jsMain/kotlin/...`.
- SDK GraphQL docs: `sdk/src/commonMain/graphql/`.
- SDK dispatchers: `sdk/src/commonMain/kotlin/...`.
- Server action tests: `server/actionz/src/jsTest/kotlin/...`.

## GraphQL Change Rules
- Deprecations must delegate through canonical command/mutation path.
- Do not duplicate legacy resolver logic when deprecating fields.
- GraphQL rename/deprecation work is cross-layer by default; update server + SDK +
  tests in one change set unless a file-level impact review proves otherwise.
- Any field add/rename/deprecation/removal requires synchronized updates:
  1. Schema
  2. Server resolver/command path
  3. SDK `.graphql` documents
  4. SDK dispatcher/model mapping
  5. Action tests and auth stubs as needed
- Run `agents.d/utilities/graphql-ref-scan.sh <field-or-operation>` for text-reference
  discovery across common GraphQL paths. This is not a verification gate and can
  miss indirect/dynamic usage.

## Editing Norms
- Keep diffs minimal and pattern-consistent.
- Do not introduce unrelated refactors in feature/bugfix changes.
- Preserve existing behavior unless change request explicitly requires it.
- Add or update behavioral/process conventions in canonical context files
  (`ARCHITECTURE_CANONICAL.md`, `BOUNDARIES.md`, `TASK_CHECKLIST.md`, and
  `PLAYBOOK_GRAPHQL.md` when GraphQL-specific), not in `AGENTS.md` or generated
  context outputs.

## Automation Norm
- Repository automation and project scripting must be expressed as Gradle tasks
  (invoked via `./gradlew ...`), not ad hoc shell scripts.
- Agent-only reusable helpers belong in `agents.d/utilities/`; check there first
  before repeating a complex/common workflow, and add utilities there when reuse
  is expected.
- After convention updates, run `./gradlew syncAiContext` to propagate generated
  context artifacts.
- For AI context generation/bootstrap, use:
  - `./gradlew syncAiContext`
  - `./gradlew agentBootstrap`

## Completion Criteria for Agent Tasks
- Impacted modules identified up front.
- Scoped validation command(s) listed before broad validation.
- All linked artifacts updated for cross-layer changes.
- Relevant Gradle tasks executed and reported.
- Risks and follow-up items explicitly listed.
