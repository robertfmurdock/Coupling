# CPL-48 — DynamoDB Migration IAM Boundary

**Work card protocol: 2**

## Goal and Constraints
Replace `AmazonDynamoDBFullAccess` as part of the target-schema rollout without
requiring role edits for each new table. This owns the outstanding DynamoDB
finding transferred from [archived security remediation](../work_completed/CPL-47-SECURITY_CRITICAL_HIGH_REMEDIATION.md),
with implementation now delivered locally. Deployed remediation remains
unverified until an authorized normal deployment. Lambda invocation remains in
that security card.

Follow the [schema proposal](../../DYNAMODB_SCHEMA_PROPOSAL.md) and
[migration strategy](../../DYNAMODB_SCHEMA_MIGRATION_STRATEGY.md). Preserve
application behavior, migration compatibility, and production legacy naming.
This card delivers removal of the broad managed policy while preserving currently
implemented application and migration capabilities. One contributor carries
the policy change, affected execution paths and validation under
the [delivery protocol](../context/WORK_CHECKLIST.md). Migration functionality is
owned by its implementation cards; IAM work does not wait for the entire migration.
No provisioning, deployment, push, release, or AWS profile edits are authorized
by this card. Temporary AWS tables, roles and policies for verification were
explicitly declined; do not provision them or make them a delivery prerequisite.

## Starting Point and Ownership

Before this slice, `server/serverless.yml` attached `AmazonDynamoDBFullAccess`. The archived
security card records a shared Lambda execution role and deployed retention of
that policy; regenerate configuration and verify applicable effective grants
before changing them. Do not assume a source declaration proves deployed access
or that every function already has a separate role.

[Ledger schema compatibility](../work_completed/CPL-46-TARGET_LEDGER_SCHEMA_COMPATIBILITY.md)
and the [party snapshot importer](../work_completed/CPL-50-PARTY_CONFIGURATION_BACKFILL.md)
are delivered. [Party catch-up](../work/CPL-69-PARTY_CONFIGURATION_CATCH_UP.md) is planned;
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
  table/index family and calling path. Retain the sanitized matrix below.
- [x] Add parsed configuration and general scope regression assertions. Use AWS
  IAM simulation for permission decisions rather than maintaining a custom IAM
  evaluator or copying the policy into expected literals. Expected red: the
  generated role still attaches `AmazonDynamoDBFullAccess`.
- [x] Replace the managed policy with the permission contract above. Preserve
  implemented persistence, index access, transactions, bootstrap and TTL paths.
  Deliver tests and policy changes together using existing validation capabilities.
  No temporary AWS infrastructure is part of this slice.

## Acceptance Evidence

- Generated configuration establishes the intended role attachments, operations
  and resource boundaries, including removal of the broad managed policy. Review
  all effective grants; a narrow inline policy cannot cancel another broad allow.
- Policy assertions and available AWS policy validation/simulation establish the
  intended authorization boundary, including allowed operations, out-of-namespace
  denial and future authorized-family table names without a policy edit. Record
  which checks actually ran. Simulation does not prove deployed endpoint behavior;
  keep that limitation explicit rather than creating infrastructure to close it.
- Existing application/repository checks establish behavior preservation. Record
  focused checks, full `./gradlew check`, separate test/design reviews and slice
  commits. Reuse existing delivery evidence for any authorized deployment; do not
  infer deployed remediation from local policy output. Completion requires the
  scoped policy change and the evidence above, not future migration capabilities
  or legacy permission retirement.

Reuse existing tasks and application paths under [tooling purpose and lifetime](../context/GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime).
Do not create a fixture framework or AWS resources solely to produce evidence.

## Current State
2026-11-24: implementation and local acceptance complete. Serverless now includes
`server/deploy/dynamodb-iam.json` in the existing shared role. No additional AWS
resources or CloudShell setup are needed for this change. The CloudFormation
condition preserves the established unprefixed target naming for `prod`; it does
not select feature behavior. Other target names derive from the configured stage,
independently of legacy `DYNAMO_PREFIX`.

