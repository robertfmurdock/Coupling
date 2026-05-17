# Playbook: GraphQL Changes

Use this playbook for field add/rename/deprecation/removal and mutation routing.

## 1) Scope and Text-Reference Scan
1. Identify target operation/field names.
2. Run:
   - `agents.d/utilities/graphql-ref-scan.sh <pattern>`
3. Treat scan output as discovery only:
   - It is string matching, not schema/runtime verification.
   - It can miss indirect/dynamic references and can include stale matches.
4. Record impacted files in:
   - `server/src/jsMain/resources/schema.graphqls`
   - `server/src/jsMain/kotlin/...`
   - `sdk/src/commonMain/graphql/...`
   - `sdk/src/commonMain/kotlin/...`
   - `client/...`

## 2) Schema and Resolver Changes
1. Update schema in `schema.graphqls`.
2. Update resolver path in `server/src/jsMain/kotlin/...`.
3. For deprecated fields, route to canonical command/mutation logic.

## 3) SDK Synchronization
1. Update/remove matching SDK `.graphql` operations.
2. Update dispatchers and model mapping logic.
3. Ensure dispatcher path uses canonical mutation/command behavior.

## 4) Test Updates
1. Update server action tests in `server/actionz/src/jsTest/kotlin/...`.
2. Preserve behavioral assertions from legacy path.
3. Add authorization stubs required by new command path.

## 5) Verification
1. Re-run text-reference scan:
   - `agents.d/utilities/graphql-ref-scan.sh <pattern>`
2. Run targeted tests first (`:module:task`) for each touched layer:
   - server mutation/resolver path: `:server:actionz:jsTest`
   - SDK dispatcher/document updates: `:sdk:commonTest`
3. Run broader validation as needed:
   - `./gradlew test`
   - `./gradlew check`

## 6) Done Criteria
- Deprecated paths delegate (no duplicated legacy logic).
- Schema + server + SDK + tests are aligned in one change set.
- Validation results are reported with any known gaps.
