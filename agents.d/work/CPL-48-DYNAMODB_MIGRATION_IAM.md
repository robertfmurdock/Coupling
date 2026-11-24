# CPL-48 — DynamoDB Migration IAM Boundary

**Work card protocol: 2**

## Goal and Constraints
Replace `AmazonDynamoDBFullAccess` as part of the target-schema rollout without
requiring role edits for each new table. This owns the outstanding DynamoDB
finding transferred from [archived security remediation](../work_completed/CPL-47-SECURITY_CRITICAL_HIGH_REMEDIATION.md),
not a completed remediation. Lambda invocation remains in that security card.

Follow the [schema proposal](../../DYNAMODB_SCHEMA_PROPOSAL.md) and
[migration strategy](../../DYNAMODB_SCHEMA_MIGRATION_STRATEGY.md). Preserve
application behavior, migration compatibility, and production legacy naming.
This card delivers removal of the broad managed policy while preserving currently
implemented application and migration capabilities. One contributor carries
the policy change, affected execution paths and validation under
the [delivery protocol](../context/WORK_CHECKLIST.md). Migration functionality is
owned by its implementation cards; IAM work does not wait for the entire migration.
No provisioning, deployment, push, release, or AWS profile edits are authorized
by this card. Prepare concrete isolated fixtures before requesting authorization.

## Starting Point and Ownership

`server/serverless.yml` still attaches `AmazonDynamoDBFullAccess`. The archived
security card records a shared Lambda execution role and deployed retention of
that policy; regenerate configuration and verify applicable effective grants
before changing them. Do not assume a source declaration proves deployed access
or that every function already has a separate role.

[Ledger schema compatibility](../work_completed/CPL-46-TARGET_LEDGER_SCHEMA_COMPATIBILITY.md)
and the [party snapshot importer](../work_completed/CPL-50-PARTY_CONFIGURATION_BACKFILL.md)
are delivered. [Party catch-up](CPL-69-PARTY_CONFIGURATION_CATCH_UP.md) is planned;
online migration catalogs, coordinated writes and freshness detection remain
implementation work. Inventory implemented paths now and update permissions with
later capabilities rather than granting speculative operations in advance.

## Permission Contract

