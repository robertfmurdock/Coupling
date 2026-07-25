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

- [ ] Identify existing test utilities and create the smallest reusable fixture-project setup
- [ ] Slice 1: explicit working directory
  - [ ] Write one test that resolves from a supplied fixture directory
  - [ ] Pass working directory through the existing call path instead of capturing `process.cwd()` globally
  - [ ] Run scoped tests
- [ ] Slice 2: installed version beats declared range
  - [ ] Write one fixture test where the declared range differs from the installed version
  - [ ] Refactor existing resolution to prefer the installed package's `package.json`
  - [ ] Run scoped tests
- [ ] Slice 3: subpath ownership
  - [ ] Write one test proving a subpath import resolves through its owning package
  - [ ] Implement only the required normalization
  - [ ] Run scoped tests
- [ ] Slice 4: missing package error
  - [ ] Write one black-box failure test for an uninstalled package
  - [ ] Add an actionable error without changing successful output
  - [ ] Run scoped tests
- [ ] Add further ecosystem cases only as separate one-test slices when required for the first release
- [ ] Run packed-artifact and client compatibility checks
- [ ] Final refactor pass and move card to `agents.d/work_completed/`

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- Standalone packed-artifact smoke test
- `./gradlew :client:lookupCdnUrls`

Results: [fill as work proceeds]
