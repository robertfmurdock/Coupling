# Critical and High Security Remediation

## Goal and Constraints

Address all critical and high findings from the 2026-10-30 source review:
cross-party authorization reuse, unauthorized Discord integration replacement,
reusable account-link tokens, player administration privileges (policy-dependent),
and excessive runtime AWS permissions. Review findings are hypotheses until their
endpoint reproductions establish the behavior; do not describe them as verified
production exploits.

Application findings require a meaningful failing endpoint test before their
production fix. Lambda IAM follows the revised normal-deployment validation
decision recorded below, without isolated AWS fixtures. Exercise the real
authentication, routing, resolver, authorization, and
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

- [x] **2c. Harden Discord OAuth state (follow-up).**
  - Current callback trusts an unsigned partyId in URL state. Bind initiation and
    callback to the initiating user and party with validated, expiring state.
  - Agreed: authenticated initiation returns opaque ten-minute state bound to the
    user, party, and a SHA-256 challenge of a browser-generated verifier. Completion
    rechecks party access and atomically consumes state before external exchange.
  - Reuse the stage-prefixed DISCORD table with reserved OAuth partitions and a
    fixed sort key; no new table, key-schema migration, or table-wide TTL change.
    Expired records are removed when encountered; abandoned records otherwise remain.
  - Synchronize required state/verifier fields across commands, GraphQL, SDK, and
    client. Older SDK calls and already-open installation flows must restart.
  - Browser verifier stays in tab-local sessionStorage; multiple pending flows
    remain independent. Failed code exchange consumes state and requires restart.

- [x] **3. Consume account-link tokens once (high).**
  - Endpoint red: generate a link token as target user, redeem through the HTTP
    API as account A, then redeem the same token as account B. Expect only A to
    link; B receives rejection and neither side gains B's connection. Expected
    current failure: the second redemption succeeds within the token lifetime.
  - Also demonstrate replay after disconnect. Use production-equivalent
    persistent storage to prove single-use consumption and atomic relationship
    writes; process-local locking/in-memory tests alone are insufficient.
  - **Scope correction, 2026-10-30:** the concurrency-hardening suggestion is
    struck. The highlighted security risk is reuse of an already redeemed token,
    including after disconnect; it is not unrelated overlapping edits by an
    already authenticated identity. General user-write versioning, automatic
    retries, and guarantees that competing user edits all succeed are not targets.
    Simultaneous use of the same token may check the atomic single-use guarantee;
    it does not establish a broader concurrency-protection requirement.
  - Contract/design: record token consumption and both users' relationship writes
    in one transaction. A consumed-token marker in the existing USER table rejects
    replay, including if an old user snapshot is later saved again. Preserve
    existing first redemption, replacement, expiry, normal saves, and disconnect
    behavior; no new table or general user-state coordination is required.
  - Boundary: authenticated GraphQL account-link commands, signed token
    validation, user repository conditional/transactional writes.
  - Independently releasable: makes the existing single-use promise true without
    changing already established relationships.

- [x] **4. Resolve owner/player permissions: retain current policy.**
  - Agreed 2026-10-31: current owner/player permissions are intentional, including
    player administration. Connected-account access, nonmember rejection, and
    existing party API-secret scope remain unchanged. No revocation or permission
    changes are requested. This is a resolved product-policy question, not a
    demonstrated security violation; do not manufacture denial tests.

- [ ] **5a. Restrict DynamoDB permissions: transferred, still outstanding.**
  - Owned by [DynamoDB migration IAM](DYNAMODB_MIGRATION_IAM.md), coordinated with
    the target-table rollout. Production `coupling-*`, other stages
    `<stage>-coupling-*`; new tables within authorized namespaces/families need
    no per-table IAM edit. Preserve legacy naming during migration.
  - The linked card carries endpoint red/green, transitional legacy permissions,
    bootstrap/migration compatibility, and per-environment cutover retirement.
    Transfer is not remediation; keep this finding open until evidence is linked.

