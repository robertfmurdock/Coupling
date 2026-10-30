# Production Code Style

Load for production source edits. For tests, use [TESTING.md](TESTING.md); for
illustrative production patterns, consult [FEW_SHOT_CODE_STYLE.md](FEW_SHOT_CODE_STYLE.md).
The contribution sequence is defined in [AGENTS.md](../../AGENTS.md).

## Design

- Prefer small functions with one clear responsibility; target fewer than ten lines
  when that improves clarity, rather than extracting merely to satisfy a count.
- Name intent, not implementation. Extract well-named private functions when they
  make the flow easier to follow.
- Prefer immutable data and functional/composable transforms (`map`, `filter`,
  `fold`) over inheritance-heavy designs and mutable accumulators.
- Reuse established capabilities. Introduce abstractions when demonstrated demand
  and stable semantics justify them, not for hypothetical future needs.
- Make ownership and contracts explicit; follow the architecture guidance for
  cross-module changes. Keep edits scoped to the requested outcome.
- Prefer code that expresses its intent. Retain comments explaining reasons and
  constraints that the code cannot express.

## Kotlin/JS Interop

- The `jstools` convention enables Kotlin's plain-object plugin.
- For known JavaScript request/configuration shapes, prefer
  `@JsPlainObject external interface` types and generated factories over `json`,
  `dynamic`, or chained `unsafeCast`s.
- Keep `Json` for genuinely open-ended maps; localize unavoidable JavaScript casts
  to external bindings or test-double boundaries.
- Follow `libraries/repository/dynamo/.../external/awsdynamoclient/Aws.kt` for the
  local pattern.

## Formatting

Follow local Kotlin formatting. Break long chains before `?.` and assertion calls:

```kotlin
data["version"]
    ?.jsonPrimitive
    ?.content
    .assertIsEqualTo("1.2.4")
```

## Deprecation

Establish and test the replacement's behavior before deprecating the old API.
Document why, the replacement, and removal timing. Keep deprecated paths delegated
to canonical behavior and cover both supported APIs. For GraphQL, follow the
[GraphQL playbook](PLAYBOOK_GRAPHQL.md).
