# DynamoDB schema migration code strategy

## Purpose

This document defines the repository changes that safely move Coupling from
the legacy record-history DynamoDB tables to the workload-oriented tables in
[DYNAMODB_SCHEMA_PROPOSAL.md](DYNAMODB_SCHEMA_PROPOSAL.md). It deliberately
does not change GraphQL fields, resolver contracts, or endpoint behavior.

The current server remains legacy-backed while migration capabilities are built.
The rollout below defines the intended behavior; it does not claim the online
migration catalog or freshness detection is already implemented.

## Table namespace and IAM rollout

Target tables use production `coupling-*` and other deployed stages
`<stage>-coupling-*`, including their indexes. Adding a table within an
already authorized namespace/resource family requires no per-table IAM edit.
Limit resource ARNs to the configured account and region and grant only required
operations. Preserve role-specific resource families where duties differ; do
not give every handler access to every target table merely because names match.

Keep production's existing empty legacy `DYNAMO_PREFIX` unchanged. Legacy
resolution continues to address existing tables; target resolution uses the
application-and-stage namespace independently. Current production target names
already begin with `coupling-`; this is not a legacy-table rename migration.

During foundation/incubation, replace `AmazonDynamoDBFullAccess` with required
target namespace permissions plus explicit required legacy table/index access.
Inventory and preserve bootstrap, ledger, backfill, dual-write, reconciliation,
and TTL operations before removing permissions. Separate migration privileges
from runtime only where required and with a validated replacement execution path.
At each environment's target-only retirement remove normal runtime legacy access; retain
legacy recovery evidence under separately authorized recovery access. Do not
wait for global legacy-code removal to retire that environment's permissions.

The [migration IAM work card](agents.d/work_completed/CPL-48-DYNAMODB_MIGRATION_IAM.md) owns replacement
of the broad managed policy while preserving implemented capabilities, including
policy-generation tests and available policy validation. Use existing validation
capabilities; do not provision temporary AWS infrastructure for evidence. Record
deployed endpoint enforcement separately from local or simulated policy results. Later target-only
transition work owns retirement of legacy and temporary migration permissions. The
[security remediation card](agents.d/work_completed/CPL-47-SECURITY_CRITICAL_HIGH_REMEDIATION.md)
records the transfer; the scoped implementation is delivered in CPL-48, while
deployed enforcement remains unverified until an authorized normal deployment. No provisioning, deployment,
or AWS profile changes are authorized by these documents.

## Operating states and rollout

Migration is a repeatable application capability throughout the transition, not
an operation whose success depends on running once at exactly the right time.
The intended runtime sequence is:

1. **Legacy only.** Existing reads and writes continue while useful target and
   import capabilities are delivered in independently releasable slices.
2. **Incubating migration.** `ENABLE_PRERELEASE_FEATURES=true` requests the
   migration catalog: coordinated writes to both stores, target reads, and
   repeatable import/catch-up of missing or stale legacy data before returning
   affected results. Environments without incubation remain legacy-backed.
3. **Always-enabled migration.** Promote that same migration behavior to normal
   operation independently of the prerelease flag. Both writes and self-healing
   reads remain active while confidence accumulates across ordinary usage.
4. **Target only.** After fleet reconciliation and retirement evidence are
   complete, remove legacy writes, import/detection, migration routing and the
   storage interpretation of the flag. Target repositories become unconditional.
   The shared prerelease flag remains available for other incubating features.

These are application capabilities with normal configuration, not a sequence of
named environment deployment gates. Each environment has independent durable
migration progress. Local execution and tests exercise the same paths.

`DYNAMODB_SCHEMA_MIGRATION_MODE` already exists as a foundation control. Keep
checked-in values disabled until the migration catalog is implemented; replace
its runtime selection role with the shared prerelease flag as part of delivering
incubation. Do not build another long-lived control lifecycle around it.
Ledger commands, rather than feature flags, own durable progress and target-only
retirement. Invalid configuration and unsupported ledger schemas fail explicitly.