- [ ] **5b. Review and restrict Lambda invocation permissions.**
  - Local packaging on 2026-10-31 confirmed the wildcard in generated IAM;
    see authenticated local packaging evidence below. User subsequently approved
    config correction followed by normal deployment and notification verification,
    replacing isolated AWS fixtures. Deployed verification remains outstanding.
  - Inspect generated CloudFormation through the existing Gradle packaging path
    before claiming the legacy wildcard declaration is effective.
  - Revised by user: use generated-policy evidence for the original broad grant,
    make the config correction, deploy through the normal Coupling workflow,
    and confirm or deny the intended notification flow works. No isolated role
    or unrelated canary invocation is required. Never invoke arbitrary functions.
  - Define any required invocation grant with a generated reference to this
    stack's `notifyConnect` function. If the wildcard is absent, record the
    exposure as unconfirmed and treat a missing grant as a functional correction.
  - Verify the deployed role has the scoped grant and the authorized notification
    flow works. Record that unrelated-function denial was not endpoint-tested;
    normal-flow success alone does not prove that denial. No fixture provisioning.
  - Boundary: generated Serverless IAM policy, AWS Lambda endpoints, notification
    flow. Normal deployment is the agreed experiment; do not edit AWS profiles.
    Earlier local-only restrictions are superseded for this rollout, not for
    unrelated infrastructure work.

## Design Decisions

- 2026-10-31: user rejected isolated AWS role/canary fixtures as unwieldy.
  Agreed experiment: correct Serverless config, redeploy in Coupling's normal
  way, and check whether the notification flow works. Generated IAM inspection
  establishes the original configuration defect; deployed behavior is the next
  validation boundary. Do not claim an unrelated-function denial endpoint test.

- 2026-10-31: retain intentional player administration permissions (slice 4).
  Transfer DynamoDB IAM hardening to the schema migration with endpoint gates
  intact; keep Lambda invocation review here. No AWS provisioning is authorized.

- Application findings retain endpoint demonstration before fixes; Lambda IAM
  uses the user's revised config-first, normal-deployment experiment below.
- Slice 3 scope correction (2026-10-30): concurrency hardening was struck from
  the original suggestion because it was not the highlighted token-replay risk.
  Keep the remediation to single-use redemption and its atomic relationship writes;
  do not reintroduce general versioning or retry machinery as security requirements.
- Scope audit (2026-10-30): integration-summary API work is an optional product
  follow-up, owner/player restrictions require an explicit policy decision, and
  AWS permission hardening does not authorize a general migration redesign.
  Slice 3 self-link rejection is an accepted input-validation choice needed to
  avoid writing the same user twice, not an additional highlighted security risk.
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
- Slice 2a complete in `c8c9fe8ea`: local Discord simulation. Slice 2b complete
  in `fed9c056a`: HTTP-only endpoint red confirmed and authorization fix validated.
  Slice 2c complete: browser/user/party-bound single-use OAuth state uses existing
  DISCORD storage, with endpoint, repository, browser, lint, and full-check validation.
  Slice 3 complete: consumed-token markers and both relationship writes share
  one transaction in the existing USER table. General concurrency hardening was
  struck from scope. Next: resolve the product-policy prerequisite for slice 4;
  slice 5 still requires isolated AWS test authorization.
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
- **Optional product follow-up, outside this remediation:** an integration-summary
  API is not a requirement of the Discord authorization fix. The existing API
  exposes announcement message IDs but no
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

### Slice 2c: Discord OAuth state

- Starting commit: `fed9c056a` (completed slice 2b). One contributor owns the
  dependent test/implementation steps; no deployment or provisioning is required.
- Endpoint red:
  `fish -lc './gradlew :sdk:jvmTest --tests "*SdkDiscordTest.grantWithoutInitiation*" --no-daemon'`
  ran one HTTP test and failed with `expected Rejected but was Accepted`. The
  authorized caller supplied a valid simulated code without any OAuth initiation.
  Authentication and party/announcement setup passed before the trigger.
