# AGENTS

This repo is a multi-module Gradle project (Kotlin/JVM and Kotlin/JS) that
includes a web client and a server.

## Quick commands
- `./gradlew test`
- `./gradlew build`
- `./gradlew check`
- `./gradlew :module:task` (run module-specific tasks as needed)

## Layout
- `client/` frontend (Kotlin/JS, Vite config in `client/vite.config.mjs`)
- `server/` backend services
- `libraries/` shared modules
- `cli/`, `sdk/`, `e2e/`, `scripts/`, `deploy/` supporting tooling

## Notes for agents
- Use the Gradle wrapper (`./gradlew`) for builds and tests.
- Keep changes small and consistent with existing patterns.
- For GraphQL API changes (renames, adds, deprecations/removals), use `scripts/graphql-ref-check.sh` to find schema/SDK/server/client references.

## Architecture notes
- GraphQL schema lives in `server/src/jsMain/resources/schema.graphqls`. Resolvers are in `server/src/jsMain/kotlin/...` and should route deprecated fields through the canonical command/mutation instead of duplicating logic.
- SDK GraphQL documents are in `sdk/src/commonMain/graphql/`. Dispatchers in `sdk/src/commonMain/kotlin/...` should prefer the canonical mutation and map domain models directly.
- Server action tests live under `server/actionz/src/jsTest/kotlin/...`. When porting tests, keep the same behavioral assertions and add required authorization stubs for the new command path.

## Common pitfalls
- Deprecations must delegate: avoid re-implementing legacy mutation logic; route through the canonical command/mutation to keep behavior aligned.
- GraphQL/SDK drift: if you remove or deprecate a schema field, update/remove the matching SDK `.graphql` document and its dispatcher in the same change set.
