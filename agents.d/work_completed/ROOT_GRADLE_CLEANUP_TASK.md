# Task: Clean up root build.gradle.kts following project conventions and Gradle best practices

## Goal
Refactor root build.gradle.kts to be cleaner, more maintainable, and aligned with both project conventions and Gradle best practices. The file has become messy with recent changes and needs reorganization.

## Hard constraints
- No behavior changes - all tasks must continue to work identically
- Maintain all existing task registrations and their configurations
- Preserve all existing dependencies and task ordering
- Keep all dockerCompose functionality working
- Ensure AI context generation continues to work
- All tests and checks must pass

## Issues identified

### 1. Extract custom task classes to buildSrc (lines 70-346)
**Problem:** 276 lines of task implementation inline in build file
- `SyncAiContextTask` (lines 70-256)
- `ValidateAiContextManifestTask` (lines 258-298)
- `AgentBootstrapTask` (lines 300-346)

**Action:** Move to `buildSrc/src/main/kotlin/com/zegreatrob/coupling/build/tasks/` or create a convention plugin in `buildSrc/src/main/kotlin/com/zegreatrob/coupling/plugins/`

### 2. Simplify dockerCompose configuration (lines 19-42)
**Problem:** Inline AWS SSM parameter fetching with exec + Jackson JSON parsing happens at configuration time
```kotlin
val (sak, pk, sk) = providers.exec {
    commandLine("/bin/bash", "-c", "aws ssm get-parameters...")
}.standardOutput.asText.get().toByteArray().let { ObjectMapper().readValue(it, List::class.java) }
```

**Action:** 
- Extract to a helper function or separate configuration task
- Consider lazy evaluation to avoid blocking configuration phase
- Consider caching mechanism if called multiple times

### 3. Extract helper functions and data classes (lines 358-425)
**Problem:** Business logic embedded in build file
- `registerTestLogCliTask` function (32 lines, lines 358-390)
- `AttributionCoverage` data class (lines 406-411)
- `readAttributionCoverage` function (lines 413-425)

**Action:** Move to buildSrc utilities or convention plugin

### 4. Organize imports (lines 1-6)
**Problem:** Mixed import groups - task imports, Java/Jackson, Gradle API
```kotlin
import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
```

**Action:** Group logically (Java stdlib, Gradle API, third-party, project)

### 5. Simplify testLogToolsRunner configuration (lines 49-61)
**Problem:** Manual attribute wiring is verbose
- 13 lines of boilerplate attribute configuration

**Action:** Consider extracting to a helper function if this pattern is reused, or document why explicit configuration is needed

### 6. Consider convention plugins for major concerns
**Problem:** Build file handles multiple unrelated concerns

**Potential plugins:**
- AI context management plugin (tasks + configuration)
- Test log processing plugin (configuration + tasks + helpers)  
- Docker Compose setup plugin (if reused elsewhere)

## Gradle best practices to follow
- Keep build files declarative
- Extract imperative logic to plugins/buildSrc
- Avoid configuration-time execution where possible (AWS calls)
- Use lazy configuration (`Provider` types)
- Separate concerns into focused plugins
- Minimize build script line count

## Success criteria
- Root build.gradle.kts reduced to < 200 lines
- All custom task classes in buildSrc or convention plugins
- All helper functions and data classes in buildSrc
- Imports organized into logical groups
- Configuration-time AWS calls optimized (lazy/cached)
- All existing tasks work identically
- `./gradlew check` passes
- `./gradlew agentBootstrap` works
- Docker Compose tasks work

## Checklist
- [x] Extract AI context task classes to buildSrc (SyncAiContextTask, ValidateAiContextManifestTask, AgentBootstrapTask)
- [x] Extract test log helper functions and data classes to buildSrc (registerTestLogCliTask, AttributionCoverage, readAttributionCoverage)
- [x] Optimize dockerCompose AWS SSM parameter fetching (extract helper, add lazy evaluation)
- [x] Organize imports into logical groups
- [x] Run `./gradlew check` to verify all functionality
- [ ] Move this file to agents.d/work_completed/