- Implementation: opaque random state is stored by hash in the existing DISCORD
  table; conditional deletion matches initiating user, target party, verifier hash,
  and unexpired deadline. Authorization precedes consumption and code exchange.
  A mismatched attempt cannot consume valid state; exchange/storage failure after
  consumption requires restarting. In-memory mode has a shared pending-flow backend;
  the atomicity proof uses real local DynamoDB through HTTP requests.
- Repository expiry/isolation checks passed against local DynamoDB. Initial endpoint
  green exposed test setup cleanup: first initialization of the alternate SDK deletes
  its visible parties. Initialize it before granting membership, then explicitly
  prove its party access so user-binding denials cannot be authorization failures.
- Client uses Web Crypto randomness/SHA-256 and stores verifier plus party in
  sessionStorage keyed by state. Callback completion clears its entry and shows
  success/restart feedback without rendering code, state, or verifier.
- Build changes: actionz uses the established Kotlin Node wrappers for cryptography;
  client tests use the existing test-action fixture library. No SDK database dependency.
- Test-clarity review: SDK tests use only HTTP commands and queries. They prove
  authorized setup and original announcement routing, then reject missing/tampered
  state, wrong verifier/user/party, nonmember initiation/grant, and unauthenticated
  initiation/grant. A shared-party fixture proves the second user is authorized
  before testing user binding. Concurrent requests have one winner; replay cannot
  change routing. Separate pending flows complete independently. Browser tests
  cover storing before navigation, missing-tab rejection, success/rejection cleanup,
  preserving other pending flows, and a known SHA-256 vector.
- Production-design review: server commands own state issuance/consumption; repository
  conditional writes enforce single-use across requests/processes. Current membership
  authorization remains in both resolvers. State/verifier fields are required across
  all layers; GraphQL descriptions specify challenge encoding and expiry. Existing
  integration queries remain partitioned by party, excluding pending OAuth records.
- Browser harness discovery: the first browser run started Chrome but failed before
  tests because the installed TestMints hook requires a legacy `.js` reporter module;
  this client emits `.mjs` and exports the singleton via `getInstance()`. A client-only
  Karma alias and test-resource adapter preserve the existing reporter/hooks. No
  browser checks or logging were disabled. The corrected four-test browser run passed.
- Final focused green/lint:
  `fish -lc './gradlew :sdk:jvmTest --tests "*SdkDiscordTest*" :client:jsBrowserTest :client:lintKotlin :server:lintKotlin :server:actionz:lintKotlin :sdk:lintKotlin :libraries:action:lintKotlin :libraries:repository:core:lintKotlin :libraries:repository:dynamo:lintKotlin --no-daemon'`
  passed outside the sandbox with a fresh daemon in 1m 7s: all 12 HTTP scenarios,
  five browser tests, and affected lint. The two local DynamoDB repository tests
  passed under `:libraries:repository:dynamo:jsNodeTest --tests "*DynamoDiscordOAuthRepositoryTest*"`;
  coverage includes exact expiry plus cleanup and failed-consumption/query isolation.
- Full gate: `fish -lc './gradlew check --no-daemon'` passed outside the sandbox
  with a fresh daemon in 7m; 596 actionable tasks (157 executed, two from cache,
  437 up-to-date). Complete SDK Node/JVM suites, repository suites, browser tests,
  56 browser E2E tests, and structured-log validation passed.
- Complete-slice review: no new table, table-key migration, TTL configuration,
  AWS profile, or membership-policy changes. Pending records use a separate
  namespace; integration installation/replacement and announcement reads remain
  covered through HTTP. Expired abandoned records are retained until encountered.
  Compatibility intentionally requires fresh initiation and upgraded SDK callers.
  `git diff --check` passed; no production deployment or live Discord validation
  was performed.