Before target-only retirement, disabling incubation returns to legacy operation
only after coordinated writes are completed or durably recoverable there. A later
activation rechecks freshness and catches up; it does not trust a historical
completion marker. A target-only ledger always selects target storage regardless
of the prerelease setting. Neither configuration nor an older supported binary
may silently restore legacy routing after retirement. Each transition slice must
cover in-flight work and deployment overlap; a flag change alone is not evidence
that prior writers have drained.

## Repeatable import and timestamp detection

Initial backfill constructs most target data. During both incubating and
always-enabled migration, reads also detect legacy data that is absent or newer
than imported progress and run the same import capability before answering from
the target. This is explicit migration behavior, not silent legacy fallback.
Do not require fleet-wide parity before any target read: prove completeness and
freshness for the scope needed by that read. Discovery/list operations must also
find legacy-only entities; checking only existing target IDs is insufficient.

Use legacy persistence/revision timestamps as the starting point for incremental
detection, not contribution authored/effective dates or target current-row
modification times. Track progress by source and the smallest useful access scope
(entity, collection or partition), with the full source key and schema version.
A newer target row does not prove that older history, related rows or deletions
were imported. Completeness includes a successfully checked empty source.

The existing party-configuration source orders revisions by entity ID and a
canonical millisecond timestamp; deletes are revision records. Timestamps come
from application clocks, not a database commit sequence. Inspect each source's
actual key, timestamp and write semantics before applying this pattern elsewhere.
Reread timestamp boundaries inclusively and deduplicate by complete source
identity; use a stable key tie-breaker where several records share a timestamp.
Advance durable progress only with successful, idempotent import effects.

A timestamp maximum is a catch-up optimization, not proof that no delayed or
out-of-order write can arrive behind it. Recheck an overlapping range where a
bounded lateness assumption is supported; repeatable full-scope reconciliation
must cover records outside that range and same-key content changes. Do not claim
an arbitrary overlap interval guarantees completeness. A read that cannot establish
its required freshness must retry, resume work, or report an explicit incomplete
operation rather than serve a knowingly partial target result. A source lacking
reliable incremental detection needs its own bounded comparison rule before use.

Import preserves source revision identity, timestamps, actors and deletion
semantics. Replay must neither duplicate history or aggregate increments nor
resurrect deleted entities. Use stable revision identity for import replay.
Within offline import ownership, current state follows the latest revision in
recorded source order. Online
migration must guard the boundary where imports and normal writes overlap to
preserve accepted target writes. Never decide authority solely by comparing
wall-clock timestamps.
Ambiguous canonical conflicts are reported for reviewed repair; only derived
projections may be automatically rebuilt.

Ordinary writes that depend on prior source state establish that state first.
During online migration, coordinate legacy and target effects through durable,
replayable plans.
Reads cannot claim freshness while an affecting plan is incomplete. The delivery
slice defines atomicity, visible outcomes and recovery for its actual boundary;
use transactions where feasible and stable child work for larger operations.
Reuse the same mutation identity for retries and write-plan recovery.
Unfinished work must remain recoverable beyond any ordinary retry-receipt expiry. Preserve existing caller behavior; a required new
transport header is a separate compatibility decision, not an implicit storage
migration requirement.

## Durable migration ledger

Keep migration state in target Core, scoped by configured namespace and stage.
It records schema version, monotonic ledger version, operating phase, run ownership,
source progress, reconciliation evidence and target-only retirement revision.
Per-source checkpoints record full source identity, timestamp/key boundaries,
completed import effects and comparison evidence. Write-plan receipts record
mutation identity, fingerprint, completion on each store and replayable outcome.

Schema checks precede interpretation; unsupported state is preserved and rejected.
Conditional ledger updates prevent competing workers from independently advancing
state. Checkpoints, imports, reconciliation and recovery are safe to repeat after
interruptions, retries and subsequent releases. Historical reconciliation evidence
describes a scope and source boundary; it is not permanent authority to skip checks.

The current `DISABLED`/`BACKFILLING`/`DUAL_WRITE`/`CUTOVER` foundation model must be
adapted by implementation slices to support concurrent catch-up while migration
serves requests. In particular, `BACKFILLING` is not a global prohibition on target
reads, and `CUTOVER` means target-only retirement, not promotion of dual writes to
always-enabled operation. Existing offline snapshot imports remain useful but do
not yet implement this online contract.

