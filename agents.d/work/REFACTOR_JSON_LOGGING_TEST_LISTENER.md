# Task: Refactor JsonLoggingTestListener for improved maintainability

## Goal
Decompose `JsonLoggingTestListener.kt` (385 lines) into smaller, more focused components to improve readability, testability, and maintainability. The class currently handles multiple responsibilities including test identity tracking, JSON parsing, log normalization, and file appending.

## Hard constraints
- No behavior changes - all existing test logging functionality must work identically
- All tests must continue to pass
- Preserve the exact JSON output format (other systems may depend on it)
- No changes to the public API (how the listener is instantiated and registered)

## Context
This class was identified in the gradle plugin audit as unusually large (10x typical task class size). It implements both `TestListener` and `TestOutputListener` interfaces and handles complex log parsing and normalization logic.

## Current structure
**File**: `coupling-plugins/src/main/kotlin/com/zegreatrob/coupling/plugins/JsonLoggingTestListener.kt` (385 lines)

**Responsibilities**:
1. Test lifecycle tracking (beforeTest, afterTest)
2. Test identity management (suite/test/testId tracking)
3. Test occurrence counting (handling duplicate test names)
4. Output event handling and normalization
5. JSON parsing and extraction
6. Log message normalization (testmints-specific)
7. Command log normalization
8. Test attribution (adding test context to logs)
9. File appending (writing JSONL output)

**Key components**:
- `TestIdentity` data class (suite, test, testId)
- Test descriptor → identity mapping
- Test occurrence tracking
- Multiple normalization functions
- JSON parsing with Jackson ObjectMapper

## Improvement opportunities

### 1. Extract JSON parsing logic
Create a dedicated `TestLogParser` class to handle:
- JSON parsing from output events
- Logger name extraction
- Message extraction
- Properties extraction

### 2. Extract test identity management
Create a `TestIdentityTracker` class to handle:
- Test identity creation from descriptors
- Descriptor → identity mapping
- Test occurrence counting
- Identity key generation

### 3. Extract log normalization logic
Create separate normalization utilities:
- `TestmintsLogNormalizer` - testmints-specific log handling
- `CommandLogNormalizer` - command log normalization
- `TestAttributionHelper` - adding test context to logs

### 4. Keep core listener focused
The main `JsonLoggingTestListener` should:
- Implement TestListener and TestOutputListener interfaces
- Delegate to extracted components
- Orchestrate the overall flow
- Handle file appending (or delegate to TestLoggingFileAppender)

## Success criteria
- JsonLoggingTestListener reduced to ~100-150 lines (orchestration only)
- 3-5 new focused helper classes/objects
- Each helper has a single, clear responsibility
- No behavior changes (verify with existing tests)
- Improved readability and clarity of intent
- `./gradlew check` passes

## Validation strategy
- Start with smallest sufficient validation (`:coupling-plugins:build`)
- Run test logging in a few modules to spot-check behavior
- Run `./gradlew check` to verify all tests still pass
- Manually inspect a few test JSONL output files to verify format unchanged
- Check that testmints log parsing still works correctly

## Implementation approach
Work incrementally:
1. Extract JSON parsing logic into TestLogParser
2. Extract test identity tracking into TestIdentityTracker
3. Extract normalization logic into focused helpers
4. Refactor main listener to use extracted components
5. Verify no behavior changes with full test suite

## Estimated impact
- Lines reduced: ~385 → ~100 in main class + ~300 in focused helpers
- Complexity reduced: Multiple responsibilities → single responsibility per class
- Testability improved: Individual components can be tested in isolation
- Maintainability improved: Changes localized to specific concerns
