# Resumable Party Configuration Backfill

## Goal and Constraints
Import one named party's complete configuration snapshot through an internal
command. Preserve runtime legacy routing. No endpoint, provisioning of AWS
resources, deployment, push, or release.

## Safe Slice Sequence
- [x] Import history and current metadata with deterministic revision IDs and
      atomic checkpoints; test interruption, retry, deletions, watermark isolation,
      ledger compatibility/ownership, namespace naming and target conflicts.
      Independently useful as an offline snapshot construction capability.

## Design Decisions
A fixed source watermark bounds each run. Per-revision transactions own History,
Core metadata and checkpoint advancement together, guarded by ledger/run state.
Completed party imports do not promote the environment beyond BACKFILLING.
The source is append-only legacy history; its fixed watermark excludes later
appends, not out-of-band edits to existing source rows. Such edits require later
reconciliation. A different run cannot take over an active environment ledger.
Existing checkpoints require the active BACKFILLING ledger; pause/restart and
catch-up orchestration remain later work. Target tables use configured STAGE;
local and repository tests retain their configured local Dynamo namespace.
Later slices own fleet discovery, catch-up, reconciliation and dual writes.

## Internal Entry Point
The server-side `PartyConfigurationBackfillDispatcher` implements
`BackfillPartyConfigurationCommand.Dispatcher`. Call its `perform` with
`BackfillPartyConfigurationCommand(stage, partyId, runId)` from internal Kotlin
code. Target tables and the supported ledger must already be bootstrapped;
this command does not provision resources. Deployed STAGE must match the command.
Use the same environment run ID when resuming or importing another party.
A competing worker's conditional failure is surfaced; rerunning the same command
resumes durable progress. No public resolver, handler, or task invokes it.

## Current State
2026-11-05: complete. One slice from base `132b3f4f2`; runtime routing and deployment
configuration remain legacy-backed. No deployed-data backfill was executed.
Delivery commit: `git log -1 --format=%H -- agents.d/work_completed/PARTY_CONFIGURATION_BACKFILL.md`.
No implementation blockers or remaining work within this slice.

## Validation
- Red: seven initial tests executed, six failed against the explicit contract
  stub. The earlier compilation warning was corrected before observing red.
- Green: 17 new DynamoDB/revision/naming tests and one command dispatcher test.
  Initial implementation runs exposed optional-map marshalling and absent STAGE
  normalization issues; both corrected at the JS boundary.
- Affected module checks and server compilation passed:
  `./gradlew :libraries:repository:dynamo:check :server:actionz:check :server:compileKotlinJs --no-daemon`.
  DynamoDB: 116 tests, zero failures, one existing skip. Actions: 65 tests,
  zero failures. Formatting and lint passed; a binding filename lint issue and
  two exception-observation mistakes in tests were corrected before this pass.
- Full `fish -lc './gradlew check --no-daemon'` passed outside the sandbox with a
  fresh daemon in 3m 45s: 621 tasks, 139 executed and 482 up-to-date. Browser E2E
  passed (one spec file), and structured-log validation passed. Unchanged test
  outputs were reused where Gradle reported up-to-date.
- Logs: `/tmp/coupling-backfill-red.log`, `/tmp/coupling-backfill-modules.log`,
  `/tmp/coupling-backfill-check.log`.
- Test-clarity review: explicit source revisions, actual DynamoDB rejection for
  ledger/history/checkpoint races, durable replay after a lost response, preserved
  legacy snapshots, full historical-default equality, deletion/restoration and
  watermark exclusion. Existing catalog-selection and endpoint tests pass.
- Production-design review: direct consistent source/ledger reads, bounded pages,
  deterministic ULIDs, shared legacy decoding and target naming, atomic revision/
  metadata/checkpoint writes, and conditional ledger ownership. Completion cannot
  enable target reads or promote the environment to dual-write.
- Complete-slice review confirms no GraphQL/deployment/IAM changes, source writes,
  automatic migration invocation, push, or release. Full-history reconciliation,
  catch-up and fleet orchestration remain subsequent work.
