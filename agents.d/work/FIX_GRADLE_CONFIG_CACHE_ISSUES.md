# Task: Fix Gradle Configuration Cache Issues

## Goal
Resolve as many of the 60 configuration cache problems (56 unique) as possible in a reasonable amount of time to enable faster build times and better Gradle performance through configuration cache support.

## Hard constraints
- No behavior changes to build outputs or test execution
- All tests must continue to pass
- Build tasks must produce identical artifacts
- Changes must be incremental and verifiable
- Each fix group must be validated before moving to the next

## Problem Analysis

Based on the configuration cache report, there are two main categories of issues:

### 1. KotlinJsTest and KotlinJvmTest: "cannot serialize Gradle script object references"
**Affected tasks:** 15+ test tasks across multiple modules
- `:cli:jsNodeTest`, `:cli:jvmTest`
- `:cli:test-log-tools:jvmTest`
- `:client:components:external:jsNodeTest`
- `:client:components:graphing:jsNodeTest`
- `:client:components:jsNodeTest`
- `:client:jsBrowserTest`
- `:deploy:prerelease:jsNodeTest`, `:deploy:prod:jsNodeTest`, `:deploy:sandbox:jsNodeTest`
- `:e2e:jsNodeTest`

**Root cause:** The `testLogging.gradle.kts` convention plugin creates `JsonLoggingTestListener` instances at configuration time and captures script-scoped state:
- Line 50: `JsonLoggingTestListener(path, testRunIdentifier, logFilePathProvider.get())` is evaluated at configuration time
- Lines 78-85: `doFirst`/`doLast` blocks capture `logFilePathProvider.get()` and `path` eagerly
- These capture references to the build script object, which cannot be serialized

### 2. DefaultIncrementalSyncTask: "cannot serialize object of type 'ProcessResources'"
**Affected tasks:** 3+ sync tasks
- `:client:components:graphing:jsTestTestDevelopmentExecutableCompileSync`
- `:client:components:jsTestTestDevelopmentExecutableCompileSync`
- `:client:jsProductionExecutableCompileSync`
- `:client:jsTestTestDevelopmentExecutableCompileSync`

**Root cause:** Build files reference `ProcessResources` task instances directly:
- `client/build.gradle.kts:220`: `(findByPath(":sdk:jsProcessResources") as ProcessResources).destinationDir`
- `cli/build.gradle.kts:115-122`: `jsProcessResources.destinationDir` references
- These create task-to-task references that capture Task instances in configuration cache state

## Implementation Strategy

Work in order of impact (most occurrences first) and validate incrementally.

### Phase 1: Fix test logging script object references (affects 15+ tasks)
**Target:** `coupling-plugins/src/main/kotlin/com/zegreatrob/coupling/plugins/testLogging.gradle.kts`

**Changes needed:**
1. Replace eager `logFilePathProvider.get()` calls with lazy `Provider` references
2. Move `JsonLoggingTestListener` instantiation from configuration time to execution time via `doFirst`
3. Use `providers.provider {}` or task properties to capture values lazily
4. Replace script-scoped variable captures in `doFirst`/`doLast` with `Provider.map {}` or task inputs

**Validation:** Run a sample of affected test tasks to verify logging still works

### Phase 2: Fix ProcessResources task serialization (affects 3+ tasks)
**Target files:**
- `client/build.gradle.kts` (line 220)
- `cli/build.gradle.kts` (lines 115-122)
- `e2e/build.gradle.kts` (lines 86-92)

**Changes needed:**
1. Replace direct task instance references with `Provider<Directory>` or task output properties
2. Use `tasks.named("jsProcessResources").flatMap { it.destinationDir }` instead of casting to `ProcessResources`
3. Wire task dependencies via `dependsOn` and use output properties for file locations

**Validation:** Verify affected modules build correctly and resources are in expected locations

### Phase 3: Additional issues (time permitting)
If time allows after Phases 1-2, investigate the remaining 41 problems mentioned in the report.

## Expected Impact
- **Phase 1:** Should resolve ~15 unique configuration cache warnings (all KotlinJsTest/KotlinJvmTest)
- **Phase 2:** Should resolve ~3-4 unique warnings (DefaultIncrementalSyncTask issues)
- **Total:** ~18-19 of 56 unique problems resolved (32-34%)
- **Build impact:** Significant improvement in configuration cache reusability for test and sync tasks

## Risks and Considerations
1. **Test logging behavior:** Changes to `testLogging.gradle.kts` affect all test tasks; must verify output format unchanged
2. **Resource location assumptions:** Code may assume `jsProcessResources.destinationDir` is available at configuration time
3. **Task execution order:** Provider-based wiring changes task graph construction; verify dependencies preserved
4. **Gradle version compatibility:** Configuration cache behavior may vary; test with Gradle 9.5.1
5. **Incomplete fix:** Some root causes may be in third-party plugins (Kotlin JS, Apollo, etc.) and unfixable in this repo

## Validation Strategy
After each phase:
1. Run affected module checks: `./gradlew :module:check`
2. Verify configuration cache report improves: check problem count
3. Run full `./gradlew check --configuration-cache` to catch cross-module issues
4. Spot-check test log output format in `build/test-output/test.jsonl`
5. Verify build artifacts unchanged (checksums match if possible)

## Success Criteria
- At least 15 configuration cache warnings resolved (Phase 1 complete)
- All existing tests pass with identical behavior
- Test logging continues to function correctly
- Build produces identical artifacts
- Configuration cache can be stored without critical errors
- Changes follow configuration-cache-compatible patterns per Gradle docs

## References
- Gradle config cache requirements: https://docs.gradle.org/9.5.1/userguide/configuration_cache_requirements.html
- Disallowed types: script objects, Task instances
- Recommended patterns: lazy providers, task properties, `Provider.map {}`
