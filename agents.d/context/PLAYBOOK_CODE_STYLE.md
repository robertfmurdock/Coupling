# Code Style Playbook

Load when writing or modifying source code. For examples: `FEW_SHOT_CODE_STYLE.md`.

## TDD: Red-Green-Refactor
**Required for every implementation slice.** Non-negotiable gate: cannot mark implementation complete without test.

### The Cycle
1. **Red**: Write one test for the next behavior. Run it. Confirm it fails for the **right reason** (e.g., "function not found", not "wrong assertion").
2. **Green**: Write simplest code to make that test pass. No more.
3. **Refactor**: Clean up what you just wrote (names, duplication, structure). Tests stay green.
4. **Verify**: Run scoped tests (`./gradlew :module:test`) before moving to next slice.

### Test the Behavior, Not the Plumbing
- Test = executable specification of user-visible behavior or component contract
- Plumbing changes (adding GraphQL query fields, updating imports, config changes) aren't TDD slices
- Wrong: "Test that query includes field X" → this tests syntax, not behavior
- Right: "Test that component renders value from field X" → this tests the feature

### Before You Start
- Search for existing test patterns in the module: `find . -name "*Test.kt" | grep <module>`
- Identify test utilities (builders, fakes, assertions) to reuse
- Confirm test infrastructure exists (if not, add "setup tests" to checklist)

### Test Granularity
- One test = one focused objective
- Verify multiple related outputs in one test
- Multiple tests only for different scenarios/variations
- Only add passing tests if their absence confuses readers

### Testmints Phase Discipline
- `setup` arranges scenario-specific data, collaborators, and the world that enables the feature.
- `exercise` contains the one externally meaningful trigger, or the deliberate sequence that is itself the contract.
- `verify` contains assertions; carry the exercise result and required observations forward explicitly.
- Name the bounded behavior under test. Split scenarios with different subjects instead of hiding them in a generic factory.
- Prefer explicit scenarios and narrowly scoped helpers; introduce shared test machinery only when it improves the case at the call site.

### Kotlin/JS Interop
- The `jstools` convention enables Kotlin's plain-object plugin.
- For known JavaScript request or configuration shapes, prefer `@JsPlainObject external interface` types and generated factories over `json`, `dynamic`, or chained `unsafeCast`s.
- Keep `Json` for genuinely open-ended maps; localize unavoidable JavaScript casts to external bindings or test-double boundaries.
- Follow `libraries/repository/dynamo/.../external/awsdynamoclient/Aws.kt` for the local pattern.

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