- Delivered with tests, implementation, browser-test compatibility resources,
  and this card in the slice commit. Obtain its ID with
  `git log -1 --format=%H -- agents.d/work/SECURITY_CRITICAL_HIGH_REMEDIATION.md`.
  Not pushed or released. Account-link and remaining security slices stay open.

### Slice 3: single-use account-link tokens

- Endpoint red confirmed with unchanged production code:
  `fish -lc './gradlew :sdk:jvmTest --tests "*SdkUserTest.connectionToken*" --tests "*SdkUserTest.concurrentConnection*" --no-daemon'`.
  All three scenarios failed for the intended reason: another account's replay
  returned true, replay after disconnect returned true, and the same token had
  two successful redemptions. Authentication, first redemption, and HTTP-only
  fixture cleanup passed. An initial Gradle configuration-cache setup failure
  was corrected before obtaining this evidence.
- User narrowed the design to the original replay defect: no general user-write
  versioning, automatic retries, or guarantees that unrelated overlapping edits
  all succeed. An earlier broader implementation and its validation are superseded.
- Final design: DynamoUserRepository conditionally writes one consumed-token marker
  in the existing USER table in the same transaction as both users' user/email
  records. A used marker rejects the entire transaction. Normal saves, issuance,
  and disconnect retain their existing implementations. No new table, table-key
  schema, AWS resource, or repository class is introduced. Markers are retained
  alongside existing history; no TTL or cleanup infrastructure is added.
- Existing pending tokens remain valid for one redemption. Existing relationships
  and signed-token expiry validation are unchanged. A later stale user save cannot
  revive a consumed token because its marker remains. Resolving unrelated stale
  edits and choosing the intended winner among token holders are outside this fix.
- Memory mode shares a consumed-secret set and mutex across repository instances.
  Compound connection writes delegate to the same primary repository as reads.
  SDK tests use only HTTP and reuse the authorized synthetic E2E identity, whose
  password is supplied using the established Gradle SSM provider. Browser E2E is
  ordered after SDK tests to avoid sharing that identity concurrently.
- Final focused green/lint:
  `fish -lc './gradlew :sdk:jvmTest --tests "*SdkUserTest*" :libraries:repository:dynamo:jsNodeTest --tests "*DynamoUser*Test*" :libraries:repository:memory:jsNodeTest --tests "*MemoryUserRepositoryTest*" :libraries:repository:compound:jsNodeTest --tests "*CompoundUserRepositoryTest*" :sdk:lintKotlin :libraries:repository:core:lintKotlin :libraries:repository:dynamo:lintKotlin :libraries:repository:memory:lintKotlin :libraries:repository:compound:lintKotlin :server:actionz:lintKotlin :server:lintKotlin --no-daemon'`
  passed in 1m 3s: all 13 HTTP user scenarios, four DynamoDB token scenarios,
  existing user repository tests (one existing performance test remains ignored),
  memory/compound tests, and affected lint.
- Test-clarity review: SDK setup proves three authenticated synthetic identities,
  successful first redemption, and isolated connection state. HTTP verification
  checks rejection plus all users' visible links. Direct DynamoDB cases verify
  both user/email writes, whole-transaction rejection for a used marker, rejection
  even after an old user snapshot is restored, and existing pending-token compatibility.
  Memory/compound cases exercise shared single-use authority rather than duplicate
  independent consumption. Existing signature/expiry tests remain in server:secret.
- Production-design review: existing signed-token validation and target lookup feed
  one repository operation. Its five writes are the conditional marker plus the
  two existing records for each user. Public GraphQL/SDK contracts, normal saves,
  issuance, disconnect, and membership policy are unchanged. AWS transaction input
  now correctly permits an omitted condition on unconditional record writes.
