# Architecture

## Project and Ownership

Multi-module Gradle: Kotlin/JVM and Kotlin/JS, web client and backend services.
See `settings.gradle.kts` for the module include map.

- `client/`: frontend; Vite config at `client/vite.config.mjs`. No server policy in
  the client; treat the server as external and consume its contracts.
- `server/`: backend and GraphQL schema/resolvers.
- `libraries/`: shared code and infrastructure. No app policy in shared code; add
  abstractions only when multi-module demand and stable semantics exist.
- `sdk/`: GraphQL documents and dispatchers. Explicit contract mappings, with no
  silent fallbacks that mask contract drift.
- `cli/`, `e2e/`, `scripts/`, `deploy/`: tooling and operations.

Tests constrain behavior at the affected boundary; deleting a test removes a
constraint without necessarily producing a build failure. See [TESTING.md](TESTING.md).

## Build and Deployment

All environments run the same application with different normal configuration.
Production serves normal usage; sandbox provides isolated data and integrations
for trying released behavior; prerelease enables incubating features through the
shared `ENABLE_PRERELEASE_FEATURES` flag. These are operating configurations,
not successive deployment gates.

Avoid branching application behavior on environment identity. Prefer universal
configuration settings that apply consistently in every environment. Minimize
feature flags and use the existing prerelease flag for incubating features where
appropriate rather than adding a flag for each feature. Environment-specific
origins, credentials and data namespaces remain ordinary configuration values.

Work cards describe incubating behavior through `ENABLE_PRERELEASE_FEATURES`
being enabled or disabled, and promotion as making that behavior unconditional.
Do not use named environments as feature states, acceptance gates or rollout
steps, or invent environment-specific settings, policies or orchestration for a
feature. Mention an environment name only when needed for an actual resource
address, existing configuration constraint or historical evidence. A flag's
removal does not itself justify new environment-specific behavior.

New user-visible UI changes start behind the existing
`ENABLE_PRERELEASE_FEATURES` gate so they can be pushed without changing the
default user experience while awaiting owner review. Incorporate the owner's UI
feedback, then promote the reviewed UI by removing the gate and making it
unconditional. Do not add a separate flag for each UI change.

This consistency is critical to trustworthy tests and to local development being
as good a proxy for the released application as possible. Keep concessions to a
minimum: reuse the released application's behavior and execution paths wherever
practical, and make necessary differences explicit so their validation limits
remain visible.

Use the commands and safe delivery protocol in [AGENTS.md](../../AGENTS.md).
Generated build artifacts are not version-controlled; agent entry files are
hand-maintained committed documentation.

Serverless packaging belongs to each `:deploy:<stage>` module. Its service root is
`build/deploy`; the final artifact remains `server/build/<stage>/lambda-dist`.
The runtime version derives from `libraries/js-dependencies/package.json` when
stage service config is copied. Package tasks share `:server:serverlessWarmup`
before running in parallel.

## GraphQL Locations

Design APIs to be broadly easy to use, discoverable, and informative. Authorized
clients should be able to inspect relevant state and understand operation results
and failures through the API. Expose useful non-secret configuration and status
under appropriate permissions; protect credentials and sensitive information.
Missing inspection capabilities are API design findings, not reasons for SDK
tests to bypass the API.

- Schema: `server/src/jsMain/resources/schema.graphqls`
- Resolvers: `server/src/jsMain/kotlin/`
- SDK documents: `sdk/src/commonMain/graphql/`
- SDK dispatchers: `sdk/src/commonMain/kotlin/`
- Action tests: `server/actionz/src/jsTest/kotlin/`

Use [PLAYBOOK_GRAPHQL.md](PLAYBOOK_GRAPHQL.md) for synchronized changes,
deprecation routing, reference discovery, and validation.

## Integration

Design seams around product concepts and ownership, not local neatness. Identify
contract/invariant/auth assumptions at affected boundaries and revise them when
evidence conflicts. Keep parallel ownership disjoint and integration touchpoints
explicit. Surface conceptual conflicts promptly. For deployment-affecting changes,
consider the integration path, build/deploy ergonomics, and rollback.

## Persistence and concurrent writes

Keep ordinary inserts and full-state saves simple. Preserve each accepted
revision where the domain keeps history, and use the last committed save for
current state. Import historical data by stable source identity and use the
source’s recorded order to reconstruct current state within the import’s
ownership. Arrival order can differ from source order.

Tie each write guard, receipt, and recovery mechanism to a concrete invariant
or failure scenario. Examples include unique claims, updates derived from a
prior read, atomic progress and recovery from partial cross-store writes.
Reuse existing identities and transaction boundaries before introducing new
state or caller requirements.

## Observability

Design application observability as a reusable system capability. Prefer ordinary
application logs as the primary evidence for performance, reliability and
correctness investigations, so people with log access can answer routine questions
without a profiler, custom parser or dedicated measurement runner. Improve signals
at the owning application boundary rather than building parallel instrumentation
only for tests or experiments.

Use structured, directly queryable events with stable operation names, meaningful
lifecycle boundaries, correlation, explicit outcomes and numeric elapsed durations
where timing matters, including failures and cancellations. Preserve types and
context through formatting and collection. Reuse existing logging and request
paths; keep volume bounded and exclude secrets and sensitive application data.

Verify what reaches the intended logging destination. Browser console output or
normalized test logs alone do not establish runtime collection coverage. Make
sampling, missing events and unsupported boundaries explicit; missing duration is
not zero, and overlapping command durations are not total request elapsed time.

Use specialized tools for concrete questions logs cannot establish, such as paint
behavior, controlled cache comparisons or actual network bytes. Retain findings
and limitations, and feed reusable signals back into application observability
where useful. New collection infrastructure or maintained tooling needs a concrete
recurring use and clear ownership; follow
[tooling purpose and lifetime](GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime).

## Existing Utilities

`agents.d/utilities/` contains reference discovery, safe dead-code deletion, and
Chrome troubleshooting helpers. Read their instructions before use. New repository
automation belongs in Gradle tasks; retaining an existing helper is not a reason
to introduce additional shell automation.
