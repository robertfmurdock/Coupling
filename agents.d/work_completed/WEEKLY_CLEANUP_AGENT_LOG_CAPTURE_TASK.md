# Task: Weekly Cleanup Agent Log Capture and Review Infrastructure

## Goal
Make agent reasoning from weekly cleanup runs easily accessible for follow-up work — both for human review during PR triage and for agent-assisted guideline improvement in future sessions.

## Motivation
Agent behavioral patterns are visible in run logs, but the current `agent-stream.jsonl` artifact is noisy JSONL and requires a manual download step. Without friction-free access to reasoning logs, improving agent guidelines requires reconstructing what the agent did from PR diffs alone.

## Hard Constraints
- Do not alter how the agent is invoked or its output format.
- Rendered logs must not bloat the repo; only compact markdown summaries go into `.github/weekly-cleanup/logs/`.
- Raw JSONL artifact upload stays as-is (existing behavior must not regress).
- Log commits must be part of the cleanup PR commit (same branch), not a separate commit to master.

## What "agent reasoning" means here
Extract from `agent-stream.jsonl`:
- Assistant text blocks (the agent's visible reasoning before/between tool calls)
- Tool call names and key inputs (what the agent did, not full responses)
- Final result outcome

Omit: tool result bodies, raw file contents echoed back, internal scaffolding noise.

## Rollout Slices

- [ ] Slice 1 — Gradle task: `weeklyCleanupRenderLog`
  - Input: `build/weekly-cleanup/agent-stream.jsonl`
  - Output: `build/weekly-cleanup/agent-log.md`
  - Format: one section per agent turn; each section contains:
    - Assistant text block (full reasoning text)
    - Bulleted list of tool calls: `- <ToolName>: <key input summary>`
  - Final section: outcome line from the result event (subtype + error if any)
  - Task should be a no-op / skip gracefully if JSONL is absent

- [ ] Slice 2 — Upload rendered log as GH artifact
  - Add an `Upload Agent Log` step in `weekly-cleanup-pr.yml` after `Upload Agent Stream Log`
  - Artifact name: `agent-log`
  - Path: `build/weekly-cleanup/agent-log.md`
  - Same retention and `if-no-files-found: ignore` policy as the JSONL artifact
  - Run `weeklyCleanupRenderLog` before the artifact upload step (after agent step)

- [ ] Slice 3 — Commit rendered log into repo at `.github/weekly-cleanup/logs/`
  - File naming: `.github/weekly-cleanup/logs/<RUN_DATE>-<FOCUS>.md`
    - `RUN_DATE` and `FOCUS` come from the plan env (already available in workflow env)
    - Replace `/` in FOCUS with `-` for filename safety
  - Include this file in the `create-pull-request` commit (it will be staged before that step runs)
  - This makes logs directly `Read`-able by an agent in any future session without downloading artifacts
  - Top of each committed log file must include:
    - Link to the GH Actions run: `https://github.com/robertfmurdock/Coupling/actions/runs/<run_id>`
    - `gh` CLI command to download raw JSONL: `gh run download <run_id> -n agent-stream-log`
    - These are emitted by a Gradle task using `${{ github.run_id }}` passed as a project property

- [ ] Slice 4 — Include condensed summary in PR body
  - Add a `<details><summary>Agent reasoning log</summary>…</details>` block at the bottom of the PR body in `Create Cleanup Pull Request`
  - Content: first assistant text block per turn only (condensed vs. full log)
  - Implement as a Gradle task `weeklyCleanupRenderLogSummary` that writes `build/weekly-cleanup/agent-log-summary.md`
  - Workflow reads this file and injects it into the PR body using a heredoc or step output
  - Cap summary at ~100 lines to stay within PR body limits; truncate with a note if longer
  - Summary header must include the same GH Actions run link and `gh run download` command as the committed log

## Success Criteria
- A future session can do: "read `.github/weekly-cleanup/logs/` and help me improve the test-grooming prompt" with no artifact download
- PR body contains a collapsed section showing agent turn-by-turn reasoning without extra navigation
- `weeklyCleanupRenderLog` runs in < 5 seconds on typical JSONL sizes
- Zero regression to existing artifact upload or PR creation behavior

## Validation Checklist Per Slice
- Slice 1: run `./gradlew :scripts:weekly-cleanup:weeklyCleanupRenderLog` against a local `agent-stream.jsonl`; verify markdown output is human-readable
- Slice 2: trigger `workflow_dispatch` dry run; confirm `agent-log` artifact appears in GH Actions run
- Slice 3: inspect PR branch after `create-pull-request`; confirm `.github/weekly-cleanup/logs/<date>-<focus>.md` is present and `Read`-able
- Slice 4: inspect created PR body; confirm `<details>` block is present and collapsible in GitHub UI

## Definition of Done
- All four slices merged and green in a live `workflow_dispatch` run
- At least one `.github/weekly-cleanup/logs/` entry exists in the repo
- PR body format reviewed and confirmed readable by a human in GitHub UI

## Checklist
- [x] Slice 1 — `weeklyCleanupRenderLog` Gradle task
- [x] Slice 2 — Upload rendered log as GH artifact
- [x] Slice 3 — `weeklyCleanupWriteLogEntry` Gradle task + workflow step
- [x] Slice 4 — `weeklyCleanupRenderLogSummary` task + PR body `<details>` block
- [x] Move this file to agents.d/work_completed/