Before target-only retirement, reconcile every legacy source and scope, including
cold data never encountered by reads. Account for outstanding write plans and
legacy-only writers across deployment overlap, and verify parity through that
boundary. Retire detection only when subsequent accepted writes are guaranteed to
use the target path. Preserve recovery evidence under separate access; do not
silently re-enable legacy as a recovery response.

## Repository and action boundaries

`RepositoryCatalog` is the server selection boundary. The runtime provides
three catalog implementations:

- Legacy catalog: existing DynamoDB repository implementations.
- Migration catalog: target reads with repeatable legacy detection/import, plus
  coordinated legacy and target mutation repositories.
- Target catalog: target repositories only after cutover.

Existing contracts stay in place when they express an application behavior.
New contracts are introduced at the access-pattern boundary rather than at a
table boundary: current party state, user and player identity lookup, timeline
windows, aggregate reads, history, and live connections. Target query
repositories own request-scoped batching so actions that share a party or
window do not repeat DynamoDB calls.

Resolvers continue to invoke command/query/action dispatchers, which obtain
repositories at the application boundary. Automatic catch-up runs inside the
migration catalog’s repository path. Model explicitly requested maintenance
as a command when an operator workflow exists. Keep DynamoDB access inside
the persistence layer. See [commands and automatic work](agents.d/context/ARCHITECTURE_CANONICAL.md#commands-and-automatic-work).

## Delivery milestones

1. **Foundation and repeatable construction.** Deliver scoped target repositories
   and resumable imports, reusing completed bootstrap and party-configuration
   snapshot work. Introduce contracts when concrete access patterns require them.
2. **Incubating migration.** Deliver coordinated writes, freshness detection,
   read-triggered import and repeatable reconciliation for the affected boundaries.
   Enable the catalog through the shared prerelease flag only when its supported
   application flows meet this contract. Keep cross-layer changes in the same slice.
3. **Always-enabled migration.** Promote the proven migration catalog to ordinary
   operation. Retain both stores, detection and repair while validating real usage.
4. **Target only and cleanup.** Establish fleet completeness and writer retirement,
   remove legacy runtime permissions, then delete dual writes, imports, temporary
   ledger/control code and legacy implementations as safely releasable slices.
   Retain ordinary target domain idempotency and observability capabilities.

Work cards own bounded outcomes, mechanisms and acceptance evidence under the
[delivery protocol](agents.d/context/WORK_CHECKLIST.md). Each slice is useful and
safe to release if subsequent work stops. These milestones do not require building
all target abstractions before delivering any useful migration capability.

## Verification and operational evidence

Run the same endpoint and end-to-end application contracts against legacy,
incubating migration, always-enabled migration and target-only configurations.
Extend them for new contracts without forking expected product results by storage
mode. Preserve authorization, exact statistics, historical membership, timestamps,
team choices and deletions; counts/checksums alone do not prove semantic parity.

At the owning boundaries test first import, empty sources, partial imports,
repeated reads, interruption/resume, equal timestamps, delayed older writes,
deletes/restorations, cold legacy-only entities, import/write races, partial dual
writes, disable/re-enable, deployment overlap and target-only retirement. Add these
scenarios with the slices that introduce the behavior, rather than duplicating
the full matrix at every layer. Existing repository tests constrain valuable
behavior without mandating their legacy implementation model.

The target request path uses direct/bounded access patterns and does not scan or
reconstruct current state from revision history. Migration-only import and
reconciliation may traverse legacy history with bounded pages and durable progress;
large repairs resume through ordinary commands rather than unbounded request work.
Define request budgets and explicit resumable outcomes in the affected slice.

Use ordinary structured application logs for import/catch-up, reconciliation,
write-plan recovery and transitions: stable operation names, run/request correlation,
source scope without sensitive payloads, counts, progress, outcomes and elapsed
durations. Verify collection at the intended destination and expose useful non-secret
status through appropriately authorized application contracts where needed.

Before retirement, record completeness, unresolved-conflict and outstanding-work
evidence, deployment compatibility and recovery results. Measure representative
current-party reads, pairing and contribution imports at realistic history sizes;
use the proposal's product/performance criteria and existing delivery signals.
A passing build or time spent incubating alone does not prove data completeness.
