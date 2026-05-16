# AGENTS

This file is source-controlled and intentionally not generated.

## Required Read Order
1. `agents.d/context/ARCHITECTURE_CANONICAL.md`
2. `agents.d/context/BOUNDARIES.md`
3. `agents.d/context/TASK_CHECKLIST.md`
4. `agents.d/context/PLAYBOOK_GRAPHQL.md` (GraphQL tasks only)
5. `agents.d/context/generated/repo-index.md`
6. `agents.d/context/generated/workflows.md`

## Core Commands
- `./gradlew agentBootstrap` (session start)
- `./gradlew test`
- `./gradlew build`
- `./gradlew check`
- `./gradlew :module:task`
- `./gradlew syncAiContext` (manual regeneration)

## Mandatory Rules
- Use the Gradle wrapper (`./gradlew`).
- Add/update conventions in canonical context files listed above; keep this file
  as pointer/read-order guidance.
- Express repository automation as Gradle tasks, not ad hoc shell scripts.
- Keep changes scoped and pattern-consistent.
- For GraphQL API changes, run `agents.d/utilities/graphql-ref-scan.sh` as a text-reference discovery helper (not a verification gate).
- Deprecations must delegate through canonical command/mutation paths.
- Keep schema/server/sdk/tests synchronized in one change set.
