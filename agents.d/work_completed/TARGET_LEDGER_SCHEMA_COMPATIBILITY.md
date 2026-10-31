# Target Ledger Schema Compatibility

## Goal and Constraints
Reject unsupported migration ledger schemas before catalog selection or bootstrap
can interpret their phase. Preserve durable ledger contents and current routing
for schema version 1. No deployment enablement or GraphQL changes.

## Safe Slice Sequence
- [x] Validate ledger schema on reads. Integration tests exercise existing-state
      lookup and bootstrap with future/missing schema versions; expect today's
      decoder to accept them incorrectly. A shared decoder guard owns the check.
      Independently releasable because it closes an unsafe compatibility gap
      without enabling target request traffic.

## Design Decisions
- The persisted target schema version is authoritative. Unknown or missing versions
  are errors, not reasons to initialize over existing progress or choose legacy.
- Test bootstrap preservation separately from catalog lookup rejection.

## Current State
2026-10-31: delivered by the owner in `f8d0cd1bd` on 2026-10-30. The decoder
and three integration scenarios are committed; the earlier uncommitted/password
blocker below is historical. Follow-on SSM wiring was committed in `be4856be0`;
account-link SDK identity wiring followed in `f92e98ab0`. Full repository check
passed on 2026-10-31 during the IAM documentation handoff (29s, 596 tasks,
54 executed and 542 up-to-date). No deployment enablement or GraphQL changes.
Refresh validation passed; this card is complete and moved to
`agents.d/work_completed/`.

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
- Historical initial full `./gradlew check` failed at `:e2e:e2eRun`: Auth0 reports "Missing required
  parameter: password", then HTTP 401. The shell then lacked
  `COUPLING_E2E_TEST_PASSWORD`; no secret values inspected or substituted.
  Historical log: `/tmp/coupling-ledger-schema-check.log`; browser log:
  `e2e/build/reports/logs/e2e/run.log`. This blocker preceded the commits recorded above.

### 2026-10-31 clarity and consistency refresh
- Re-ran all five `TargetSchemaBootstrapTest` scenarios together with the two
  `DynamoDiscordOAuthRepositoryTest` scenarios and DynamoDB lint; all passed:
  `./gradlew :libraries:repository:dynamo:jsNodeTest --tests '*DynamoDiscordOAuthRepositoryTest*' --tests '*TargetSchemaBootstrapTest*' :libraries:repository:dynamo:lintKotlin --no-daemon`.
- `fish -lc './gradlew check --no-daemon'` passed outside the sandbox with a
  fresh daemon in 6m 19s (596 tasks: 71 executed, 525 up-to-date). Browser E2E
  and structured-log validation passed; unchanged SDK test outputs were reused.
  Log: `/tmp/coupling-oauth-typed-check.log`.
- Companion behavior-preserving refactor types OAuth put/delete request envelopes
  using the established AWS bindings and localizes conditional-error inspection
  there. Existing JSON callers, OAuth keys, conditions, and expiry behavior remain
  unchanged. No new tests or API comments were added for this refactor.
- Documentation now distinguishes shell-supplied primary/alternate SDK passwords
  from the SSM-supplied account-link/E2E identity. Test-clarity review retained
  existing scenarios; production-design review confirmed binding ownership and
  unchanged wire fields/error handling. Complete-slice review and
  `git diff --check` passed. No deployment, provisioning, push, or release.
