# Module Boundaries

## Client (`client/`)
- Owns UI behavior, rendering, and client-side interaction logic.
- Avoid pushing server-side rules into client-only implementations.
- Treat server contracts as external boundaries; do not infer undocumented behavior
  from incidental implementation details.

## Server (`server/`)
- Owns GraphQL schema and resolver behavior.
- Deprecated fields should call canonical command/mutation paths.
- Avoid duplicating logic across old/new resolver entry points.
- When resolver/mutation behavior changes, update server action tests in the same
  change set.
- Keep authoritative validation and policy decisions on server command/mutation
  paths, not spread across resolver entry points.

## SDK (`sdk/`)
- Owns GraphQL operation documents and dispatcher integration surface.
- Dispatchers should map to domain models through canonical paths.
- GraphQL server operation renames/deprecations require synchronized SDK document
  and dispatcher updates.
- SDK mappings should preserve explicit contract intent; avoid silent fallback
  behavior that masks schema/contract drift.

## Shared Libraries (`libraries/`)
- Own shared domain models/utilities and cross-cutting abstractions.
- Do not embed app-specific policy where shared code is expected.
- Introduce shared abstractions only with demonstrated multi-module demand and
  stable semantics.

## Test Modules (`server/actionz`, `e2e`, test libraries)
- Preserve behavioral assertions when migrating command paths.
- Add required authorization stubs for new command path coverage.
- Keep one confidence-anchor test at the boundary level that proves the primary
  cross-module behavior.
- Prefer placing permutations and edge-case matrices in lower-level tests unless
  they alter boundary behavior.

## Cross-Cutting Constraints
- GraphQL contract changes must be synchronized across schema/server/sdk/tests.
- Run `agents.d/utilities/graphql-ref-scan.sh` before and after GraphQL changes as a
  discovery aid only; rely on Gradle tests/checks for verification.
- Keep scope narrow: only touch modules required by the task.
- Client-only UI changes should not modify server/schema/sdk unless explicitly
  required.
- For boundary-crossing changes, explicitly record:
  - boundary assumption(s),
  - invariant(s) that must hold across modules,
  - ownership of each changed seam.
- Tests should verify behavioral intent at the affected boundary, not just internal
  implementation details.
- When proposing test changes, explicitly call out whether a test should move up
  or down an architectural level and why confidence is maintained.
