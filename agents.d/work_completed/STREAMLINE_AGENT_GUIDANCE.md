# Streamline Agent Guidance

## Goal and Constraints
Replace generated context with committed Markdown and make safe incremental delivery
the contribution protocol. Preserve repository-specific conventions and cleanup gates.
No product behavior changes, agent benchmarks, or live cleanup runs.

## Safe Slice Sequence
- [x] One migration: commit shared guidance/adapters, migrate cleanup consumers, and
      remove generation tasks together. Verify build wiring and prompt rendering,
      then full checks. Independently releasable because all active consumers move
      with the removed infrastructure; no later slice is needed.

## Design Decisions
Separate testing and production design guidance. Require visible before-test and
before-implementation shots; use cards for multi-commit work/handoffs and delegate
selectively. This documentation/build-wiring migration needs no artificial red test.

Full-check prerequisite: the existing lockfile retains obsolete AWS SDK 3.1159
entries although declarations already require 3.1160. Refresh with the existing
Gradle lock task; accept only stale-entry removal, with no resolved-version changes.
This mechanical reconciliation is included so the full validation gate can run.

## Current State
2026-10-30: migration complete and reviewed from base `2ae62845d`. Full checks,
including browser tests and test-log validation, passed. This card is archived
with the migration commit; locate its commit with
`git log -1 -- agents.d/work_completed/STREAMLINE_AGENT_GUIDANCE.md`.
No remaining slices or blockers. Nothing pushed or released.

## Validation
- `./gradlew :coupling-plugins:test :scripts:weekly-cleanup:weeklyCleanupRenderPrompt --console=plain`: PASS.
- Re-rendered the final cleanup prompt: PASS after daemon restart (AWS CLI works in
  the shell; the stale daemon could not start it).
- Local Markdown link checks, obsolete active-reference scan, staged diff review,
  and `git diff --cached --check`: PASS. Both adapters are staged as tracked files.
- Initial full check stopped at stale lock entries. `./gradlew kotlinUpgradeYarnLock
  --console=plain`: PASS; only obsolete AWS SDK 3.1159 entries/selectors removed,
  no resolved package versions changed.
- Full check from zsh failed SDK authentication because test passwords were absent.
  The owner identified fish universal variables; using fish's existing exported
  environment resolved SDK authentication. `AUTH0_CLIENT_SECRET` is absent in fish
  too and was not required by this test client's authentication flow.
- Browser launch failed with the sandboxed daemon, including when an approved
  external client reused that daemon. Stopping Gradle and starting a fresh daemon
  outside the sandbox resolved Chrome startup. No tests or browser configuration
  were weakened.
- Full check from fish with a fresh approved external daemon: PASS (`./gradlew
  check --console=plain`), including Chrome browser tests and `validateTestJsonl`.
  Full check completed in 6m 43s; browser spec passed in 6m 27s. Log:
  `/private/tmp/coupling-guidance-check-fish.log`.
- No credentials/profiles were modified or printed. No agent benchmarks, live
  cleanup runs, pushes, or releases performed.
