# Module Boundaries

## Client (`client/`)
- Owns UI behavior, rendering, and client-side interaction logic.
- Avoid pushing server-side rules into client-only implementations.

## Server (`server/`)
- Owns GraphQL schema and resolver behavior.
- Deprecated fields should call canonical command/mutation paths.
- Avoid duplicating logic across old/new resolver entry points.
- When resolver/mutation behavior changes, update server action tests in the same
  change set.

## SDK (`sdk/`)
- Owns GraphQL operation documents and dispatcher integration surface.
- Dispatchers should map to domain models through canonical paths.
- GraphQL server operation renames/deprecations require synchronized SDK document
  and dispatcher updates.

## Shared Libraries (`libraries/`)
- Own shared domain models/utilities and cross-cutting abstractions.
- Do not embed app-specific policy where shared code is expected.

## Test Modules (`server/actionz`, `e2e`, test libraries)
- Preserve behavioral assertions when migrating command paths.
- Add required authorization stubs for new command path coverage.

## Cross-Cutting Constraints
- GraphQL contract changes must be synchronized across schema/server/sdk/tests.
- Run `agents.d/utilities/graphql-ref-scan.sh` before and after GraphQL changes as a
  discovery aid only; rely on Gradle tests/checks for verification.
- Keep scope narrow: only touch modules required by the task.
- Client-only UI changes should not modify server/schema/sdk unless explicitly
  required.
