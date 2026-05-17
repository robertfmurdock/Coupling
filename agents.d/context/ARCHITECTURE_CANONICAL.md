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

## Module Ownership Rules
- **Client**: Do not embed server-side policy in client code. Treat server
  contracts as external; do not infer undocumented behavior from
  implementation details. Client-only UI changes should not touch
  server/schema/sdk unless explicitly required.
- **Libraries**: Do not embed app-specific policy in shared code. Introduce
  shared abstractions only when multi-module demand and stable semantics are
  already demonstrated in this codebase.
- **SDK**: Dispatcher mappings must use explicit contract intent; avoid silent
  fallback behavior that masks schema/contract drift.
- **Tests**: Verify behavioral intent at the affected boundary, not just
  internal implementation. When migrating command paths, preserve behavioral
  assertions and add required authorization stubs. When proposing to move a
  test between architectural levels, state why confidence is maintained.

  **Tests as behavioral specifications:** Test files are auto-discovered by
  the test runner — absence of import references is not a signal of deadness,
  and a build passing after deletion only proves the *remaining* tests still
  pass. Treat each test as a machine-executable constraint on future behavior:
  removing it silently removes that constraint, with no build failure to
  signal the gap.

## Build and Test Norms
- Use `./gradlew` for all build and test tasks.
- Generated artifacts are not version-controlled. This follows the standard Gradle
  model: generated outputs belong in ignored output paths (primarily `build/`) or
  explicitly gitignored generated paths when a non-`build/` location is required.
- Testing level strategy:
  - Place at least one confidence-anchor test at the highest architecture level
    that best demonstrates the change's primary property.
  - After that primary property is established, prefer adding variations,
    permutations, and edge cases at less-integrated levels unless the variation
    changes cross-boundary behavior.
  - Treat moving tests between architectural levels as a valid optimization when
    confidence is preserved and feedback speed improves.
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
- After writing a comment, take a refactor pass to embed its content into the
  code itself — better names, extracted functions, clearer structure. A comment
  that survives this pass is one whose WHY genuinely cannot be expressed in code.
- Prefer immutable data structures and functional transformations (`map`, `filter`,
  `fold`, etc.) over mutable accumulators and imperative loops. Avoid loops whose
  exit path depends on `break`, `continue`, or accumulated mutable state — these
  obscure intent and complicate reasoning. When a loop is necessary, make its
  termination condition and output unambiguous.
- Preserve existing behavior unless change request explicitly requires it.
- Task artifacts are stored under `agents.d/tasks/`.
- Completed task artifacts are stored under `agents.d/tasks_completed/`.
- Prefer direct, local solutions first; only introduce abstractions when duplication
  or change pressure is already demonstrated in this codebase.
- Add or update behavioral/process conventions in canonical context files
  (`ARCHITECTURE_CANONICAL.md`, `BOUNDARIES.md`, `TASK_CHECKLIST.md`,
  `GRADLE_PLAYBOOK.md` when Gradle-specific, and `PLAYBOOK_GRAPHQL.md` when
  GraphQL-specific), not in `AGENTS.md` or generated context outputs.

## Integration and Delivery Heuristics
- Optimize for system flow, not local module neatness alone.
- When changing architecture-facing behavior, include expected impact on:
  - integration path (what must still compose cleanly),
  - build/test/deploy ergonomics,
  - rollback or reversal strategy.
- Avoid "activity without achievement": report outcomes and risks that influence
  decisions, not only file/activity counts.

## Multi-Agent Coordination Norms
- Before implementation, state seam assumptions that affect correctness (module
  ownership, contracts, invariants, auth/validation expectations).
- Treat assumptions as provisional; if evidence conflicts, update assumptions and
  note the change explicitly.
- If parallel work is used, assign disjoint ownership and identify integration
  touchpoints up front.
- Surface conceptual conflicts immediately (different boundary/contract choices),
  even when code compiles and tests pass.

## Automation Norm
- Repository automation and project scripting must be expressed as Gradle tasks
  (invoked via `./gradlew ...`), not ad hoc shell scripts.
- When you need a shell script for agent use, the default location is `agents.d/utilities/`
  — not `scripts/` (Gradle-invoked project automation) or `.github/` (CI workflow support).
  Check there first before repeating a complex/common workflow; create a new utility there
  when the pattern is likely to recur. Current utilities:
  - `graphql-ref-scan.sh <pattern>` — text-reference discovery across GraphQL paths.
  - `tcr-delete.sh <file> [reason]` — TCR-style dead code deletion: deletes the file,
    runs `./gradlew check`, auto-reverts on failure, and appends the verdict to
    `.github/weekly-cleanup/cleanup-history.md`. Use this instead of manual delete +
    check + revert sequences when evaluating dead code candidates.
- After convention updates, run `./gradlew syncAiContext` to propagate generated
  context artifacts.

## Completion Criteria for Agent Tasks
- Impacted modules identified up front.
- Seam assumptions and invariants that govern the change are listed.
- Scoped validation command(s) listed before broad validation.
- All linked artifacts updated for cross-layer changes.
- Relevant Gradle tasks executed and reported.
- Risks, follow-up items, and any unresolved contract questions are explicitly listed.