No deployment, role mutation or real data operation was performed. The live role
still carries the broad grant until an authorized normal deployment applies this
change. This card's implementation is complete; that deployed security gap is
explicitly unverified. Implementation commit: `git log -1 --format=%H -- agents.d/work_completed/CPL-48-DYNAMODB_MIGRATION_IAM.md`.

## Inventory and Proposed Boundary

Read-only AWS inspection on 2026-11-24 used account `174159267544`, region
`us-east-1`. The deployed `coupling-server-prod` CloudFormation template and live
IAM attachment both retain `AmazonDynamoDBFullAccess`. The role
`coupling-server-prod-us-east-1-lambdaRole` has no permissions boundary; API,
connect, disconnect, message and notify-connect Lambdas all reference it. Its
other inline policies grant logging, connection management and scoped notification
invocation, with no additional DynamoDB grant. These checks inspect infrastructure
metadata, not application records. Fresh candidate packaging and simulation are recorded below.

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

## Acceptance Results

- Red: the parsed YAML test failed at the actual generated `provider.iam` value
  because it still contained the broad managed-policy declaration. Before edits,
  `iam:SimulatePrincipalPolicy` on the deployed role reported `allowed` for
  `GetItem` and `DeleteTable` against an unrelated example ARN. Those operations
  were simulated, never executed.
- Green: two local tests parse generated YAML and the referenced JSON. They guard
  the role wiring, established target-naming condition, absent managed-policy
  attachment, explicit actions and account/region-scoped resources. They do not
  claim to evaluate IAM authorization. Source files are declared Gradle test inputs.
- AWS `SimulateCustomPolicy` matched 88 expected decisions per configured namespace:
  72 allows and 16 implicit denies, 264 total, with no missing context. Allowed
  cases cover the inventory, legacy email indexes, Core condition checks, History,
  TTL and future namespace/family tables. Denials cover unrelated namespaces,
  another account/region, unsupported operations, legacy secret scans, unnecessary
  legacy deletes, index writes and TTL on the wrong family.
- Fresh existing `serverlessPackage` tasks passed for all three deployed
  configurations from fish. Generated CloudFormation contains eight scoped
  DynamoDB statements, no managed-policy attachment, the existing five-function
  shared role and unchanged non-DynamoDB grants. Resolved packaged statements
  exactly match the policies used for simulation. Initial packaging from zsh
  returned `Forbidden`; comparison showed only fish had the Serverless key.
  No credentials or profiles were changed.
- Final `fish -lc './gradlew :coupling-plugins:check check'` passed outside the
  sandbox in 21s: 692 tasks, 60 executed, 632 up-to-date. Existing SDK/E2E results
  were reused. Focused parsed-policy test and generation checks passed earlier.
  `git diff --check` and local documentation links passed.
- Test review: retained assertions parse data and guard general scope properties;
  authorization evidence comes from AWS's simulator, not string matching or a new
  evaluator. Production review: all discovered operations retained, explicit
  legacy/index lists, separate bootstrap/item/TTL authority, family wildcards
  limited to configured account/region, no feature-dependent IAM configuration.
  Complete-slice review found no new infrastructure or migration behavior.

Simulation validates the supplied identity policy, not live service execution,
all account-level controls or a deployed role update. No deployed remediation or
endpoint enforcement is claimed. Temporary extraction/simulation probes remain
outside tracked source; no fixture framework is retained. Local logs are
`/tmp/cpl48-parsed-red.log`, `/tmp/cpl48-green.log`,
`/tmp/cpl48-package-fish.log` and `/tmp/cpl48-final-check.log`.

Resolved packaged policy SHA-256 (compact JSON, account `174159267544`,
region `us-east-1`):

| Configuration | SHA-256 |
| --- | --- |
| prod | `84fd5e2852c621c5be04726cea3e9d317b5e33fd34f3888d62b99d3c3b2a45b5` |
| sandbox | `f5412d5b0a8617df02565e03a35b8b70a2fbddee62acbe5f122a7755bee7aac6` |
| prerelease | `fc51cad6edec5b09400bdf64e7a6a2da133df05a6649777495ee279987bbd420` |
