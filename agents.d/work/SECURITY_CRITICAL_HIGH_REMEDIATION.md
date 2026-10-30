# Critical and High Security Remediation

## Goal and Constraints

Address all critical and high findings from the 2026-10-30 source review:
cross-party authorization reuse, unauthorized Discord integration replacement,
reusable account-link tokens, player administration privileges (policy-dependent),
and excessive runtime AWS permissions. Review findings are hypotheses until their
endpoint reproductions establish the behavior; do not describe them as verified
production exploits.

Every finding requires a meaningful failing endpoint test before its production
fix. Exercise the real authentication, routing, resolver, authorization, and
persistence boundaries; dispatcher-only tests or source/configuration assertions
do not satisfy this requirement. Assert both rejection and absence of unauthorized
state changes or disclosures. Preserve positive authorized scenarios so unrelated
authentication/setup failures cannot masquerade as a fix.

Use isolated local/test fixtures and synthetic identities, parties, OAuth codes,
and AWS resources. Do not attack production or print credentials. Any required
deployment or external resource provisioning must have explicit authorization.
No pushing or releasing is authorized by this card. Medium findings from the
review remain outside this card: URL token logging and GraphQL work budgets.

## Safe Slice Sequence

- [x] **1. Bind authorization to the requested party (critical).**
  - Endpoint red: authenticated caller controls A but cannot access B. Send one
    HTTP GraphQL mutation with ordered fields first targeting A and then creating
    a secret for B. Expect the second field to be denied, no B secret saved, and
    no usable B token returned. Expected current failure: B secret creation
    succeeds because A's cached dispatcher authorizes B's command.
  - Cover legitimate multi-party operations for callers authorized for both
    parties, and check affected read/write paths for target confusion. Include
    a second explicit-target command where needed to constrain the shared flaw.
  - Contract/design: cache dispatchers by PartyId (or remove the cache); ensure
    authorization and command execution refer to the same target. Trace commands
    that use input partyId versus currentPartyId before choosing the smallest fix.
  - Boundary: HTTP GraphQL through DispatcherProviders, CommandDispatcher,
    party command execution, and repository effects. Include required SDK GraphQL
    documents in this slice.
  - Independently releasable: closes cross-party privilege escalation while
    preserving legitimate single- and multi-party requests.

- [ ] **2. Authorize Discord integration replacement (high).**
  - Endpoint red: authenticated nonmember supplies a valid synthetic OAuth code
    for their own Discord destination and victim B's ID. Expect rejection and
    B's existing integration unchanged. Expected current failure: replacement
    is accepted and saved without party authorization.
  - Exercise the HTTP mutation against the real integration command and storage;
    stub only the outbound Discord service using an established test seam.
    A local in-memory repository that bypasses code exchange cannot prove this.
    Cover authorized setup/replacement and denied unauthenticated requests.
  - Contract/design: resolve authorized target before external exchange or save;
    bind OAuth initiation/callback to initiating user and party with validated,
    expiring state. Inspect existing client state handling and SDK contracts;
    include necessary client/server/schema changes together. Separate additional
    OAuth hardening into a revised slice if it is independently releasable.
  - Boundary: GraphQL endpoint, Discord exchange, party integration persistence,
    and OAuth callback flow where its contract changes.
  - Independently releasable: prevents nonmembers redirecting another party's
    announcements while retaining authorized integration setup.

- [ ] **3. Consume account-link tokens once (high).**
  - Endpoint red: generate a link token as target user, redeem through the HTTP
    API as account A, then redeem the same token as account B. Expect only A to
    link; B receives rejection and neither side gains B's connection. Expected
    current failure: the second redemption succeeds within the token lifetime.
  - Also demonstrate replay after disconnect, and concurrent redemption with
    separate authenticated requests: exactly one winner and symmetric links.
    Use production-equivalent persistent storage for the atomicity proof;
    process-local locking/in-memory tests alone are insufficient.
  - Contract/design: atomically consume the pending secret and establish the
    relationship, including both users' records. Ensure retries/conflicts cannot
    restore a consumed secret or leave one-sided links. Review replacement and
    expiration behavior and preserve successful first redemption.
  - Boundary: authenticated GraphQL account-link commands, signed token
    validation, user repository conditional/transactional writes.
  - Independently releasable: makes the existing single-use promise true without
    changing already established relationships.

