# Gradle Plugin Code Quality Audit

## Summary
Audited `coupling-plugins/` (~1,689 lines) to identify improvement opportunities. The plugin structure is generally sound but has inconsistencies and opportunities for quality improvements following Gradle best practices.

## Code Inventory

### Convention Plugins (9 files, ~466 lines)
- `jvm.gradle.kts` (35 lines) — Base JVM module config
- `mp.gradle.kts` (46 lines) — Multiplatform module config
- `jstools.gradle.kts` (65 lines) — JS-specific module config
- `testLogging.gradle.kts` (86 lines) — Test logging infrastructure setup
- `versioning.gradle.kts` (32 lines) — Version configuration
- `reports.gradle.kts` (29 lines) — Test reporting config
- `weekly-cleanup.gradle.kts` (85 lines) — Weekly cleanup task registration
- `linter.gradle.kts` (23 lines) — Linting config
- `deploy.gradle.kts` (90 lines) — Deploy task setup

### Custom Task Classes (19 files, ~1,223 lines total)
**Weekly Cleanup Suite** (~21k bytes / 7 files):
- `WeeklyCleanupPlanTask.kt`
- `WeeklyCleanupEvaluateTask.kt`
- `WeeklyCleanupCandidatesTask.kt`
- `WeeklyCleanupRenderPromptTask.kt`
- `WeeklyCleanupRenderLogTask.kt`
- `WeeklyCleanupRenderLogSummaryTask.kt`
- `WeeklyCleanupWriteLogEntryTask.kt`
- `WeeklyCleanupLogTask.kt`

**AI Context Suite** (~3 files):
- `AgentBootstrapTask.kt` (57 lines)
- `SyncAiContextTask.kt` (196 lines)
- `ValidateAiContextManifestTask.kt`

**Test Logging Suite** (~4 files):
- `JsonLoggingTestListener.kt` (14k bytes — largest file!)
- `TestLogHelpers.kt`
- `WriteTestLog4j2Config.kt`
- `WriteJsTestLogHook.kt`
- `TestLoggingFileAppender.kt`

**Infrastructure Helpers**:
- `NodeExec.kt` (75 lines) — Custom exec task for Node
- `AwsSsmHelpers.kt` — AWS SSM utilities
- `PlanEnvParser.kt` — Environment parsing
- `JsConstraintExtension.kt` / `NpmConstrained.kt` — NPM constraint extensions

## Key Findings

### 1. Inconsistent Lazy Configuration
**Issue**: Mixed use of lazy vs eager task creation patterns.

**Evidence**:
- `testLogging.gradle.kts:24-29` — Uses `findByName()` + conditional `register()` pattern:
  ```kotlin
  val writeLogConfig = rootProject.tasks.findByName("writeTestLog4j2Config")
      ?.let { rootProject.tasks.named(it.name) }
      ?: rootProject.tasks.register("writeTestLog4j2Config", WriteTestLog4j2Config::class.java) {...}
  ```
- `deploy.gradle.kts:21-77` — Consistently uses `registering()` for all tasks
- `testLogging.gradle.kts:8-20` — Has configuration-time execution (file I/O, system properties)

**Impact**: Reduced build performance, configuration cache incompatibility

**Recommendation**: 
- Convert all task registration to `register()` with lazy configuration
- Move configuration-time file I/O into task actions
- Use `Provider` types throughout

### 2. Missing or Incorrect Inputs/Outputs
**Issue**: Custom tasks don't consistently declare inputs/outputs.

**Evidence**:
- `AgentBootstrapTask.kt:11-15` — All properties marked `@Internal`, no inputs or outputs
  ```kotlin
  @get:Internal
  abstract val repoRootDirPath: Property<String>
  
  @get:Internal
  abstract val contextManifestFilePath: Property<String>
  ```
  Should have `contextManifestFilePath` as `@InputFile` and derive output from manifest content.
  
- `SyncAiContextTask.kt:38-39` — Manually disables up-to-date checks:
  ```kotlin
  init {
      outputs.upToDateWhen { false }
  }
  ```
  Should properly declare input files and output files instead.

- `WeeklyCleanupPlanTask.kt:14-32` — Has `@Input` properties but missing `@OutputFile` for `outputFilePath`:
  ```kotlin
  @get:Internal
  abstract val outputFilePath: Property<String>
  ```

**Impact**: 
- Tasks always run (never up-to-date)
- No incremental build benefits
- Configuration cache warnings
- Poor local and CI performance

**Recommendation**: 
- Audit all custom tasks
- Mark file paths as `@InputFile`, `@InputDirectory`, `@OutputFile`, or `@OutputDirectory`
- Mark value inputs as `@Input`
- Remove manual up-to-date overrides

