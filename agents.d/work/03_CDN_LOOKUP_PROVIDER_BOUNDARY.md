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

- [ ] Record current esm.sh behavior with focused characterization tests before moving it
- [ ] Slice 1: base URL construction
  - [ ] Write or isolate one characterization test for package, version, and subpath
  - [ ] Move that construction behind the smallest provider contract
  - [ ] Delete the displaced construction from the old location
  - [ ] Run scoped tests
- [ ] Slice 2: dependency query parameters
  - [ ] Write one characterization test for pinned dependencies
  - [ ] Move existing behavior into the esm.sh provider
  - [ ] Delete the displaced construction from the old location
  - [ ] Run scoped tests
- [ ] Slice 3: externals and encoding
  - [ ] Write one characterization test for external encoding
  - [ ] Move existing behavior into the esm.sh provider
  - [ ] Delete the displaced construction from the old location
  - [ ] Run scoped tests
- [ ] Slice 4: provider selection seam
  - [ ] Write one test proving the canonical lookup invokes the selected provider
  - [ ] Add explicit selection with esm.sh as the only registered/default provider
  - [ ] Run scoped tests
- [ ] Verify no parallel legacy URL builder remains
- [ ] Run standalone and client compatibility checks
- [ ] Final refactor pass and move card to `agents.d/work_completed/`

## Explicitly Deferred

- A second CDN implementation
- Plugin discovery or dynamic provider loading
- Generalizing provider inputs beyond behavior demonstrated by esm.sh

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- Standalone packed-artifact smoke test
- `./gradlew :client:lookupCdnUrls`

Results: [fill as work proceeds]
