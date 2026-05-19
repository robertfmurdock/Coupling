# Task: Organize gradle plugin structure with sub-packages

## Goal
Organize the coupling-plugins source files into logical sub-packages to improve discoverability, maintainability, and clarity of responsibility boundaries. Currently all 22 Kotlin files are in a flat structure under `com.zegreatrob.coupling.plugins`.

## Hard constraints
- No behavior changes - all functionality must work identically
- Maintain backward compatibility (Gradle plugins may be referenced externally)
- All tests must continue to pass
- Package names should align with Gradle conventions

## Current structure
All files in: `coupling-plugins/src/main/kotlin/com/zegreatrob/coupling/plugins/`

**22 files:**
- 3 AI context tasks (AgentBootstrapTask, SyncAiContextTask, ValidateAiContextManifestTask)
- 8 weekly cleanup tasks (WeeklyCleanup*Task files)
- 5 test logging files (JsonLoggingTestListener, TestLogHelpers, TestLoggingFileAppender, WriteTestLog4j2Config, WriteJsTestLogHook)
- 2 JS/npm utilities (JsConstraintExtension, NpmConstrained)
- 2 infrastructure helpers (AwsSsmHelpers, PlanEnvParser)
- 1 node execution helper (NodeExec)
- 1 conventions helper (KotlinConventions)

## Proposed structure

```
com.zegreatrob.coupling.plugins/
├── ai/                           # AI context management tasks
│   ├── AgentBootstrapTask.kt
│   ├── SyncAiContextTask.kt
│   └── ValidateAiContextManifestTask.kt
├── cleanup/                      # Weekly cleanup automation tasks
│   ├── WeeklyCleanupCandidatesTask.kt
│   ├── WeeklyCleanupEvaluateTask.kt
│   ├── WeeklyCleanupLogTask.kt
│   ├── WeeklyCleanupPlanTask.kt
│   ├── WeeklyCleanupRenderLogSummaryTask.kt
│   ├── WeeklyCleanupRenderLogTask.kt
│   ├── WeeklyCleanupRenderPromptTask.kt
│   └── WeeklyCleanupWriteLogEntryTask.kt
├── testlogging/                  # Test logging infrastructure
│   ├── JsonLoggingTestListener.kt
│   ├── TestLoggingFileAppender.kt
│   ├── TestLogHelpers.kt
│   ├── WriteJsTestLogHook.kt
│   └── WriteTestLog4j2Config.kt
├── js/                           # JavaScript/npm support utilities
│   ├── JsConstraintExtension.kt
│   ├── NodeExec.kt
│   └── NpmConstrained.kt
├── conventions/                  # Build convention utilities
│   └── KotlinConventions.kt
└── util/                         # General utilities
    ├── AwsSsmHelpers.kt
    └── PlanEnvParser.kt
```

## Benefits
1. **Improved discoverability**: Related files grouped together
2. **Clear boundaries**: Explicit separation of concerns
3. **Easier navigation**: Logical grouping reduces cognitive load
4. **Better encapsulation**: Sub-packages can have internal visibility
5. **Scalability**: New features have clear homes

## Risks and considerations
1. **Import changes**: All Kotlin files will need updated imports
2. **Convention plugin references**: The `.gradle.kts` plugins reference task classes by name
3. **IDE refactoring**: May need manual fixes after automated refactoring
4. **Binary compatibility**: Package changes affect compiled class locations

## Implementation approach
Work incrementally by feature area:
1. Create sub-package directories
2. Move files in groups (e.g., all test logging files together)
3. Update imports after each group
4. Validate with `./gradlew :coupling-plugins:build` after each group
5. Run full `./gradlew check` after all moves complete

## Alternative: Start small
If full reorganization is too risky, start with just one or two sub-packages:
1. Move test logging files to `testlogging/` sub-package
2. Move weekly cleanup files to `cleanup/` sub-package
3. Evaluate benefits before moving remaining files

## Success criteria
- Logical sub-package structure implemented
- All imports updated correctly
- No behavior changes
- `./gradlew check` passes
- Convention plugins still find their task classes correctly
- Improved code organization clarity

## Validation strategy
- Build coupling-plugins after each file group move
- Verify task registration still works (check task list in a module)
- Run specific task to verify functionality (e.g., `./gradlew agentBootstrap`)
- Run full `./gradlew check` before completion
- Spot-check that weekly cleanup and test logging still work

## Estimated impact
- Improved maintainability (easier to find related code)
- Clearer responsibility boundaries
- Better foundation for future growth
- No performance impact
- Some one-time import churn
