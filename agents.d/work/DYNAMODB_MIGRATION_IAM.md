# DynamoDB Migration IAM Boundary

## Goal and Constraints
Replace `AmazonDynamoDBFullAccess` as part of the target-schema rollout without
requiring role edits for each new table. This owns the outstanding DynamoDB
finding transferred from [security remediation](SECURITY_CRITICAL_HIGH_REMEDIATION.md),
not a completed remediation. Lambda invocation remains in that security card.

Follow the [schema proposal](../../DYNAMODB_SCHEMA_PROPOSAL.md) and
[migration strategy](../../DYNAMODB_SCHEMA_MIGRATION_STRATEGY.md). Preserve
application behavior, migration compatibility, and production legacy naming.
No provisioning, deployment, push, release, or AWS profile edits are authorized.
Do not expand the separate ledger-schema compatibility card into this work.

## Safe Slice Sequence
- [ ] Inventory effective generated policies, required DynamoDB operations and
  resource families for application persistence, bootstrap/table creation, TTL,
  ledger, backfill, dual-write, reconciliation, and notification persistence.
  Design the minimum role boundaries; preserve existing paths or provide their
  tested replacement in the same slice.
- [ ] After explicit fixture authorization, establish endpoint red using an
  isolated role carrying the effective generated runtime policy and synthetic
  canary resources. Prove authentication and allowed persistence first, then
  demonstrate out-of-namespace DynamoDB access succeeds under the broad policy.
  Never access real application data. Record exact resources, account/region,
  cleanup procedure and results; simulation/snapshots alone are insufficient.
- [ ] Replace the broad policy with required operations on account/region-scoped
  target namespace/family ARNs and indexes, plus explicit required legacy tables
  and indexes. Endpoint green must prove allowed persistence/migration flows,
  access to a newly created target table without editing IAM, and AccessDenied
  outside the namespace. Deliver tests and implementation together after full
  checks; preserve legacy and target operation while migration is incomplete.
- [ ] At each environment's cutover remove ordinary runtime legacy access.
  Verify target flows still work and legacy access is denied using synthetic
  fixtures. Keep recovery access separately authorized. Retire temporary
  migration privileges only after their duties stop or move to a tested path.

## Design Decisions
- 2026-10-31: production target names match `coupling-*`; other deployed stages
  match `<stage>-coupling-*`, including indexes. Future tables in an authorized
  namespace/family inherit permissions without a per-table role edit.
- Production legacy `DYNAMO_PREFIX` stays empty. Target and legacy naming are
  distinct during migration; do not redirect legacy reads by changing it globally.
- Namespace permission does not imply every role has every operation. Preserve
  required resource-family boundaries and isolate secret authority; account for
  real cross-family writes such as History. Use stable family prefixes where
  needed rather than enumerating each future table.
- Real AWS endpoint evidence is mandatory before the security policy fix. If
  fixture authorization or provisioning is unavailable, leave implementation
  blocked and do not claim remediation. Local emulation is supplementary.

## Current State
2026-10-31: planning handoff created from security slice 5a. No AWS fixtures or
production policy changes. Next: inventory generated permissions and migration
operations, then present concrete isolated fixtures for authorization.

## Validation
Handoff documentation local links and `git diff --check` passed. Full
`./gradlew check --no-daemon` passed outside the sandbox with a fresh daemon
in 29s (596 tasks: 54 executed, 542 up-to-date); existing test outputs were
reused. No IAM implementation or AWS endpoint evidence is claimed. Handoff
commit: `git log -1 --format=%H -- agents.d/work/DYNAMODB_MIGRATION_IAM.md`.

For implementation record endpoint red/green, focused checks, full check,
separate test/design reviews, complete-slice review, cleanup, and commit IDs.
Link completed evidence back to the security card before closing its finding.
