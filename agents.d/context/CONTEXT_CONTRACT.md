# Context Contract

Every agent execution context must provide all items below before making code
changes.

## 1) Repository Shape
- Project type: multi-module Gradle (Kotlin/JVM + Kotlin/JS).
- Module families: `client/`, `server/`, `libraries/`, `sdk/`, `cli/`, `e2e/`.
- Module include map from `settings.gradle.kts`.

## 2) Commands and Validation
- Use Gradle wrapper only: `./gradlew`.
- Core validations: `./gradlew test`, `./gradlew build`, `./gradlew check`.
- Module-specific task convention: `./gradlew :module:task`.

## 3) Architecture Rules
- GraphQL schema file location.
- Resolver location and deprecation delegation rule.
- SDK GraphQL document and dispatcher locations.
- Test location and expected behavior parity for migrated command paths.

## 4) Change Safety Rules
- Keep changes scoped to impacted modules.
- For GraphQL API changes, run `agents.d/utilities/graphql-ref-scan.sh` as a
  text-reference discovery helper.
- Keep schema, SDK `.graphql`, SDK dispatcher, and server resolver behavior in
  sync in the same change set.

## 5) Agent Output Requirements
- Explicitly state assumptions.
- List changed files and why.
- Report validations run and results.
- Call out residual risks or skipped checks.
