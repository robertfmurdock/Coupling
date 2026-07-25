# Publish CDN Lookup as a Standalone npm Tool

## Goal
Incrementally refactor `scripts/cdn-lookup` into a standalone npm CLI whose package-resolution core is independent of CDN-provider behavior.

## Delivery Invariant
This is a refactor of the existing production path, not a rewrite beside it.

- Each child card changes the existing canonical implementation into the next shape.
- Do not create a second lookup engine, argument path, configuration model, or provider implementation for later migration.
- Temporary compatibility entry points must immediately delegate to the canonical path and have an explicit removal condition.
- The existing Coupling client remains a consumer throughout the sequence; each completed card leaves it working.
- Complete and validate cards in order. Do not begin a later card to make an earlier card's design more general.

## Ordered Work Cards

1. [01_CDN_LOOKUP_STANDALONE_WALKING_SKELETON.md](01_CDN_LOOKUP_STANDALONE_WALKING_SKELETON.md) — no-config lookup and an installable npm tarball
2. [02_CDN_LOOKUP_RELIABLE_PACKAGE_RESOLUTION.md](02_CDN_LOOKUP_RELIABLE_PACKAGE_RESOLUTION.md) — accurate, fixture-tested installed-version resolution
3. [03_CDN_LOOKUP_PROVIDER_BOUNDARY.md](03_CDN_LOOKUP_PROVIDER_BOUNDARY.md) — extract current esm.sh behavior behind a provider contract
4. [04_CDN_LOOKUP_PUBLIC_CLI_UX.md](04_CDN_LOOKUP_PUBLIC_CLI_UX.md) — add public command ergonomics one behavior at a time
5. [05_CDN_LOOKUP_COUPLING_MIGRATION_AND_RELEASE.md](05_CDN_LOOKUP_COUPLING_MIGRATION_AND_RELEASE.md) — move the client to the public invocation and add release readiness

## Shared Constraints

- Follow the npm packaging, command structure, release wiring, and test patterns established by the Coupling CLI.
- Red-green-refactor one observable behavior at a time; begin each implementation slice with one focused failing test.
- Preserve existing profile inheritance, dependency groups, peer traversal, subpath imports, externals, globals, and generated client output unless a card explicitly changes them.
- Keep stdout machine-readable and diagnostics on stderr.
- Express build, packaging, publication, and verification automation as Gradle tasks.
- Move each child card to `agents.d/work_completed/` when complete; move this umbrella card only after all child cards are complete.

## Completion Criteria

- A clean JavaScript project can install the packed artifact and generate pinned CDN URLs without Coupling-specific configuration.
- CDN-specific behavior is isolated behind a tested provider boundary.
- The Coupling client exercises the same canonical command path as external consumers.
- Packaging, behavior, configuration, errors, and provider semantics are documented and release-ready.
