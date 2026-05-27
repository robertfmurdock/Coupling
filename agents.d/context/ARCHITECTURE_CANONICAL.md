# Architecture Canonical

Canonical reference for AI agents. Link to this, don't duplicate.

## Project
Multi-module Gradle: Kotlin/JVM + Kotlin/JS. Web client + backend services.

## Modules
- `client/`: frontend (Kotlin/JS), Vite config at `client/vite.config.mjs`
- `server/`: backend + GraphQL schema/resolvers
- `libraries/`: shared code, reusable infrastructure
- `sdk/`: GraphQL documents + dispatchers
- `cli/`, `e2e/`, `scripts/`, `deploy/`: tooling/ops

## Ownership
- **Client**: No server policy in client. Treat server as external.
- **Libraries**: No app policy in shared code. Add abstractions only when multi-module demand + stable semantics already exist.
- **SDK**: Explicit contract mappings. No silent fallbacks.
- **Tests**: Verify behavior at affected boundary. Tests = executable constraints (deletion removes constraints with no build signal).

## Build
- Use `./gradlew` always
- Generated artifacts not version-controlled
- Testing: confidence-anchor at highest level, variations at lower levels
- Baseline: `./gradlew test|build|check`
- Scoped: `./gradlew :module:task`

## GraphQL
**Paths:**
- Schema: `server/src/jsMain/resources/schema.graphqls`
- Resolvers: `server/src/jsMain/kotlin/...`
- SDK docs: `sdk/src/commonMain/graphql/`
- SDK dispatchers: `sdk/src/commonMain/kotlin/...`
- Action tests: `server/actionz/src/jsTest/kotlin/...`

**Change Rules:**
- Deprecations delegate through canonical path (no logic duplication)
- Field add/rename/deprecation/removal = synchronized update: schema, resolver, SDK docs, dispatcher, tests
- Run `agents.d/utilities/graphql-ref-scan.sh <pattern>` for reference discovery (not exhaustive)

## Editing
- Minimal diffs, pattern-consistent
- No unrelated refactors in features/bugfixes
- Preserve behavior unless explicitly changed
- Work cards: `agents.d/work/` → `agents.d/work_completed/`
- Direct solutions first; abstractions only when duplication/pressure demonstrated
- Convention updates go in canonical files (`ARCHITECTURE_CANONICAL.md`, `BOUNDARIES.md`, `WORK_CHECKLIST.md`, playbooks), not `AGENTS.md`

## Delivery
- Optimize system flow, not local neatness
- State impact: integration path, build/test/deploy ergonomics, rollback strategy
- Report outcomes/risks, not activity counts

## Multi-Agent Coordination
- State seam assumptions up front (ownership, contracts, invariants, auth)
- Update assumptions when evidence conflicts
- Parallel work: disjoint ownership, integration touchpoints identified
- Surface conceptual conflicts immediately

## Automation
- Express as Gradle tasks (`./gradlew ...`), not shell scripts
- Agent utilities: `agents.d/utilities/`
  - `graphql-ref-scan.sh <pattern>` — GraphQL reference discovery
  - `tcr-delete.sh <file> [reason]` — safe dead code deletion with auto-revert
  - `chrome-e2e-diagnostics.sh [--check|--clean]` — Chrome e2e troubleshooting
  - `kill-chrome-processes.sh` — kill stuck ChromeDriver
- After convention updates: `./gradlew syncAiContext`

## Completion Criteria
- Impacted modules identified
- Seam assumptions/invariants listed
- Scoped validation before broad
- Cross-layer artifacts updated
- Risks/follow-ups/unresolved questions listed