- [ ] **4. Enforce the agreed owner/player permission contract (high, conditional).**
  - First resolve product policy with the user: current code deliberately accepts
    player membership for the common party gate. Do not silently redefine all
    player mutation permissions. Record the agreed capability matrix here before
    writing denial assertions; include owners, connected accounts, players,
    nonmembers, and party API secrets.
  - Endpoint red under that contract: player attempts party deletion and API
    secret issuance. Expect denial, party retained, and no credential issued.
    Expected current failure: both operations are allowed by the membership gate.
    Add integration administration cases if reserved to owners by agreed policy.
  - Cover allowed player operations and owner success, including connected-owner
    identities and the agreed scope of existing API secrets.
  - Contract/design: server-owned capability checks for administrative operations;
    synchronize any affected client affordances and SDK/schema contracts. Decide
    how previously issued player credentials are handled; do not imply that new
    issuance checks revoke existing credentials.
  - Boundary: HTTP authorization and all affected administration commands.
  - Independently releasable once policy is agreed: removes excess privileges
    while preserving explicitly supported player workflows.

- [ ] **5. Restrict runtime AWS permissions (high blast-radius risk).**
  - Endpoint red: use an isolated test role carrying the effective generated
    runtime policy to call AWS endpoints against disposable canary resources
    outside the allowed stage. Assert AccessDenied for out-of-scope DynamoDB
    access and unrelated Lambda invocation; expected current failure for DynamoDB
    is success under AmazonDynamoDBFullAccess. Verify whether the legacy wildcard
    invocation declaration actually enters the generated policy before claiming
    its exposure. Never invoke arbitrary existing functions or access real data.
  - This is an AWS service endpoint test, not a GraphQL exploit: no application
    endpoint currently identified grants callers arbitrary AWS SDK operations.
    If the required isolated role/resources cannot be authorized or provisioned,
    leave this slice blocked before production edits. Policy snapshots and IAM
    simulation can supplement but cannot replace the endpoint demonstration.
  - Contract/design: scope DynamoDB resources/actions to the stage's required
    tables/indexes and runtime operations; invocation only to notifyConnect.
    Inventory migration/table-creation needs before removing permissions, and
    separate deploy/migration privileges from runtime where necessary.
  - Verify permitted application persistence and notification flows using the
    narrowed role. Preserve runtime migration behavior or include its safe
    replacement in this slice. Do not modify AWS profiles.
  - Boundary: generated Serverless IAM policy, AWS DynamoDB/Lambda endpoints,
    runtime persistence and notification paths.
  - Independently releasable after endpoint validation: reduces compromise scope
    without breaking required runtime operations.

## Design Decisions

- User requires endpoint demonstration before fixing every critical/high finding.
- Execute dependent TDD steps with one contributor. Before test edits, visibly
  state behavior, boundary, contract, scenarios, and expected failure. After
  meaningful red and before production edits, state smallest implementation,
  ownership, and data flow. Revise this sequence for material scope changes.
- Endpoint fixtures must prove prerequisites (authentication, party isolation,
  valid OAuth exchange or AWS permissions) rather than fail on setup errors.
- Existing test starting points: `sdk/src/commonTest/kotlin/com/zegreatrob/coupling/sdk/`
  (`RequestCombineEndpointTest.kt`, `SdkSecretTest.kt`, `SdkUserTest.kt`,
  `SdkPartyTest.kt`), plus `sdk/src/jsTest/.../SlackCommandTest.kt` for HTTP tests.
- Key implementation paths: `server/.../CommandDispatcher.kt`,
  `server/.../graphql/DispatcherProviders.kt`, `server/.../entity/discord/`,
  `server/actionz/.../action/{discord,user}/`, `libraries/repository/dynamo/`,
  and `server/serverless.yml`. Paths with ellipses are discovery hints.
- Load GraphQL/code-style guidance before relevant edits; Gradle and AWS failure
  guidance before build/infrastructure work. Repository automation belongs in
  Gradle tasks; no ad hoc test/deployment shell scripts.

