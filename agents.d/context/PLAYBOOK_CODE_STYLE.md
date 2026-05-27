# Code Style Playbook

Load when writing or modifying source code. For examples: `FEW_SHOT_CODE_STYLE.md`.

## TDD: Red-Green-Refactor
- **One test at a time** — write, fail, fix, pass, repeat (overrides task instructions)
- Only add passing tests if their absence confuses readers
- One test = one focused objective
- Verify multiple related outputs in one test
- Multiple tests only for different scenarios/variations
- "Chop down" chains: break before `?.` and `.assertIsEqualTo`

```kotlin
// Good
data["version"]?.jsonPrimitive?.content.assertIsEqualTo("1.2.4")

// Better
data["version"]
    ?.jsonPrimitive
    ?.content
    .assertIsEqualTo("1.2.4")
```

## Assertions
- Prefer `assertIsEqualTo` (clearer diffs)
- Test behavior, not structure (no symbolic assertNotNull/type checks)
- When adding alternatives, test both old and new APIs

## Functions & Comments
- Target <10 lines per function (clarity wins over brevity)
- Name intent, not implementation
- Refactor comments into code; keep only WHY that code can't express

## Data Flow
- Prefer immutable + functional transforms (`map`, `filter`, `fold`)
- Avoid loops with `break`, `continue`, mutable accumulators

## Deprecation
1. Build and test new feature first (full parity)
2. Annotate: why, replacement, when removed
3. Test both APIs

## Scope
- Keep edits minimal
- Feature/bugfix: scope = any function touching changed lines
- Refactor: scope = any file touching changed lines
- Preserve behavior unless task changes it
