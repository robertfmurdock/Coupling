# 1. CDN Lookup Standalone Walking Skeleton

## Goal
Refactor the existing command just far enough that basic no-config lookup works from an installable npm tarball while the Coupling client continues to work.

## Constraints

- Depends only on the current implementation; do not introduce a provider abstraction yet.
- Refactor the existing `main` and `generateCdnRef` path. Do not add a parallel standalone command or lookup engine.
- Keep esm.sh as the hard-coded provider for this card.
- Follow the Coupling CLI's npm metadata, executable, tarball, and command-test patterns.
- Keep `--lookup-config-base64` as a delegating compatibility input for the current client.
- **TDD required: one focused test → fail for the expected reason → simplest implementation → pass → refactor.**

## Checklist

- [x] Review card and inspect Coupling CLI packaging and command test patterns
- [x] Slice 1: basic lookup without configuration
  - [x] Write one test proving an installed package can be looked up with empty configuration
  - [x] Confirm failure is caused by the current configuration validation
  - [x] Narrow validation so configuration is required only for configured behavior
  - [x] Run scoped tests
- [x] Slice 2: named command result
  - [x] Write one test specifying the minimum standalone JSON result
  - [x] Refactor the existing result through a named result type and formatter
  - [x] Preserve the current URL-map output as a delegating compatibility formatter
  - [x] Run scoped tests
- [x] Slice 3: packed executable smoke test
  - [x] Write one black-box test that installs the packed artifact and invokes its binary in a fixture project
  - [x] Add only the npm metadata, launcher, and Gradle pack task needed to pass it
  - [x] Keep the launcher pointed at the existing canonical `main`
  - [x] Run scoped tests and inspect packed files
- [x] Verify the current client CDN lookup still passes
- [x] Final refactor pass and move card to `agents.d/work_completed/`

## Explicitly Deferred

- Correcting semver/range resolution
- CDN provider abstraction or additional providers
- Config-file discovery, rich help, multiple output formats, and polished errors
- Publishing to npm

## Implementation Notes

- Empty configuration now permits direct lookup; partial/non-empty import configuration retains strict missing-import validation.
- `generateCdnLookup` is the canonical result path. `generateCdnRef` and the base64-config CLI output delegate to it for compatibility.
- The walking-skeleton npm identity is `@continuous-excellence/cdn-lookup` with binary `cdn-lookup`; first-release naming remains explicitly revisitable in card 04.
- The packed smoke fixture installs `resolve-pkg` and the generated tarball, then invokes the installed binary from the fixture working directory.

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- New scoped Gradle pack/smoke-test task
- `./gradlew :client:lookupCdnUrls`

Results:

- Red: no-config test failed with `Missing import configuration for: resolve-pkg`.
- Red: named-result test failed because `generateCdnLookup` did not exist.
- Red: packed smoke test failed because `cdnLookupNpmPack` did not exist.
- Green: all listed commands passed; packed contents were inspected from npm's manifest output.
- `./gradlew :scripts:cdn-lookup:check` passed.
- `./gradlew check` passed (570 tasks; existing Gradle deprecation and Dockerfile JSON-form warnings remain).
- `git diff --check` passed.