## Current State

- Date: 2026-10-30. Starting commit: `be4856be0d8608e99c9498d34214cd7dfd90f1ab`.
- Slice 1 complete: endpoint red confirmed; party-keyed dispatcher cache
  implemented; focused green/lint and full-suite validation passed. Tests, fix,
  and this card are delivered together in the slice commit (see Git history).
- Next slice: unauthorized Discord integration replacement. No work on slice 2
  has started.
- Tests belong to existing `SdkSecretTest.kt` and `SdkPinTest.kt`. Typed operations
  come from `sdk/src/commonMain/graphql/test-mutations.graphql` and execute through
  the SDK's existing authenticated Apollo HTTP performer. A test-only helper in
  `AuthorizedSdk.kt` exposes that path; no hand-built request strings remain.
- Use `fish -lc` for Gradle to inherit the SDK test passwords. Initial zsh
  authentication failures were setup failures, not meaningful security red.
  Fish authentication works without AUTH0_CLIENT_SECRET in this environment.
- Target-flow review: create/delete secret, save Slack integration, and save
  pair assignments use command partyId; save/delete player/pin and party deletion
  use dispatcher currentPartyId. The shared cache fix binds both patterns to the
  requested party without changing schemas or membership policy.
- Prerequisites for later slices: owner/player capability agreement for slice 4;
  isolated AWS test environment authorization for slice 5. Discover the Discord
  outbound test seam before slice 2 and record the approach.

## Validation

### Slice 1

- Red (final typed-operation tests):
  `fish -lc './gradlew :sdk:jvmTest --tests "*SdkSecretTest*" --tests "*SdkPinTest*" --no-daemon'`
  ran 14 tests; 12 passed and the two expected regressions failed. The caller
  could not access B before the mutation, but B's owner read back the newly
  issued unauthorized secret. Two authorized pin saves both landed in A.
- Earlier raw-HTTP reproductions established the same failures; replaced them
  with typed generated operations and repeated red on unchanged production code.
- Test review: command behavior remains in existing command suites; setup
  establishes independent owners, exercise sends one ordered mutation and reads
  storage through owners' endpoints, verify checks denied return and no B write.
  Positive secret and pin scenarios constrain legitimate multi-party behavior.
- Production design review: cache remains request-owned and asynchronous, keyed
  by PartyId just like the existing player cache. Authorization still runs for
  each requested party. No endpoint, schema, or capability-policy changes.
- Green/lint:
  `fish -lc './gradlew :sdk:jvmTest --tests "*SdkSecretTest*" --tests "*SdkPinTest*" :sdk:lintKotlin :server:lintKotlin --no-daemon'`
  passed all 14 endpoint tests and affected lint tasks.
- Full gate: `fish -lc './gradlew check --no-daemon'` passed outside the sandbox
  with a fresh daemon, including SDK Node/JVM, server, and browser E2E checks.
  Duration 6m 53s; 587 actionable tasks (94 executed, 493 up-to-date).
- Complete-slice review: three endpoint scenarios cover unauthorized explicit
  targets, authorized multi-party secret issuance, and dispatcher-current-party
  pin writes. No public API, schema, or player/owner policy changes. Other
  critical/high slices remain independently queued. `git diff --check` passed.
- Commit: the slice commit containing this completed checklist and validation;
  obtain its ID with `git log -1 --format=%H -- agents.d/work/SECURITY_CRITICAL_HIGH_REMEDIATION.md`.
  Not pushed or released.
- No production deployment was tested or performed. Later findings remain open.

### Delivery gates

- Record exact red/green commands, test-clarity and production-design reviews,
  complete-slice review, full-check outcome, and commit ID per slice.
- Run `./gradlew check --no-daemon` outside the sandbox via the approved execution
  mechanism from its first attempt, per GRADLE_PLAYBOOK.md. Commit passing tests
  and implementation together before starting the next slice. Record blockers
  instead of calling an unverified slice release-safe.
- At completion review all five findings against endpoint evidence, record any
  deployment-only gaps and credential-remediation follow-up, and move this card
  to `agents.d/work_completed/`. Pushing/releasing requires user authorization.
