# 5. CDN Lookup Coupling Migration and Release

## Goal
Make the Coupling client invoke the standalone public CLI contract, then finish documentation and npm release automation.

## Constraints

- Requires completion of `04_CDN_LOOKUP_PUBLIC_CLI_UX.md`.
- This is invocation migration, not implementation migration: the client must consume the already-canonical command and lookup engine.
- Do not retain duplicate Gradle-only and npm-only command behavior.
- Remove compatibility inputs only after reference discovery proves no consumer remains.
- Follow Coupling CLI release, package metadata, and publication patterns.
- Keep module-specific packaging automation in `scripts:cdn-lookup` and use lazy, configuration-cache-compatible Gradle APIs.
- **TDD required for behavior changes; packaging changes require a failing pack/install verification before implementation.**

## Checklist

- [ ] Slice 1: client invokes readable public configuration
  - [ ] Add one test or build-level assertion covering the client's generated CDN output
  - [ ] Change `lookupCdnUrls` to invoke the public config/command contract
  - [ ] Preserve profiles, dependencies, externals, globals, and `cdn.json`
  - [ ] Run client lookup verification
- [ ] Slice 2: remove compatibility transport
  - [ ] Search for `--lookup-config-base64` and the Gradle-only executable contract
  - [ ] Remove each compatibility entry point only after its final consumer has moved
  - [ ] Verify the canonical path remains the only implementation
- [ ] Slice 3: release-ready package contents
  - [ ] Make the pack verification fail on missing or unintended files
  - [ ] Complete package metadata and runtime dependency inclusion following Coupling CLI patterns
  - [ ] Run isolated install and invocation
- [ ] Slice 4: publication automation
  - [ ] Add a non-publishing verification of release version/task wiring
  - [ ] Add scoped Gradle publication wiring following Coupling CLI patterns
  - [ ] Do not publish during implementation verification
- [ ] Slice 5: documentation
  - [ ] Document install, usage, config, output, providers, errors, and how to add a provider
  - [ ] Verify documented commands against the packed artifact
- [ ] Run affected module checks, then broad repository check
- [ ] Final refactor pass and move card to `agents.d/work_completed/`
- [ ] Mark the umbrella card complete after all child cards are complete

## Validation

- `./gradlew :scripts:cdn-lookup:check`
- Gradle pack/install smoke-test task
- `./gradlew :client:lookupCdnUrls`
- Current client production bundle task
- `./gradlew check`

Results: [fill as work proceeds]
