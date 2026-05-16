# Task: AI Agent Context Rollout (Bootstrap-First)

## Goal
Standardize AI context consumption around a single runtime contract:
agent sessions start with `./gradlew agentBootstrap`, which regenerates
derived outputs as needed.

## Key Assumption
- Generated outputs are intentionally gitignored (`AGENTS.md`, `CLAUDE.md`,
  `.github/copilot-instructions.md`, and `agents.d/context/generated/`).
- Because those files are not tracked, "drift via git diff on generated files"
  is not a meaningful control.
- Control point should be bootstrap execution, not checked-in generated state.

## Completed Foundations
- [x] Canonical context pack created under `agents.d/context/`.
- [x] Architecture/boundary/playbook/checklist/context JSON created.
- [x] Adapter templates created in `agents.d/context/adapters/`.
- [x] Gradle tasks in place:
  - `./gradlew syncAiContext`
  - `./gradlew agentBootstrap` (depends on `syncAiContext`)
- [x] Generated outputs added to `.gitignore`.

## Revised Slices
- [x] Slice 1 - Replace drift gating with bootstrap execution gate
  - Removed `git diff --exit-code` drift check for generated outputs.
  - CI now runs `./gradlew agentBootstrap --no-configuration-cache --stacktrace`
    as a bootstrap health check.

- [x] Slice 2 - Bootstrap contract in contributor-facing docs
  - `README.md` documents session-start requirement:
    - `./gradlew agentBootstrap`
  - `README.md` documents regeneration command:
    - `./gradlew syncAiContext`

- [x] Slice 3 - Automation entrypoint alignment
  - Confirmed bootstrap-first startup guidance in internal entrypoints/templates:
    - `agents.d/context/adapters/AGENTS.base.md`
    - `agents.d/context/adapters/CLAUDE.base.md`
    - `agents.d/context/adapters/COPILOT.base.md`
    - `agents.d/context/README.md`
  - Updated `agentBootstrap` task output to clarify bootstrap already refreshes
    generated AI context files.
  - Regenerated synced outputs via `./gradlew agentBootstrap`.

- [x] Slice 4 - Trial matrix definition (bootstrap-aware)
  - Representative scenarios and scoped validation:
    1. GraphQL rename/deprecation
       - Expected modules: `server/`, `sdk/`, `server/actionz/`
       - Validation:
         - `./gradlew :server:jsTest`
         - `./gradlew :sdk:check`
         - `./gradlew :server:actionz:jsTest`
         - `scripts/graphql-ref-check.sh <field-or-operation>`
    2. Server mutation path change
       - Expected modules: `server/`, `server/actionz/`
       - Validation:
         - `./gradlew :server:jsTest`
         - `./gradlew :server:actionz:jsTest`
    3. Client-only UI change
       - Expected modules: `client/` (and possibly `client/components/`)
       - Validation:
         - `./gradlew :client:jsTest`
    4. Shared library refactor
       - Expected modules: impacted `libraries/*` module(s), plus direct dependents
       - Validation (example for `libraries:model`):
         - `./gradlew :libraries:model:check`
         - Dependent-module checks when impacted (e.g. `:server:jsTest`,
           `:sdk:check`)

- [ ] Slice 5 - Trial evidence capture (Codex)
  - For each scenario, execute with bootstrap-first flow:
    1. `./gradlew agentBootstrap`
    2. task execution/dry-run
    3. scoped validation
  - Record pass/fail and correction needed.

- [ ] Slice 6 - Trial evidence capture (non-Codex)
  - Repeat same matrix with at least one additional agent type.
  - Record ambiguity, ignored guidance, and corrections required.

- [ ] Slice 7 - Tighten canonical guidance from evidence
  - Update as needed:
    - `agents.d/context/ARCHITECTURE_CANONICAL.md`
    - `agents.d/context/BOUNDARIES.md`
    - `agents.d/context/PLAYBOOK_GRAPHQL.md`
    - `agents.d/context/TASK_CHECKLIST.md`
  - Re-run `./gradlew agentBootstrap` and confirm success.

- [ ] Slice 8 - Closeout
  - Add completion summary with evidence highlights.
  - Rename file to:
    - `agents.d/AI_AGENT_CONTEXT_ROLLOUT_TASK_DONE.md`

## Definition of Done (Revised)
- Agent startup path is explicit and standardized on `./gradlew agentBootstrap`.
- CI validates bootstrap execution (not generated-file git diff).
- Trial matrix demonstrates acceptable adherence across at least two agent types.
- Canonical docs reflect evidence-driven guidance improvements.
