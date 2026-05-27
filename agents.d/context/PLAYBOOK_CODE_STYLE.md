# Code Style Playbook

Load when writing or modifying source code. For examples: `FEW_SHOT_CODE_STYLE.md`.

## TDD: Red-Green-Refactor
**Required for every implementation slice.** Non-negotiable gate: cannot mark implementation complete without test.

### The Cycle
1. **Red**: Write one test for the next behavior. Run it. Confirm it fails for the **right reason** (e.g., "function not found", not "wrong assertion").
2. **Green**: Write simplest code to make that test pass. No more.
3. **Refactor**: Clean up what you just wrote (names, duplication, structure). Tests stay green.
4. **Verify**: Run scoped tests (`./gradlew :module:test`) before moving to next slice.

### Before You Start
- Search for existing test patterns in the module: `find . -name "*Test.kt" | grep <module>`
- Identify test utilities (builders, fakes, assertions) to reuse
- Confirm test infrastructure exists (if not, add "setup tests" to checklist)

### Test Granularity
- One test = one focused objective
- Verify multiple related outputs in one test
- Multiple tests only for different scenarios/variations
- Only add passing tests if their absence confuses readers

### Formatting
"Chop down" chains: break before `?.` and `.assertIsEqualTo`

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