- Full gate: `fish -lc './gradlew check --no-daemon'` passed outside the sandbox
  with a fresh daemon in 7m 9s; 587 actionable tasks (125 executed, 462 up-to-date).
  Complete SDK Node/JVM suites passed, including all 13 user scenarios on each
  runtime, as did repository/server suites, client browser tests, 56 browser E2E
  tests, lint, and structured-log validation.
- Complete-slice review: the final production change is limited to atomic
  single-use redemption and equivalent repository adapters. No table, key schema,
  AWS profile, general write versioning, retry mechanism, client feature, or
  permission-policy change is included. Self-link rejection is explicit input
  validation from the accepted plan, not a separate security finding.
  `git diff --check` passed. Optional API work and the policy/migration boundaries
  in later slices were explicitly labeled during the scope audit.
- Validation setup notes: earlier local timeouts were traced to a busy DynamoDB
  SQLite worker; disposable containers were reset through Gradle. A full-check
  attempt was stopped for the requested simplification, and prototype records
  were discarded before validating the final design. No production deployment or
  external resource provisioning was performed.
- Delivered with tests, implementation, and this card in the slice commit; obtain
  its ID with `git log -1 --format=%H -- agents.d/work/SECURITY_CRITICAL_HIGH_REMEDIATION.md`.
  Not pushed or released. Slices 4/5 remain queued with their prerequisites.

### 2026-10-31: policy resolution and migration IAM handoff

- Base commit: `f92e98ab0`. Slice 4 is resolved with existing permissions
  intentional; no policy implementation or denial tests. DynamoDB hardening is
  transferred to `DYNAMODB_MIGRATION_IAM.md` with its AWS endpoint prerequisite
  intact. Proposal/strategy now describe namespaces, explicit transitional legacy
  access, operation/family boundaries, and per-environment cutover retirement.
- Lambda review attempted through
  `./gradlew :deploy:sandbox:serverlessPackage --no-daemon`. It failed during
  Serverless Framework 4.46.0 authentication with `Forbidden`, before generated
  CloudFormation was available. `aws sts get-caller-identity` succeeded using
  the configured read-only SSO role; this is not evidence of expired AWS login.
  No profiles or credentials were changed and no deployment was attempted.
- The effective wildcard invocation policy remains unverified. No production
  IAM edits or endpoint claims. Next: restore authorized Serverless packaging
  access, inspect the generated template, then define the smallest required
  invocation grant. Use the stack's generated function identity without creating
  a CloudFormation role/function dependency cycle.
- Real AWS fixtures remain unauthorized. Before requesting authorization, present
  exact resource inventory, account/region, permissions, cleanup, and costs.
- Documentation local links and `git diff --check` passed. No behavior tests were
  invented for this documentation slice. `./gradlew check --no-daemon` passed
  outside the sandbox with a fresh daemon in 29s: 596 actionable tasks, 54
  executed and 542 up-to-date. Existing successful test outputs were reused.
  Log: `/tmp/coupling-iam-handoff-check.log`.
- Complete-slice review: documentation only; no runtime, schema, SDK, client,
  credentials, or deployed resources changed. Transferred DynamoDB work remains
  open, and the Lambda exposure is unconfirmed. Documentation/design review
  verified namespace maintenance, legacy compatibility, role boundaries, cutover
  gates, and linked ownership. Test-clarity review is not applicable to this
  documentation slice. Commit: obtain with
  `git log -1 --format=%H -- agents.d/work/DYNAMODB_MIGRATION_IAM.md`.
  Not pushed or released.

### 2026-10-31: local Lambda IAM investigation

- Scope confirmed by user: local investigation only. No AWS fixtures, Lambda
  invocation, deployment, runtime IAM edits, push, or release are authorized.
- Retried `fish -lc './gradlew :deploy:sandbox:serverlessPackage --no-daemon'`
  from clean base `d1f118883`. It failed in 9s at Serverless `Authenticating`
  with `Forbidden` (204 actionable tasks: 17 executed, 187 up-to-date).
  No generated template was available at the configured package destination,
  `server/build/sandbox/lambda-dist`. There are no generated policy excerpts
  to report; the effective wildcard grant remains unconfirmed.