Use one permission-generation contract with configured resource namespaces under
the [application configuration contract](../context/ARCHITECTURE_CANONICAL.md#build-and-deployment).
Incubation uses `ENABLE_PRERELEASE_FEATURES`; promotion makes migration behavior
unconditional. Neither requires environment-specific feature configuration or
rollout policies.

- Scope target table/index ARNs to the configured account, region and stage:
  production `coupling-*`, other deployed stages `<stage>-coupling-*`. A new table
  within an authorized resource family needs no per-table IAM edit. Preserve
  role-specific families and secret authority; a namespace match alone does not
  justify access for every handler. Account for cross-family History transactions.
- Keep explicit required legacy table/index access during coexistence. Production
  legacy `DYNAMO_PREFIX` remains empty; target naming is resolved independently.
  Include persistence, notification storage, bootstrap/table creation, TTL, ledger,
  import, reconciliation and recovery operations wherever actually executed.
- Separate migration privileges only where duties require it and a validated
  replacement execution path exists. Narrowing IAM must not strand lazy table
  initialization or existing import/recovery paths.
- Permissions support implemented capabilities independently of the incubating
  flag. Do not add flag-driven IAM selection or a new migration control.

Target-only permission retirement belongs to the future target-only transition,
under the migration strategy. It is outside this card and is not a completion gate.

## Delivery Slice — Restrict Current Permissions

- [x] Inventory generated policies and actual operations by execution role,
  table/index family and calling path. Identify any required replacement path
  before removing its permissions. Retain a concise sanitized operation/resource
  matrix and the proposed fixture resources, role, account/region and cleanup.
- [ ] After fixture authorization, prove authentication and allowed synthetic
  persistence, then demonstrate that the current broad policy permits an
  out-of-namespace DynamoDB operation. This successful operation is the expected
  security red; an authentication or missing-resource error is not evidence.
- [ ] Replace the managed policy with the permission contract above. Test generated
  policy scope and effective endpoint behavior together, including legacy and
  target persistence, index access, cross-table transactions, implemented migration
  operations, a newly created authorized-family table without a policy edit,
  and denial outside the namespace and across unauthorized role/stage boundaries.
  Deliver tests and policy changes together. This slice independently removes the
  broad grant while keeping current application and migration paths operational.

## Acceptance Evidence

- Generated configuration establishes the intended role attachments, operations
  and resource boundaries, including removal of the broad managed policy. Review
  all effective grants; a narrow inline policy cannot cancel another broad allow.
- Mandatory isolated AWS endpoint red/green establishes actual authorization.
  Use only synthetic resources and record the policy/configuration revision,
  assumed identity, exact operations/resources, results and cleanup. Never read
  real application data. Simulation and local DynamoDB checks are supplementary;
  ordinary build/deployment health cannot prove an IAM denial boundary. If fixture
  authorization is unavailable, continue read-only preparation but leave the
  policy change undelivered and remediation unclaimed.
- Existing application/repository checks establish behavior preservation. Record
  focused checks, full `./gradlew check`, separate test/design reviews and slice
  commits. Reuse existing delivery evidence for any authorized deployment; do not
  infer deployed remediation from local policy output. Completion requires the
  scoped policy change and the evidence above, not future migration capabilities
  or legacy permission retirement.

Reuse existing tasks and application paths. Keep one-off endpoint probes temporary
and retain sanitized findings; evidence requirements do not authorize a maintained
fixture framework. Apply [tooling purpose and lifetime](../context/GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime)
before retaining automation.

## Current State
2026-11-24: inventory complete; the DynamoDB security finding remains outstanding.
No AWS fixtures or policy changes have been made. Required endpoint red is pending
fixture authorization and a provisioning/execution identity; the current session
is `AWSReservedSSO_ReadOnlyAccess_8f5ea127031c9918`. Do not change profiles to
work around that boundary. Next: authorize/provision the fixtures below, establish
endpoint red, then implement the smallest scoped replacement and endpoint green.

## Inventory and Proposed Boundary

Read-only AWS inspection on 2026-11-24 used account `174159267544`, region
`us-east-1`. The deployed `coupling-server-prod` CloudFormation template and live
IAM attachment both retain `AmazonDynamoDBFullAccess`. The role
`coupling-server-prod-us-east-1-lambdaRole` has no permissions boundary; API,
connect, disconnect, message and notify-connect Lambdas all reference it. Its
other inline policies grant logging, connection management and scoped notification
invocation, with no additional DynamoDB grant. These checks inspect infrastructure
metadata, not application records. Fresh local Serverless packaging has not been
performed; verify the candidate generated template before accepting implementation.

The immediate design keeps the existing shared execution role. There is no
existing per-handler DynamoDB separation to preserve; introducing one is not
necessary to remove the broad managed policy. Preserve existing non-DynamoDB
policies and do not grant other roles the same storage authority by default.

| Implemented owner/path | Resources | Required DynamoDB IAM actions |
| --- | --- | --- |
| Legacy repository initialization | Configured legacy `TRIBE`, `USER`, `PLAYER`, `PIN`, `PAIR_ASSIGNMENTS`, `CONTRIBUTION`, `BOOST`, `SECRET`, `SLACK`, `DISCORD`, `LIVE_CONNECTION` | `CreateTable`, `DescribeTable` |
| Legacy revision persistence and lookup | Those explicit legacy tables; `USER/USER_EMAIL_INDEX` and `PLAYER/PlayerEmailIndex` for index queries | `PutItem`, `Query`; scope each action to actual owners when constructing statements |
| Legacy discovery | `TRIBE`, `USER`, `PLAYER` | `Scan` |
| Physical deletion/receipt consumption | `CONTRIBUTION`, `LIVE_CONNECTION`, `DISCORD` | `DeleteItem` |
| User identity transaction | `USER` | `PutItem` on both transaction items |
| Target bootstrap | Configured target Core, Activity, History and Live Connections tables, including creation of `byConnection` | `CreateTable`, `DescribeTable` |
| Target TTL initialization | Target Live Connections | `DescribeTimeToLive`, `UpdateTimeToLive` |
| Ledger and target party configuration/import | Target Core and History; legacy `TRIBE` source | Core `GetItem`, `PutItem`, `DeleteItem`, `ConditionCheckItem`; History `Query`, `PutItem`; source `Query` |

Operation discovery: repository implementations and shared syntax under
`libraries/repository/dynamo/src/jsMain/kotlin`, especially
`MigrationSchemaBootstrap`, `DynamoPartyConfigurationBackfill`,
`TargetPartyConfigurationRepository` and `DynamoUserRepository`.
PartiQL and batch-get helpers have no discovered application callers; their mere
existence does not justify permissions. No discovered application path requires
`DeleteTable`, `ListTables`, backups, exports, streams or `UpdateItem`.
[AWS transaction authorization](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/transaction-apis-iam.html)
uses underlying item permissions, including `ConditionCheckItem` for ledger guards.
Keep control-plane grants separate from item/index grants; avoid a blanket
`dynamodb:*` replacement. Target resource families remain extensible under the
existing namespace contract; new capabilities add necessary operations with tests.

## Isolated Endpoint Fixture Proposal — Not Yet Authorized

Use account `174159267544`, region `us-east-1`, synthetic namespace `cpl48`.
Before creation, check exact names are unused; stop on collision rather than
reusing any existing resource. This namespace tests configuration substitution,
not an additional application environment or feature setting.

- Temporary role `CouplingCpl48DynamoCanary`, trusted only by the specifically
  authorized operator identity, with short sessions and no application-role trust.
  Use a permissions boundary limiting all DynamoDB authority to the exact fixture
  table/index ARNs below. It must prevent the broad baseline managed policy from
  granting access to real data or unrelated AWS services. The operator identity
  and corresponding trust policy remain to be supplied before provisioning.
- Tables `cpl48-TRIBE`, `cpl48-USER` (with `USER_EMAIL_INDEX`),
  `cpl48-coupling-core`, `cpl48-coupling-activity`, `cpl48-coupling-history`,
  `cpl48-coupling-live-connections` (with `byConnection` and TTL),
  `cpl48-coupling-future-canary`, and negative controls `cpl48-outside-canary`
  and `cpl48-other-coupling-canary`. Use on-demand billing and synthetic data only.
  Source tables use the actual legacy key shapes; target tables use `PK`/`SK`.
- Baseline: attach the current effective DynamoDB managed policy to the bounded
  canary role. Establish allowed persistence and successful negative-control
  operations. Candidate: remove that attachment and install the generated scoped
  DynamoDB statements with fixture namespace substitution. The unchanged boundary
  allows both controls, so their subsequent denial must come from the candidate.
- Exercise table creation/description, index queries, physical deletion, TTL,
  conditional Core/History transactions, ledger bootstrap and named-party snapshot
  import through existing paths where possible. Create the future-family canary
  only after installing the candidate, without editing its permissions. Compare
  preserved imported history/current metadata, not only API status codes.
- The operator handles fixture deletion, not the candidate runtime policy. Cleanup:
  delete all nine named tables, detach the baseline managed policy if attached,
  delete candidate inline policies, remove the role and its boundary policy.
  Verify table absence and IAM cleanup. Do not grant canary `DeleteTable` merely
  to make cleanup convenient. Record failures and any remaining resources.

Prepare one-off requests/probes in temporary storage and retain only sanitized
results here. No new maintained fixture runner is proposed. This proposal does
not authorize creation, role assumption, policy mutation or deployed-data access.

## Validation
2026-11-24 inventory handoff: local links and `git diff --check` passed.
Full `./gradlew check` passed outside the sandbox in 6s (689 tasks: 56 executed,
633 up-to-date). Existing integration/browser results were reused. Review of
the operation inventory and fixture design found that bootstrap also needs the
Activity fixture; it is included above. No endpoint red/green or remediation is
claimed. Investigation commit: `git log -1 --format=%H -- agents.d/work/CPL-48-DYNAMODB_MIGRATION_IAM.md`.

Historical handoff documentation local links and `git diff --check` passed. Full
`./gradlew check --no-daemon` passed outside the sandbox with a fresh daemon
in 29s (596 tasks: 54 executed, 542 up-to-date); existing test outputs were
reused. No IAM implementation or AWS endpoint evidence is claimed. Handoff
commit: `git log -1 --format=%H -- agents.d/work/CPL-48-DYNAMODB_MIGRATION_IAM.md`.

This is the sole active owner of the remaining DynamoDB finding. Record completion
evidence here; the archived security card is historical context and adds no
separate closeout gate.
