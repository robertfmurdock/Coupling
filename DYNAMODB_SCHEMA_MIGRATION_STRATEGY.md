# DynamoDB schema migration code strategy

## Purpose

This document defines the repository changes that safely move Coupling from
the legacy record-history DynamoDB tables to the workload-oriented tables in
[DYNAMODB_SCHEMA_PROPOSAL.md](DYNAMODB_SCHEMA_PROPOSAL.md). It deliberately
does not change GraphQL fields, resolver contracts, or endpoint behavior.

The server remains legacy-backed unless an environment explicitly requests a
migration mode. A migration request is fail-closed: a missing or unrecognized
mode prevents the server from selecting a storage implementation.

## Mode and ledger

During implementation, `DYNAMODB_SCHEMA_MIGRATION_MODE` is required in every
deployment configuration. Its only valid values are `disabled`, `dual_write`,
and `cutover`. Every checked-in stage configuration starts at `disabled`; local
execution is legacy-backed unless temporarily configured otherwise. Prerelease
is the first existing deployment configuration eligible to request
`dual_write` after the repository migration checks exist.

The flags have a deliberate, temporary lifecycle:

1. `DYNAMODB_SCHEMA_MIGRATION_MODE` is the explicit implementation and early
   incubation control.
2. Once that control is proven, remove it and let
   `ENABLE_PRERELEASE_FEATURES=true` request dual-write behavior. `false`
   retains legacy behavior for an environment that has not cut over.
3. Once the target system is live everywhere, remove the prerelease-controlled
   storage branch as well. The target catalog becomes unconditional and both
   feature flags and legacy catalog selection are deleted.

A ledger already in `CUTOVER` always selects the target catalog regardless of
the prerelease setting; neither temporary flag can reverse a cutover.

| Requested mode | Ledger phase | Request catalog |
| --- | --- | --- |
| `disabled` | not cut over | legacy |
| `dual_write` | disabled or backfilling | legacy |
| `dual_write` | dual write | migration |
| `cutover` | cut over | target |

## Durable migration ledger

The migration ledger is a small, durable set of system items in the target
`coupling-core` table. It is created with the target tables before any runtime
catalog can select them, and is scoped by DynamoDB prefix and `STAGE`, so each
environment has independent progress.

| Item | Key | Required contents |
| --- | --- | --- |
| Migration state | `PK = MIGRATION#<stage>`, `SK = STATE` | Target schema version, phase, monotonic ledger version, requested mode, active run ID, reconciliation summary, cutover revision, and timestamps. |
| Backfill checkpoint | `PK = MIGRATION#<stage>`, `SK = BACKFILL#<source>#<segment>` | Deterministic legacy source/segment identity, exclusive checkpoint, source watermark, completed count, and checksum. |
| Reconciliation result | `PK = MIGRATION#<stage>`, `SK = RECONCILIATION#<runId>#<source>` | Compared source/target counts and checksums, mismatch samples, completion status, and target schema version. |
| Write-plan receipt | `PK = MIGRATION#<stage>`, `SK = WRITE#<mutationId>` | Stable mutation identity, target/legacy completion status, and replayable outcome while dual-write is active. |

The `STATE` item is the source of truth for catalog selection. Ledger reads reject
missing or unsupported target schema versions before interpreting the phase;
bootstrap preserves that ledger and reports the incompatibility. Commands use an
expected ledger version condition when advancing it, so concurrent workers
cannot independently promote or cut over an environment. Checkpoint and
receipt writes are idempotent; their source/segment and mutation keys make a
stopped worker resumable without reapplying target mutations. Reconciliation
items are evidence, not authority: only a command that verifies the complete
matching result may promote `BACKFILLING` to `DUAL_WRITE`.

The configuration requests a mode; server commands advance the ledger only
after their prerequisites are true. A requested `cutover` without a completed
dual-write ledger, or any attempt to select legacy or dual-write after
cutover, is an error.

While a dual-write request is backfilling, the public server remains on the
legacy catalog. Once the backfill and reconciliation commands mark the ledger
ready, the migration catalog serves reads from the target model and coordinates
each mutation across legacy and target stores. Returning to `disabled` before
cutover restores legacy reads and writes. A later dual-write request resumes
from its checkpoint and catches up legacy changes before target reads resume.

## Repository and action boundaries

`RepositoryCatalog` is the server selection boundary. The runtime provides
three catalog implementations:

- Legacy catalog: existing DynamoDB repository implementations.
- Migration catalog: target read repositories plus coordinated legacy and
  target mutation repositories.
- Target catalog: target repositories only after cutover.

Existing contracts stay in place when they express an application behavior.
New contracts are introduced at the access-pattern boundary rather than at a
table boundary: current party state, user and player identity lookup, timeline
windows, aggregate reads, history, and live connections. Target query
repositories own request-scoped batching so actions that share a party or
window do not repeat DynamoDB calls.

Resolvers continue to invoke command/query/action dispatchers. Only those
dispatchers may obtain repositories. Backfill, ledger progression,
reconciliation, and projection repair are commands/actions as well; deployment
configuration and resolvers do not access DynamoDB directly.

## Phased implementation vision

1. **Foundation.** Add target table provisioning, target repository contracts,
   the ledger item mapping, and catalog-selection code while every checked-in
   configuration remains disabled. The legacy catalog is the only reachable
   request catalog.
2. **Data construction.** Implement deterministic legacy readers and target
   writers for current rows, audit revisions, relationships, timeline buckets,
   summaries, and rebuildable projections. Backfill commands partition legacy
   sources deterministically and commit a checkpoint only after its complete,
   idempotent target write plan succeeds.
3. **Proof.** Implement reconciliation commands that derive expected target
   state from legacy data and write immutable comparison evidence. Projection
   repair commands may change only rebuildable target rows. This phase proves
   full-history parity before target data serves a request.
4. **Incubation.** Implement the migration catalog and enable it only when a
   ledger is reconciled. Queries read target models; mutations create durable,
   idempotent write plans that complete both legacy and target effects. Bounded
   plans use DynamoDB transactions; larger operations use stable child work.
5. **Maturity.** Replace the temporary migration-mode configuration with
   `ENABLE_PRERELEASE_FEATURES` as the dual-write request. Remove the temporary
   flag and its compatibility path; the ledger remains the cutover authority.
6. **Cutover.** Promote only a reconciled dual-write ledger to target-only
   catalogs. Legacy tables become read-only recovery evidence and remain
   outside normal repository routing.
7. **Always live.** After every supported environment has cut over, remove the
   prerelease-controlled storage branch, `ENABLE_PRERELEASE_FEATURES` storage
   interpretation, the legacy catalog, and the temporary migration code. Target
   repositories become the only runtime path.

No code path may silently fall back to legacy data after cutover.

## Verification

Existing endpoint and end-to-end test sources run unchanged against disabled
and explicitly enabled configurations. Their configuration/bootstrap may vary,
but the GraphQL operations and assertions do not fork.

Existing DynamoDB repository tests are an inventory of valuable data-layer
behavior, not a required implementation model. Target repositories add tests
for their access patterns and the proposal's correctness rules: direct and
bounded reads, cursor/bucket boundaries, authorization relationships,
idempotent write plans, full-history backfill/resume, dual-write catch-up,
aggregate parity, ledger optimistic concurrency, and irreversible cutover. No
target request path may use a scan or reconstruct current state from
legacy-style history.
