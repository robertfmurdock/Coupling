# CPL-69 — Repeatable Party Configuration Catch-up

**Work card protocol: 2**

## Goal and Constraints

Make the existing named-party configuration import repeatable after its first
snapshot completes. The repository operation catches up later legacy revisions and
compare the complete source history with imported history and current metadata,
including an empty source and deleted parties. This supplies a useful offline
construction and reconciliation capability before online migration is enabled.

Follow the [migration strategy](../../DYNAMODB_SCHEMA_MIGRATION_STRATEGY.md)
and [schema proposal](../../DYNAMODB_SCHEMA_PROPOSAL.md). Preserve legacy runtime
routing, GraphQL behavior, source records and existing snapshot progress. No
provisioning, deployed-data execution, IAM change, push or release is included.
One contributor owns each slice under
the [delivery protocol](../context/WORK_CHECKLIST.md).

Use the same import capability with the configured data namespace under the
[application configuration contract](../context/ARCHITECTURE_CANONICAL.md#build-and-deployment).
This capability is independent of `ENABLE_PRERELEASE_FEATURES`; later online
migration uses that flag for incubation, then becomes unconditional.

## Starting Point

[CPL-50](../work_completed/CPL-50-PARTY_CONFIGURATION_BACKFILL.md) delivered a
fixed-watermark snapshot with atomic History/Core/checkpoint transactions and
deterministic source revision IDs. `DynamoPartyConfigurationBackfill` returns
immediately for a completed checkpoint, rejects pre-existing target data on first
initialization, and ties progress to an active BACKFILLING run. It cannot currently
import later appends or prove parity after source changes.

The source is legacy `TRIBE`, keyed by party ID and canonical millisecond revision
timestamp. Deletes are revisions. Application clocks and possible same-key changes
mean the largest timestamp alone cannot establish completeness. Keep catch-up at
the repository boundary for reuse by the migration catalog. An explicit
operator entry point is outside this card.

## Contract and Design

- Separate a completed snapshot boundary from permission to skip future checks.
  A subsequent invocation captures a new bounded source view and reuses durable
  progress without duplicating imported revisions. Completion describes that
  checked scope and boundary, not permanent freshness.
- Keep source identity, schema, run ownership and conditional ledger version checks
  authoritative. Preserve existing supported checkpoints; explicitly reject
  unsupported state without overwriting it. Define safe run completion/resumption
  and takeover semantics before allowing a different run to advance progress.
- Reread timestamp boundaries inclusively and deduplicate by the complete source
  key. Compare full named-party history to detect delayed older revisions and
  same-key content changes; do not introduce an arbitrary lateness guarantee.
  Within proven import ownership, use the latest revision in recorded legacy
  order for Core, including deletion. Insert older revisions by stable source
  identity and preserve existing History entries. History order comes from the
  source key. Preserve existing version values for compatibility and remove
  reliance on import count as the Core write guard.
- Preserve source timestamps, actors, deletion/restoration semantics and canonical
  values, including legacy defaults. Atomically couple each import effect to its
  durable progress. Retries after interruption or a lost response have the same
  observable result as uninterrupted execution.
- Report canonical content conflicts and target changes outside the import's
  proven ownership for reviewed repair. Do not overwrite them based on wall-clock
  comparison. Only demonstrably derived metadata may be rebuilt automatically.
- Return explicit complete, incomplete or conflict outcomes with the checked scope
  and boundary. Record bounded structured progress/outcome events without payloads
  or secrets, using existing logging. These outcomes do not promote the environment
  ledger, select target runtime reads or claim safety against concurrent legacy
  writers beyond the stated comparison boundary.

## Safe Slice Sequence

1. **Repeat a completed snapshot.** Extend the repository operation
   to reopen bounded work for the same party, including an initially
   empty source. Test later appends, unchanged reruns, delete/restoration, multiple
   pages, interruption/lost-response replay, competing workers, ownership changes
   and existing checkpoint compatibility. Expected red: completed checkpoints
   currently return without importing new revisions. Keep each transaction's
   effects and checkpoint advancement atomic. Independently useful for repeatable
   offline catch-up while runtime remains legacy-backed.
2. **Reconcile complete named-party history.** Add full-scope comparison and safe
   missing-revision import, with explicit conflicts and boundary evidence. Test
   delayed older revisions, inclusive timestamp replay, same-key changed content,
   missing history despite newer metadata, empty/deleted sources, target conflicts,
   concurrent source changes and interruption during comparison. Expected red:
   incremental progress misses changes behind its watermark and cannot establish
   semantic parity. Reuse canonical mapping and deterministic identity. This
   slice independently establishes bounded reconciliation for one named party.

## Acceptance Evidence

- Local DynamoDB tests prove exact historical and current-state equality for the
  scenarios above, with preserved legacy data and no duplicate history on replay.
  Counts and checksums alone do not establish semantic equality.
- Repository tests prove explicit incomplete/conflict results,
  schema/ownership rejection and preservation of existing snapshot progress.
- Existing catalog-selection tests continue to prove legacy routing. No checked-in
  migration enablement changes accompany this card.
- Local evidence covers the checked source scope and boundary. Deployed-data
  parity remains outside this card.

## Scope After Completion

Fleet discovery, other legacy sources, online read-triggered freshness, coordinated
dual writes, migration catalog activation through `ENABLE_PRERELEASE_FEATURES`
and target-only retirement remain later
outcomes. [CPL-48](../work_completed/CPL-48-DYNAMODB_MIGRATION_IAM.md) delivered
the scoped IAM implementation. No temporary AWS fixtures are required for local
implementation of this card.

## Current State

Same-run catch-up reopens completed checkpoints for appends, including after
an empty snapshot. An unfinished invocation keeps its original watermark.
The repository result reports cumulative count and last key
for that bounded snapshot. The append path retains its existing guard and
checkpoint format.

The repository compares source revisions through its completed watermark against
History. Missing revisions return incomplete. A same-key content difference
returns incomplete with the conflicting source timestamp and preserves the
imported revision. This check does not establish target-only History or Core
parity. It compares the canonical fields shared with the import writer.

Next: import missing older revisions, then detect target-only History and
reconcile Core with an ownership check independent of import count.
