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

- [x] **2a. Enable local Discord simulation (prerequisite).**
  - Agreed 2026-10-30: local development defaults to simulated Discord; deployed
    environments default to live. Replace only outbound HTTP using the existing
    injected DiscordClient seam; retain real commands and DynamoDB persistence.
  - Deliver a passing simulation/configuration commit before the security slice.

- [x] **2b. Authorize Discord integration replacement (high).**
  - Endpoint red: authenticated nonmember supplies a valid synthetic OAuth code
    for their own Discord destination and victim B's ID. Expect rejection and
    B's existing integration unchanged. Expected current failure: replacement
    is accepted and saved without party authorization.
  - Exercise the HTTP mutation against the real integration command and storage;
    stub only the outbound Discord service using an established test seam.
    A local in-memory repository that bypasses code exchange cannot prove this.
    Cover authorized setup/replacement and denied unauthenticated requests.
  - Contract/design: resolve authorized target before external exchange or save.
    Preserve the existing membership policy and GraphQL/SDK contract.
  - Agreed 2026-10-30: OAuth state hardening is a separate follow-up slice 2c.
  - Boundary: GraphQL endpoint, Discord exchange, party integration persistence,
    and OAuth callback flow where its contract changes.
  - Independently releasable: prevents nonmembers redirecting another party's
    announcements while retaining authorized integration setup.

- [ ] **2c. Harden Discord OAuth state (follow-up).**
  - Current callback trusts an unsigned partyId in URL state. Bind initiation and
    callback to the initiating user and party with validated, expiring state.
  - Plan the initiation/callback contract and synchronized client/server/SDK
    changes separately; authorization slice 2b does not close this work.

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
- SDK endpoint tests must use HTTP commands/queries exclusively; no direct
  database access or repository dependencies. For Discord, observe announcement
  routing via simulated destination-bearing message IDs on pairing-set queries.
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
- Slice 2a complete in `c8c9fe8ea`: local Discord simulation. Slice 2b complete:
  HTTP-only endpoint red confirmed and authorization fix validated. OAuth state
  remains open (2c), followed by account-link token remediation (3).
- Slice 1 tests belong to existing `SdkSecretTest.kt` and `SdkPinTest.kt`. Typed operations
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
  isolated AWS test environment authorization for slice 5. Discord now uses the
  existing injected HttpClient seam with a simulated HTTP engine for local tests.

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

### Slice 2a: local Discord simulation

- Mode-selection red:
  `fish -lc './gradlew :server:discord:jsNodeTest --tests "*DiscordModeTest*" --no-daemon'`
  ran four tests against the initial live-only selection scaffold. The two live
  cases passed; simulated selection and unknown-mode rejection failed.
- Green/lint:
  `fish -lc './gradlew :server:discord:jsNodeTest :server:discord:lintKotlin :server:lintKotlin --no-daemon'`
  passed after correcting wrapping lint. Coverage includes deterministic code
  exchange, malformed codes, and message send/update/delete through DiscordClient.
- Test-clarity review: explicit mode cases and exchange/message scenarios exercise
  the public Discord client boundary; existing wire-format tests remain intact.
- Production-design review: Config selects live/simulated HTTP transport; both use
  ClientDiscordRepository and real storage. Simulated operations are stateless,
  documented, and never fall through to a network engine. No authorization fix
  is included in this prerequisite slice.
- Full gate: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox with a fresh daemon in 6m 32s; 587 actionable tasks (75 executed,
  512 up-to-date). All 12 Discord tests passed; SDK tests reused cached results,
  and browser E2E executed successfully against the rebuilt local server.
- Complete-slice review: no schema/SDK/persistence-policy changes; live remains
  the default outside explicit local configuration. `git diff --check` passed.
  Tests, implementation, documentation, and this card are committed together;
  obtain the commit with Git history. Not pushed or released.

### Slice 2b: authorize Discord integration replacement

- User corrected the initial test design: SDK endpoint tests must not query the
  database directly. Removed the DynamoDB dependency/settings and repository
  assertions. The initial storage-based red/green is superseded by endpoint-only
  tests; a full check started for that design was stopped before completion.
- Revised contract: after a denied replacement, the victim owner spins again and
  queries current pair assignments. Its simulated Discord message ID must identify
  the original destination; authorized replacement must deliver to the new one.
- Simulator observation seam red:
  `fish -lc './gradlew :server:discord:jsNodeTest --tests "*SimulatedDiscordTest*" --no-daemon'`
  ran three tests, with the destination-bearing message-ID assertion failing.
  Message IDs now include the destination plus the existing request-derived hash.
- Endpoint-only red:
  `fish -lc './gradlew :sdk:jsNodeTest --tests "*SdkDiscordTest*" :server:discord:jsNodeTest :sdk:lintKotlin :server:discord:lintKotlin --no-daemon'`
  passed the simulator tests/lint and four SDK scenarios; the nonmember scenario
  failed because the victim owner's next announcement identified the attacker's
  destination. Authorization code was restored to the original unrestricted
  resolver for this run. No GraphQL schema additions are needed: existing SDK
  commands and the current pairing-set query expose announcement delivery.
- Test-clarity review: setup proves independent parties and delivery to the victim
  destination. Exercise attempts replacement, then the owner requests a fresh
  announcement and reads it through GraphQL. Verification checks rejection and
  original routing. Positive install/replacement and denied unauthenticated/invalid
  exchange scenarios use the same observable boundary. Only Discord HTTP is doubled.
- Production design: obtain authorizedPartyDispatcher for input partyId before
  firing the existing grant command. Reuse existing membership semantics; no SDK
  command, schema, or client callback changes. OAuth state remains slice 2c.
- Green/lint:
  `fish -lc './gradlew :sdk:jsNodeTest --tests "*SdkDiscordTest*" :server:discord:jsNodeTest :sdk:lintKotlin :server:lintKotlin :server:discord:lintKotlin --no-daemon'`
  passed all five endpoint scenarios and affected lint; simulator tests also passed
  (up-to-date on this run after passing in the revised red run).
- Full gate: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox with a fresh daemon in 6m 9s; 578 actionable tasks (68 executed,
  510 up-to-date). Complete SDK Node/JVM suites and browser E2E executed; all five
  new Discord scenarios passed on both Node and JVM.
- API-only SDK test convention recorded in `agents.d/context/TESTING.md`.
- API usability gap: the existing API exposes announcement message IDs but no
  direct read of the configured Discord destination. These tests demonstrate the
  install/announce/query workflow using simulated IDs to identify delivery; they
  do not establish live configuration-inspection capability. Consider an
  authorized, non-secret integration summary as a follow-up with its own permission
  contract/tests. User's broader API direction is recorded in the architecture
  guidance: easy to use, discoverable, and informative without exposing credentials
  or compromising meaningful security.
- Complete-slice review: no SDK dependency/build changes remain. Tests use existing
  commands/queries, with destination-bearing simulated IDs as the sole observation
  addition. Resolver authorization precedes exchange/save; public contracts and
  membership policy are unchanged. `git diff --check` passed.
- Delivered with tests, implementation, and guidance in the slice commit (obtain
  its ID from Git history for this card). Not pushed, released, or tested against
  production Discord. Later security slices remain open.

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
