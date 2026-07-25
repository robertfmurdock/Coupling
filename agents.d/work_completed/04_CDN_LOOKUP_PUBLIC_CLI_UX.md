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

- [x] Decide first-release package name, binary name, config location, output formats, and supported Node versions
- [x] Slice 1: help
  - [x] Write one black-box help test
  - [x] Refactor existing argument handling into a Clikt command following the Coupling CLI command pattern
  - [x] Run scoped tests
- [x] Slice 2: version
  - [x] Write one black-box version test
  - [x] Wire package version through the canonical command
  - [x] Run scoped tests
- [x] Slice 3: readable config file
  - [x] Write one test loading the selected config-file location
  - [x] Decode it into the existing configuration model
  - [x] Make base64 compatibility input delegate to the same decoded model
  - [x] Run scoped tests
- [x] Slice 4: provider option
  - [x] Write one test for explicit esm.sh selection
  - [x] Route it through the existing provider-selection seam
  - [x] Run scoped tests
- [x] Slice 5: output behavior
  - [x] Write one test for the selected first-release output option
  - [x] Route all output through the existing formatter boundary
  - [x] Run scoped tests
- [x] Slice 6: errors
  - [x] Write one test for stderr and non-zero exit behavior
  - [x] Add the smallest error translation needed
  - [x] Run scoped tests
- [x] Add any additional approved CLI option as its own one-test slice
- [x] Run packed-artifact and client compatibility checks
- [x] Final refactor pass and move card to `agents.d/work_completed/`

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- Standalone packed-artifact command tests
- `./gradlew :client:lookupCdnUrls`

Results:
- First-release decisions: package `@continuous-excellence/cdn-lookup`; binary `cdn-lookup`; readable config file `.cdn-lookup.json` in the working directory; default standalone output `{"urls":{...}}`; base64 compatibility output remains the legacy URL map; first-release provider `esm.sh`; supported Node versions follow the repo Kotlin/JS Node toolchain and package-manager lock.
- Slice 1: `./gradlew :scripts:cdn-lookup:jsTest` failed first because `CdnLookupCommand` and Clikt dependency were absent, then passed after adding the canonical Clikt command.
- Slice 2: `./gradlew :scripts:cdn-lookup:jsTest` failed first because `--version` was not registered, then passed after adding generated package-version wiring and `versionOption`.
- Slice 3: `./gradlew :scripts:cdn-lookup:jsTest` failed first because `--config` was not registered, then passed after loading readable JSON config through the same decoded model as base64 compatibility config.
- Slice 4: `./gradlew :scripts:cdn-lookup:jsTest` failed first because `--provider` was not registered, then passed after routing provider selection through `selectedCdnProvider`.
- Slice 5: `./gradlew :scripts:cdn-lookup:jsTest` failed first because `--output` was not registered, then passed after routing public output through `formatResult`.
- Slice 6: `./gradlew :scripts:cdn-lookup:jsTest` failed first because unsupported providers escaped as `IllegalStateException`, then passed after translating provider-selection failure into a Clikt error on stderr with status 1.
- Compatibility: `./gradlew :scripts:cdn-lookup:packedExecutableSmokeTest` initially failed because Kotlin/JS passed an empty `args` array through the bin shim; restoring `process.argv.slice(2)` at the entrypoint fixed it while keeping Clikt as the canonical parser. The task then passed.
- Compatibility: `./gradlew :client:lookupCdnUrls` passed.
- Final check: `./gradlew :scripts:cdn-lookup:check` failed first on two `Main.kt` lint issues, then passed after formatting fixes.
- Follow-up from `:client:jsBrowserProductionVite`: production Vite initially failed because Clikt/Mordant treated scoped package imports like `@auth0/auth0-react` as argument-file references, producing usage text in `cdn.json`. Added a command regression test for scoped package imports and disabled `readArgumentFile`; `./gradlew :client:jsBrowserProductionVite` and `./gradlew :scripts:cdn-lookup:check` passed afterward.
