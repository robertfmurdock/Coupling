# SaveParty Consolidation + Test Perf Work Plan

## Ultimate Goal
Unify all save behavior (party, players, pins) through **SaveParty** so tests and app use the same API path, keep implicit upsert, require `party` only for new party creation, and update tests to match without changing expectations. Then run tests and measure any performance delta (including seed command impact). Keep CDN/config-cache changes isolated on their own branch.

## Current Status (As Of 2026-03-03)

### Completed
1. **Branch split done**
   - Branch `cdn-config-cache` created with commit `a4cd77482` containing only:
     - `client/build.gradle.kts`: disable config cache for Vite tasks; validate non-empty `cdn.json` before Vite.
   - `master` contains only SaveParty refactor work.

2. **Schema + core SaveParty refactor done**
   - `SavePartyInput` now supports sections: `party`, `players`, `pins`.
   - `SavePartyCommand` signature expanded: `(partyId, party?, players, pins)`.
   - Seed mutation removed (schema + server + sdk).
   - Server SaveParty now saves optional `party` and updates players/pins via repositories.
   - SaveParty requires `party` only for new party creation; implicit upsert only.

3. **Mapping/SDK/resolver updates done**
   - New `SavePartyInputMapper` and input DTOs.
   - Resolver uses new mapper.
   - SDK SaveParty dispatcher now builds new input shape.

4. **Client runtime uses SaveParty for player/pin saves**
   - PlayerConfig, UpdatingPlayerList, ContributorMenu, Contribution popups, PinConfig updated to SaveParty.

5. **Many tests updated to SaveParty**
   - e2e tests updated already (per prior summary).
   - client component tests updated to SaveParty.
   - sdk commonTest updated to SaveParty (players/pins).

### Still Pending / Verify
1. **Run tests**
   - ✅ `./gradlew test --no-configuration-cache` succeeded on 2026-03-05 after disabling `:testDistributionWebSocketCheck` when no server property is set.

2. **Remove/adjust legacy SavePlayer/SavePin if desired**
   - Current code keeps SavePlayer/SavePin actions/resolvers/dispatchers for compatibility. Decide whether to deprecate or keep. Tests now use SaveParty.

3. **Regenerate GraphQL code if needed**
   - If build fails on generated types, run the appropriate Apollo/Gradle tasks.

4. **Measure performance delta**
   - After tests pass, re-run e2e/perf and compare with prior baseline:
     - Previous seed mutation delta: 162.779s vs 168.486s (~5.7s faster without seed) from earlier run. Re-measure after new SaveParty path.
   - ✅ `./gradlew :e2e:e2eRun --no-configuration-cache --rerun-tasks` completed on 2026-03-05 (total build time 3m 48s).

## Work Log (Edits Already Made)

### Key Files Changed
- Schema: `server/src/jsMain/resources/schema.graphqls`
- SaveParty model: `libraries/action/src/commonMain/kotlin/.../SavePartyCommand.kt`
- Server SaveParty dispatcher: `server/actionz/src/jsMain/kotlin/.../ServerSavePartyCommandDispatcher.kt`
- Resolver: `server/src/jsMain/kotlin/.../SavePartyResolver.kt`
- SDK: `sdk/src/commonMain/kotlin/.../SdkSavePartyCommandDispatcher.kt`
- JSON mappers: `libraries/json/.../SavePartyInputMapper.kt`, plus player/pin/party mappers.
- Client components: PlayerConfig, UpdatingPlayerList, ContributorMenu, Contribution popups, PinConfig.
- Tests: client component tests; sdk commonTests; e2e tests.

### Tests Updated To SaveParty
- Client: `UpdatingPlayerListTest`, `PlayerConfigTest`, `PinConfigEditorTest`, `ContributorMenuTest`.
- SDK: `SdkPlayerTest`, `SdkPinTest`, `SdkPartyTest`, `SdkUserTest`, `SpinTest`, `RequestCombineEndpointTest`, `SdkPairsRecentTimesTest`, `SavePartyState`.

### Known Build Failure
- Running `./gradlew test --no-configuration-cache` fails at `:testDistributionWebSocketCheck` with:
  - "Cannot query the value of task ':testDistributionWebSocketCheck' property 'server' because it has no value available."
  - ✅ Fixed locally by skipping the task when no test distribution server property is set.

## Next Steps (Recommended Order)
1. Fix/disable Develocity test distribution WebSocket check for local runs.
2. Re-run `./gradlew test --no-configuration-cache`.
3. Fix any remaining test failures.
4. Run e2e/perf measurement and compare results.

## Notes
- User preference: keep **implicit upsert**; require `party` only for new party creation.
- User preference: keep API usage as close to app as possible; tests should be updated to new SaveParty path rather than changing expected behavior.
- Avoid config cache for Vite tasks (done in separate branch `cdn-config-cache`).
