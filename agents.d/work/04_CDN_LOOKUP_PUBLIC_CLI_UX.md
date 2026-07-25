# 4. CDN Lookup Public CLI UX

## Goal
Incrementally turn the existing command entry point into a documented public CLI while keeping one canonical command and lookup path.

## Constraints

- Requires completion of `03_CDN_LOOKUP_PROVIDER_BOUNDARY.md`.
- Follow Coupling CLI command and testing patterns, including Clikt for command parsing.
- Refactor the existing `main`; do not create a new public CLI beside the compatibility CLI.
- Every compatibility option must parse into the same canonical command options and execution path.
- Add only options selected for the first release; each option is its own behavior slice.
- **TDD required: one observable command behavior per red-green-refactor slice.**

## Checklist

- [ ] Decide first-release package name, binary name, config location, output formats, and supported Node versions
- [ ] Slice 1: help
  - [ ] Write one black-box help test
  - [ ] Refactor existing argument handling into a Clikt command following the Coupling CLI command pattern
  - [ ] Run scoped tests
- [ ] Slice 2: version
  - [ ] Write one black-box version test
  - [ ] Wire package version through the canonical command
  - [ ] Run scoped tests
- [ ] Slice 3: readable config file
  - [ ] Write one test loading the selected config-file location
  - [ ] Decode it into the existing configuration model
  - [ ] Make base64 compatibility input delegate to the same decoded model
  - [ ] Run scoped tests
- [ ] Slice 4: provider option
  - [ ] Write one test for explicit esm.sh selection
  - [ ] Route it through the existing provider-selection seam
  - [ ] Run scoped tests
- [ ] Slice 5: output behavior
  - [ ] Write one test for the selected first-release output option
  - [ ] Route all output through the existing formatter boundary
  - [ ] Run scoped tests
- [ ] Slice 6: errors
  - [ ] Write one test for stderr and non-zero exit behavior
  - [ ] Add the smallest error translation needed
  - [ ] Run scoped tests
- [ ] Add any additional approved CLI option as its own one-test slice
- [ ] Run packed-artifact and client compatibility checks
- [ ] Final refactor pass and move card to `agents.d/work_completed/`

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- Standalone packed-artifact command tests
- `./gradlew :client:lookupCdnUrls`

Results: [fill as work proceeds]