- `fish -lc 'aws sts get-caller-identity --query Arn --output text'` succeeded
  for account `174159267544` using the configured `AWSReservedSSO_ReadOnlyAccess`
  role. This distinguishes working shell AWS authentication from the Serverless
  authentication failure; it does not prove packaging has every AWS permission.
  No profiles, credentials, or authentication configuration were changed.
- Packaging ownership: the deploy convention copies the pinned Serverless
  configuration and invokes `serverless package --stage sandbox`, passing the
  shell's `SERVERLESS_ACCESS_KEY`. `.github/workflows/main.yml:26` injects
  `${{ secrets.SERVERLESS_ACCESS_KEY }}` into the Build job; the deploy convention
  forwards it to Serverless. A value-free presence check confirmed that this
  variable is unset in the local fish shell used for packaging. CI injection
  does not supply it to this local process. The GitHub secret's validity was not
  checked. Local packaging needs an authorized Serverless authentication method;
  do not assume the CI key is broken, AWS SSO is expired, or rotate credentials
  based on this failure. Other possible Serverless authentication state was not
  inspected, so the precise reason for `Forbidden` remains unestablished.
- Source evidence only: `server/serverless.yml` declares a legacy
  `provider.iamRoleStatements` Allow for `lambda:InvokeFunction` on `"*"`,
  alongside `provider.iam.role.managedPolicies` containing
  `AmazonDynamoDBFullAccess`. Generated output is still needed to determine
  how these declarations are interpreted together. DynamoDB remains owned by
  the migration IAM card.
- Notification trace: successful authenticated WebSocket connection handling in
  `server/src/jsMain/kotlin/Handler.kt` asynchronously invokes
  `coupling-server-${STAGE}-notifyConnect` with invocation type `Event` and the
  connection event payload. Invocation failures are caught and logged.
- Future correction candidate, pending generated-template inspection: put the
  required invocation statement in the supported role policy location and scope
  it to the generated notification function identity. Avoid a role-policy
  `GetAtt` of a function that itself references that role. Prefer an ARN built
  with CloudFormation partition/region/account pseudo parameters and the same
  resolved function name used by Serverless, after verifying that name in the
  generated template. Do not duplicate an assumed name or introduce a function
  environment reference cycle. No policy correction was made in this slice.
- Next gate: restore authorized Serverless packaging access, rerun the existing
  Gradle task, then inspect all generated role policies, managed-policy references,
  function names, and dependencies. Determine whether the wildcard is present
  before selecting remediation versus a missing-grant functional correction.
  AWS endpoint validation remains a separate authorization gate requiring exact
  account/region, fixtures, permissions, cost, and cleanup details. Slice 5b stays
  open; source inspection is neither exploit evidence nor remediation evidence.
- Validation: `fish -lc './gradlew check --no-daemon'` passed outside the sandbox
  with a fresh daemon in 27s (587 actionable tasks: 54 executed, 533 up-to-date).
  Existing test outputs were reused. Log: `/tmp/coupling-local-lambda-check.log`.
  `git diff --check` passed. Documentation review checked source declarations,
  notification data flow, CI/local credential injection, and explicit evidence
  limits. No behavior tests are appropriate for this documentation slice.
- Complete-slice review: only this card changed; no runtime, public API, schema,
  SDK, build logic, credentials, or AWS resources changed. Local investigation
  is complete to the authentication blocker; IAM remediation is not verified.
  Delivered in the documentation commit discoverable with
  `git log -1 --format=%H -- agents.d/work/SECURITY_CRITICAL_HIGH_REMEDIATION.md`.
  Not pushed or released.

### 2026-10-31: authenticated local packaging and generated IAM evidence

