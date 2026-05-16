# Task: AI Agent Context Rollout (Incremental)

## Goal
Finish the remaining rollout steps so all agent types can reliably load context,
follow architecture norms, and avoid instruction drift.

## Already Completed
- [x] Canonical context pack created under `agents.d/context/`.
- [x] Architecture/boundary/playbook/checklist/context JSON created.
- [x] Adapter files created for `AGENTS.md`, `CLAUDE.md`, and Copilot.
- [x] Generation/bootstrap moved to Gradle tasks:
  - `./gradlew syncAiContext`
  - `./gradlew agentBootstrap`
- [x] Generated outputs added to `.gitignore`.

## Remaining Slices
- [ ] Slice 1 - CI drift gate for generated context
  - Add workflow step to run `./gradlew syncAiContext`.
  - Fail CI on diff (`git diff --exit-code`) so checked-in canonical/adapters stay aligned.
  - Verify gate catches deliberate drift.

- [ ] Slice 2 - Bootstrap contract for automation entrypoints
  - Document that agent runners should call `./gradlew agentBootstrap` at session start.
  - Add this entrypoint to top-level docs where contributors will see it first.

- [ ] Slice 3 - Trial matrix definition
  - Define 4 representative task types:
    1. GraphQL rename/deprecation
    2. Server mutation path change
    3. Client-only UI change
    4. Shared library refactor
  - Define expected files/tests each task should touch.

- [ ] Slice 4 - Trial run evidence capture (Codex path)
  - Execute matrix tasks (or dry-run prompts) with the new context flow.
  - Record pass/fail and correction needed per scenario.

- [ ] Slice 5 - Trial run evidence capture (non-Codex path)
  - Execute the same matrix with at least one other agent type.
  - Record where guidance was ambiguous or ignored.

- [ ] Slice 6 - Tighten guidance from trial findings
  - Update `ARCHITECTURE_CANONICAL.md`, `BOUNDARIES.md`, and playbooks for any recurring misses.
  - Re-run `./gradlew syncAiContext` and confirm no drift.

- [ ] Slice 7 - Closeout
  - Add a short completion summary to this file.
  - Rename to `agents.d/AI_AGENT_CONTEXT_ROLLOUT_TASK_DONE.md`.

## Definition of Done
- CI blocks drift between canonical context and generated agent files.
- Agent startup path is explicit and standardized on `./gradlew agentBootstrap`.
- Trial matrix shows acceptable adherence across at least two agent types.
- Context docs updated with evidence-driven fixes and synced via Gradle.

