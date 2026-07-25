# 3. CDN Lookup Provider Boundary

## Goal
Refactor the existing esm.sh URL construction behind a provider contract without changing observable URLs or adding another provider.

## Constraints

- Requires completion of `02_CDN_LOOKUP_RELIABLE_PACKAGE_RESOLUTION.md`.
- Extract from the existing `GenerateCdnRef.kt` and query-derivation path; do not copy their behavior into a new implementation.
- Exactly one provider is required in this card: esm.sh.
- Package discovery, version resolution, and peer graph traversal must remain outside the provider.
- The default and compatibility entry points must call the same selected provider path.
- **TDD required: one provider behavior per red-green-refactor slice.**

## Checklist

- [x] Record current esm.sh behavior with focused characterization tests before moving it
- [x] Slice 1: base URL construction
  - [x] Write or isolate one characterization test for package, version, and subpath
  - [x] Move that construction behind the smallest provider contract
  - [x] Delete the displaced construction from the old location
  - [x] Run scoped tests
- [x] Slice 2: dependency query parameters
  - [x] Write one characterization test for pinned dependencies
  - [x] Move existing behavior into the esm.sh provider
  - [x] Delete the displaced construction from the old location
  - [x] Run scoped tests
- [x] Slice 3: externals and encoding
  - [x] Write one characterization test for external encoding
  - [x] Move existing behavior into the esm.sh provider
  - [x] Delete the displaced construction from the old location
  - [x] Run scoped tests
- [x] Slice 4: provider selection seam
  - [x] Write one test proving the canonical lookup invokes the selected provider
  - [x] Add explicit selection with esm.sh as the only registered/default provider
  - [x] Run scoped tests
- [x] Verify no parallel legacy URL builder remains
- [x] Run standalone and client compatibility checks
- [x] Final refactor pass and move card to `agents.d/work_completed/`

## Explicitly Deferred

- A second CDN implementation
- Plugin discovery or dynamic provider loading
- Generalizing provider inputs beyond behavior demonstrated by esm.sh

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- Standalone packed-artifact smoke test
- `./gradlew :client:lookupCdnUrls`

Results:
- `./gradlew :scripts:cdn-lookup:jsTest` passed after each slice.
- `rg -n "https://esm\\.sh|moduleAndSubmodule|deps=|external=|encodeURIComponent|queryParametersFor" scripts/cdn-lookup/src/jsMain scripts/cdn-lookup/src/jsTest -S` showed production URL/query construction only in `CdnProvider.kt`; remaining matches are characterization tests.
- `./gradlew :scripts:cdn-lookup:packedExecutableSmokeTest` passed.
- `./gradlew :client:lookupCdnUrls` passed.
- `./gradlew :scripts:cdn-lookup:check` initially failed on provider constant naming; after renaming to `DEFAULT_CDN_PROVIDER_NAME`, `./gradlew :scripts:cdn-lookup:check` passed.