- Supersedes the packaging blocker above. User supplied temporary Serverless
  authentication through fish; its value was not printed or copied into this
  card. User owns revoking/removing that temporary credential afterward.
- `fish -lc './gradlew :deploy:sandbox:serverlessPackage --no-daemon'` passed
  authentication but initially failed with `EPERM` creating the local cache
  directory under `~/.serverless/artifacts`. Retried the same packaging-only
  command through approved outside-sandbox execution; it passed in 10s
  (204 actionable tasks: 16 executed, 188 up-to-date). Stage: `sandbox`;
  region: `us-east-1`. No deployment or AWS fixture creation was performed.
- Inspected `server/build/sandbox/lambda-dist/cloudformation-template-update-stack.json`.
  `IamRoleLambdaExecution.Properties.Policies` includes exactly this invocation
  statement: `{"Effect":"Allow","Action":["lambda:InvokeFunction"],"Resource":"*"}`.
  The role also retains `AmazonDynamoDBFullAccess` in `ManagedPolicyArns`;
  DynamoDB hardening remains with the migration card. The legacy declaration
  is therefore effective in this generated template despite the coexisting
  newer `provider.iam.role` configuration.
  Template SHA-256:
  `6f7ef382fe4426ab51344df1c5aca79cedf6780128da437f78e8755125220a3e`.
- All five generated Lambda functions reference `IamRoleLambdaExecution.Arn`.
  `NotifyConnectLambdaFunction.Properties.FunctionName` is
  `coupling-server-sandbox-notifyConnect`, matching the application invocation
  target traced above. Other invocation statements in the template belong to
  API Gateway resource permissions, not runtime role permissions.
- Proposed future correction: remove the wildcard invocation statement from
  the shared role and add a separate `AWS::IAM::Policy` resource with only
  `lambda:InvokeFunction`, resource `Fn::GetAtt: [NotifyConnectLambdaFunction, Arn]`,
  and roles containing `Ref: IamRoleLambdaExecution`. Dependency flow is policy
  to function to role, plus policy to role; the role must not depend on that
  policy. This uses the generated function identity and avoids an inline-role
  reference cycle or a duplicated function-name convention. Validate the final
  generated dependency graph before accepting any later correction. No runtime
  or production policy edits were made here.
- Evidence limit: generated configuration confirms a broad grant would be
  packaged, not which policy is currently deployed or whether an invocation
  succeeds under AWS permission boundaries/SCPs. Slice 5b remains open. Next gate
  is a concrete isolated AWS endpoint-validation proposal (account/region,
  harmless fixtures, exact permissions, cost, cleanup) and explicit authorization
  before provisioning. Prove the original broad invocation, then authorized
  notification invocation and unrelated-function denial before claiming a fix.
- Validation: `fish -lc './gradlew check --no-daemon'` passed outside the sandbox
  with a fresh daemon in 19s (587 actionable tasks: 54 executed, 533 up-to-date).
  Existing test outputs were reused. `git diff --check` passed. Check log:
  `/tmp/coupling-lambda-generated-check.log`; packaging log:
  `/tmp/coupling-lambda-package.log`.
- Complete-slice review checked the generated role statements, all function role
  references, notification name, and proposed dependency direction. Only this
  work card changed; no behavior tests were invented for this documentation
  slice. No production IAM, runtime, credential configuration, or AWS resources
  changed. Delivered in the documentation commit discoverable with
  `git log -1 --format=%H -- agents.d/work/SECURITY_CRITICAL_HIGH_REMEDIATION.md`;
  not pushed or released.
  Generated output remains local build material; do not commit resolved service
  configuration or credentials from `serverless-state.json`.

### 2026-10-31: scoped Lambda config and normal-rollout experiment

- User replaced the isolated endpoint-red prerequisite with changing config,
  normal redeployment, and checking whether the intended behavior works. This
  decision supersedes earlier local-only and isolated-fixture sequencing for
  slice 5b. No unrelated-function invocation or fixture provisioning is planned.
