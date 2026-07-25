# 2. CDN Lookup Reliable Package Resolution

## Goal
Refactor the existing package metadata code so lookup consistently reports installed package versions from an explicit working directory.

## Constraints

- Requires completion of `01_CDN_LOOKUP_STANDALONE_WALKING_SKELETON.md`.
- Change `PackageMetadata.kt` in place and route all callers through it; do not add a second resolver for the standalone CLI.
- Preserve both standalone and Coupling client behavior after every slice.
- Add support for aliases, workspaces, or file dependencies only one failing fixture at a time; unsupported cases may produce a clear error.
- **TDD required: one focused fixture scenario per red-green-refactor slice.**

## Checklist

- [x] Identify existing test utilities and create the smallest reusable fixture-project setup
- [x] Slice 1: explicit working directory
  - [x] Write one test that resolves from a supplied fixture directory
  - [x] Pass working directory through the existing call path instead of capturing `process.cwd()` globally
  - [x] Run scoped tests
- [x] Slice 2: installed version beats declared range
  - [x] Write one fixture test where the declared range differs from the installed version
  - [x] Refactor existing resolution to prefer the installed package's `package.json`
  - [x] Run scoped tests
- [x] Slice 3: subpath ownership
  - [x] Write one test proving a subpath import resolves through its owning package
  - [x] Implement only the required normalization
  - [x] Run scoped tests
- [x] Slice 4: missing package error
  - [x] Write one black-box failure test for an uninstalled package
  - [x] Add an actionable error without changing successful output
  - [x] Run scoped tests
- [x] Add further ecosystem cases only as separate one-test slices when required for the first release
- [x] Run packed-artifact and client compatibility checks
- [x] Final refactor pass and move card to `agents.d/work_completed/`

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- Standalone packed-artifact smoke test
- `./gradlew :client:lookupCdnUrls`

Results:
- `./gradlew :scripts:cdn-lookup:jsTest` passed.
- `./gradlew :scripts:cdn-lookup:packedExecutableSmokeTest` passed.
- `./gradlew :client:lookupCdnUrls` passed.
- `./gradlew :scripts:cdn-lookup:check` initially failed on import ordering in `PackageMetadataTest.kt`; after `./gradlew :scripts:cdn-lookup:formatKotlinJsTest`, `./gradlew :scripts:cdn-lookup:check` passed.
