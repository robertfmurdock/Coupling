# Module Boundaries

## Client (`client/`)
- Owns UI behavior, rendering, and client-side interaction logic.
- Avoid pushing server-side rules into client-only implementations.

## Server (`server/`)
- Owns GraphQL schema and resolver behavior.
- Deprecated fields should call canonical command/mutation paths.
- Avoid duplicating logic across old/new resolver entry points.

## SDK (`sdk/`)
- Owns GraphQL operation documents and dispatcher integration surface.
- Dispatchers should map to domain models through canonical paths.

## Shared Libraries (`libraries/`)
- Own shared domain models/utilities and cross-cutting abstractions.
- Do not embed app-specific policy where shared code is expected.

## Test Modules (`server/actionz`, `e2e`, test libraries)
- Preserve behavioral assertions when migrating command paths.
- Add required authorization stubs for new command path coverage.

## Cross-Cutting Constraints
- GraphQL contract changes must be synchronized across schema/server/sdk/tests.
- Run `scripts/graphql-ref-check.sh` before and after GraphQL changes.
- Keep scope narrow: only touch modules required by the task.