- Baseline configuration evidence is the previously generated wildcard role
  statement. Smallest implementation: remove that legacy inline statement and
  add `NotifyConnectInvocationPolicy`, an `AWS::IAM::Policy` attached to the
  existing shared role, allowing only `lambda:InvokeFunction` against
  `Fn::GetAtt: [NotifyConnectLambdaFunction, Arn]`. All five functions still
  share the role; separating roles and changing DynamoDB policy are outside
  this slice. No application contracts or notification data flow changed.
- Initial packaging was considered up-to-date despite the changed copied
  configuration, so it was not accepted as validation. Forced only the packaging
  task with `fish -lc './gradlew :deploy:sandbox:serverlessPackage --rerun --no-daemon'`
  outside the sandbox. It passed in 25s (213 actionable tasks: 16 executed,
  197 up-to-date). Generated output confirms exactly the scoped statement and
  role attachment, no inline invocation wildcard, unchanged DynamoDB managed
  policy, and no cycle among resource Ref/GetAtt/DependsOn edges.
- Local structural inspection is config verification, not an AWS endpoint test.
  No source-string test was invented as a substitute. Full gate:
  `fish -lc './gradlew check --no-daemon'` passed outside the sandbox with a fresh
  daemon in 25s (587 actionable tasks: 55 executed, 532 up-to-date); existing
  successful test outputs were reused. `git diff --check` passed.
  Logs: `/tmp/coupling-lambda-scoped-package.log` and
  `/tmp/coupling-lambda-scoped-check.log`.
- Normal rollout path: `.github/workflows/main.yml` runs `release check` on
  master pushes; release finalizers invoke configured deploy tasks. At preparation
  time local master was nine commits ahead of origin/master, including earlier
  security fixes and their SDK compatibility changes. Publishing this branch
  would include those changes too; present that concrete release scope before
  pushing rather than treating this as an isolated IAM-only rollout.
- Post-deploy acceptance: inspect the deployed role's inline/attached policies
  for removal of wildcard invocation and presence of the generated target ARN.
  With a designated test party and authorized identity, connect a WebSocket and
  verify the new connection receives its initial online-player message from
  `notifyConnect`; open a second connection and verify presence updates. HTTP
  success or an open socket alone is insufficient: invocation failures are
  caught/logged by the connection handler. If behavior fails, inspect connection
  and notification logs for invocation authorization versus notification errors
  and correct or revert through the normal release path. Observe the latest
  deployment's role policy propagation before concluding a persistent failure.
- Deployment and AWS behavior remain unverified. Unrelated-function denial
  remains inferred from the scoped grant rather than endpoint-tested; other
  policies/resource policies may affect effective authorization. Keep slice 5b
  open until deployed policy and notification evidence are recorded. Do not
  mark this card complete or claim full AWS security verification from local checks.
- Complete-slice review: only Serverless IAM configuration and this card changed.
  Verified exact resource identity, role attachment, dependency direction, and
  retention of existing DynamoDB permissions. Local validation passes; deployed
  behavior is pending. Commit is discoverable with
  `git log -1 --format=%H -- server/serverless.yml`. Not pushed or deployed.

### Delivery gates

- Record exact red/green commands, test-clarity and production-design reviews,
  complete-slice review, full-check outcome, and commit ID per slice.
- Run `./gradlew check --no-daemon` outside the sandbox via the approved execution
  mechanism from its first attempt, per GRADLE_PLAYBOOK.md. Commit passing tests
  and implementation together before starting the next slice. Record blockers
  instead of calling an unverified slice release-safe.
- At completion review all five findings against endpoint evidence and recorded
  intentional-policy decisions; transferred findings stay open until linked
  evidence demonstrates remediation. Record any
  deployment-only gaps and credential-remediation follow-up, and move this card
  to `agents.d/work_completed/`. Pushing/releasing requires user authorization.
