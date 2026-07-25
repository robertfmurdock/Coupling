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

- [ ] Review card and inspect Coupling CLI packaging and command test patterns
- [ ] Slice 1: basic lookup without configuration
  - [ ] Write one test proving an installed package can be looked up with empty configuration
  - [ ] Confirm failure is caused by the current configuration validation
  - [ ] Narrow validation so configuration is required only for configured behavior
  - [ ] Run scoped tests
- [ ] Slice 2: named command result
  - [ ] Write one test specifying the minimum standalone JSON result
  - [ ] Refactor the existing result through a named result type and formatter
  - [ ] Preserve the current URL-map output as a delegating compatibility formatter
  - [ ] Run scoped tests
- [ ] Slice 3: packed executable smoke test
  - [ ] Write one black-box test that installs the packed artifact and invokes its binary in a fixture project
  - [ ] Add only the npm metadata, launcher, and Gradle pack task needed to pass it
  - [ ] Keep the launcher pointed at the existing canonical `main`
  - [ ] Run scoped tests and inspect packed files
- [ ] Verify the current client CDN lookup still passes
- [ ] Final refactor pass and move card to `agents.d/work_completed/`

## Explicitly Deferred

- Correcting semver/range resolution
- CDN provider abstraction or additional providers
- Config-file discovery, rich help, multiple output formats, and polished errors
- Publishing to npm

## Validation

- `./gradlew :scripts:cdn-lookup:jsTest`
- New scoped Gradle pack/smoke-test task
- `./gradlew :client:lookupCdnUrls`

Results: [fill as work proceeds]
