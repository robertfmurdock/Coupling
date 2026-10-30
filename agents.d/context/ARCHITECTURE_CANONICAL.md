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

Use the commands and safe delivery protocol in [AGENTS.md](../../AGENTS.md).
Generated build artifacts are not version-controlled; agent entry files are
hand-maintained committed documentation.

Serverless packaging belongs to each `:deploy:<stage>` module. Its service root is
`build/deploy`; the final artifact remains `server/build/<stage>/lambda-dist`.
The runtime version derives from `libraries/js-dependencies/package.json` when
stage service config is copied. Package tasks share `:server:serverlessWarmup`
before running in parallel.

## GraphQL Locations

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

## Existing Utilities

`agents.d/utilities/` contains reference discovery, safe dead-code deletion, and
Chrome troubleshooting helpers. Read their instructions before use. New repository
automation belongs in Gradle tasks; retaining an existing helper is not a reason
to introduce additional shell automation.
