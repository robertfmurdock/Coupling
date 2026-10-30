# Target Ledger Schema Compatibility

## Goal and Constraints
Reject unsupported migration ledger schemas before catalog selection or bootstrap
can interpret their phase. Preserve durable ledger contents and current routing
for schema version 1. No deployment enablement or GraphQL changes.

## Safe Slice Sequence
- [ ] Validate ledger schema on reads. Integration tests exercise existing-state
      lookup and bootstrap with future/missing schema versions; expect today's
      decoder to accept them incorrectly. A shared decoder guard owns the check.
      Independently releasable because it closes an unsafe compatibility gap
      without enabling target request traffic.

## Design Decisions
- The persisted target schema version is authoritative. Unknown or missing versions
  are errors, not reasons to initialize over existing progress or choose legacy.
- Test bootstrap preservation separately from catalog lookup rejection.

## Current State
2026-10-30: base/current commit aa869313a; master already ahead of origin by one
existing commit. Uncommitted: decoder guard, three integration scenarios, strategy
clarification, and this card. Focused checks green; full check blocked by absent
`COUPLING_E2E_TEST_PASSWORD`. Asked owner to load it through normal credentials.
Next: rerun full check with credential, review, commit, then complete this card.
Keep this slice open and uncommitted until the full check passes. The owner is
running that check as of the latest update. Follow-on credential work is tracked
in `E2E_PASSWORD_SSM.md`; do not start its implementation before this slice commits.

## Validation
- Red: `./gradlew :libraries:repository:dynamo:jsNodeTest --tests '*TargetSchemaBootstrapTest.*'`
  ran five tests; the three new scenarios failed because unsupported ledgers
  decoded successfully. The initial filter without `.*` matched no tests and
  provided no validation.
- Green: the same five tests plus `:libraries:repository:dynamo:lintKotlin` passed.
- `./gradlew :libraries:repository:dynamo:check` passed. `git diff --check` passed.
- Test review: scenarios cover unsupported and absent versions at the catalog
  lookup boundary, and non-destructive rejection during bootstrap. Existing
  scenarios retain supported-schema initialization and progress preservation.
- Production review: one guard precedes phase decoding; initialization and
  validation share schema version 1. No catch converts incompatibility to absence.
- Full `./gradlew check` failed at `:e2e:e2eRun`: Auth0 reports "Missing required
  parameter: password", then HTTP 401. Current shell has no
  `COUPLING_E2E_TEST_PASSWORD`; no secret values inspected or substituted.
  Log: `/tmp/coupling-ledger-schema-check.log`; browser log:
  `e2e/build/reports/logs/e2e/run.log`. No slice commit yet.
