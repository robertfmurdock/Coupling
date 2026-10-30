# Target Party Configuration Hardening

## Goal
Harden the target-only party configuration storage proof so its read-after-write,
concurrency, and transaction-atomicity guarantees are deterministic and testable.

## Deferred Scope
- Catalog or production routing
- Party owner relationships
- Party deletion
- Backfill or dual-write migration
- Durable mutation idempotency receipts

## Checklist
- [x] Inject the existing `DynamoDBSyntax` seam, defaulting to `DynamoDbProvider`
- [x] Use strongly consistent keyed `GetItem` metadata reads
- [x] Retry only a Core version-condition transaction cancellation, with bounded exponential delay
- [x] Add deterministic transport coverage for stale-version retry and direct read shape
- [x] Add integration coverage proving a History key collision cannot commit Core metadata
- [x] Run target tests and Dynamo lint
- [x] Move this work card to `agents.d/work_completed/`

## Validation
- `./gradlew :libraries:repository:dynamo:lintKotlin :libraries:repository:dynamo:jsNodeTest --tests '*TargetPartyConfigurationRepositoryTest'`