### 3. Duplication in Convention Plugins
**Issue**: Repeated configuration blocks across convention plugins.

**Evidence**:
- All three module plugins (`jvm.gradle.kts`, `mp.gradle.kts`, `jstools.gradle.kts`) have identical:
  - `allWarningsAsErrors = true`
  - `jvmToolchain(22)`
  - Identical `languageSettings.optIn()` lists (5 opt-ins each)
  - Similar `sourceSets.all` configuration
  - Identical dependency-bom enforcement pattern

**Duplication metrics**:
- `optIn()` calls: 15 lines (3 × 5)
- Compiler options: ~9 lines duplicated
- BOM enforcement: 3 identical patterns

**Impact**: ~25+ lines of duplicated configuration logic

**Recommendation**:
- Extract common Kotlin configuration into shared extension function or base plugin
- Create `applyCommonKotlinConfig()` helper
- Apply composition pattern

### 4. Configuration-Time Execution
**Issue**: Side effects at configuration time reduce performance and break configuration cache.

**Evidence**:
- `testLogging.gradle.kts:8-20` — File I/O at configuration time:
  ```kotlin
  val logConfigPath = logConfigFile.get().asFile
  logConfigPath.parentFile.mkdirs()
  logConfigPath.writeText(WriteTestLog4j2Config.buildConfig(logFilePathProvider.get()))
  System.setProperty("log4j2.configurationFile", logConfigPath.absolutePath)
  Configurator.initialize(null, logConfigPath.absolutePath)
  ```

**Impact**: 
- Configuration cache incompatibility
- Slower configuration phase
- Unnecessary file writes on every build

**Recommendation**: 
- Move file creation into task action
- Use lazy providers (`Provider<File>`, etc.)
- Only set system properties in task execution phase

### 5. Oversized Task Class
**Issue**: `JsonLoggingTestListener.kt` is 14k bytes — unusually large for a single class.

**Evidence**: File size is 10x typical task class size

**Recommendation**: 
- Review for decomposition opportunities
- Extract reusable helpers
- Simplify complex logic

### 6. Unclear Helper Organization
**Issue**: Helper classes (`NodeExec`, `AwsSsmHelpers`, `TestLogHelpers`) have mixed responsibility levels.

**Evidence**:
- Some are task base classes (`NodeExec`)
- Some are utility objects (`TestLogHelpers`)
- Some are task-specific (`TestLoggingFileAppender`)

**Recommendation**: 
- Group by responsibility
- Create clear helper packages/conventions
- Consider extracting to separate source sets if reusable beyond plugins

## Improvement Opportunities by Priority

### High Priority (correctness & performance)
1. **Add proper inputs/outputs to all custom tasks** — Enables incremental builds, configuration cache
2. **Convert to lazy configuration patterns** — Remove configuration-time execution
3. **Fix eager task registration** — Convert `findByName` + conditional patterns to pure `register()`

### Medium Priority (maintainability)
4. **Extract common configuration** — Reduce duplication in convention plugins (~25+ lines savings)
5. **Decompose `JsonLoggingTestListener`** — Break down large task class
6. **Organize helpers** — Clear responsibility boundaries

### Low Priority (polish)
7. **Improve naming consistency** — Task naming conventions
8. **Add minimal inline docs** — Non-obvious behavior only
9. **Verify Gradle API usage** — Check for deprecated patterns

## Validation Commands
After changes:
- `./gradlew :coupling-plugins:build` — Quick plugin compilation check
- `./gradlew --configuration-cache check` — Verify configuration cache compatibility
- `./gradlew check` — Full validation
- Spot-check: `./gradlew agentBootstrap`, `./gradlew test`

## Rollout Strategy
Work incrementally in focused slices:
1. ✅ **This audit** — Catalog issues
2. ✅ Fix inputs/outputs on custom tasks (completed in commit 3f16f183)
3. Convert to lazy configuration patterns
4. ✅ Extract common convention plugin code (completed in commit e09b0e91)
5. Simplify complex logic
6. Final validation and compliance check

## Checklist
- [x] Complete audit and catalog issues
- [x] Fix inputs/outputs on custom tasks
- [x] Extract common convention plugin code
- [x] Convert to lazy configuration patterns (completed in commit 2d0631485)
- [x] Review changes against applicable playbooks and verify compliance
- [x] Run `./gradlew check` to verify no regressions
- [ ] Move this file to agents.d/work_completed/

## Estimated Savings
- **Lines reduced**: 20-30% (targeting 400+ lines through deduplication)
- **Build performance**: Configuration phase faster, better up-to-date checking
- **Maintainability**: Single source of truth for common config
