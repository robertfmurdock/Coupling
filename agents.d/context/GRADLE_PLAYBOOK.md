# Gradle Playbook

Use this playbook when a task modifies Gradle build logic, dependency wiring, or
repository automation behavior.

## Scope Classification
- Classify the change before editing:
  - Root build logic (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`)
  - Module build logic (`:module/build.gradle.kts`)
  - Shared convention/plugin logic (`coupling-plugins/`)
  - Dependency/version catalog (`gradle/libs.versions.toml`,
    `coupling-plugins/gradle/libs.versions.toml`)
- Document impacted modules up front.

## Source of Truth Conventions
- Keep dependency versions in version catalog files unless an existing exception
  pattern already exists.
- Keep shared build behavior in convention/plugin code; avoid duplicating the
  same task/dependency/configuration across module build files.
- Keep module-specific behavior local to the owning module.
- Keep root build logic lean: do not accumulate unrelated automation tasks in
  root `build.gradle.kts` when they fit an existing subproject or a dedicated
  automation subproject (for example under `scripts/`).
- Add root-level tasks only when they are true cross-repo orchestration entry
  points that must coordinate multiple modules.

## Task and Automation Conventions
- Express repository automation as Gradle tasks executed via `./gradlew`.
- Prefer typed Gradle tasks and Kotlin DSL configuration over ad hoc shell calls.
- Prefer lazy configuration APIs (`register`, providers) over eager task creation.
- For custom tasks, declare inputs/outputs when applicable to preserve
  incremental and cache-friendly behavior.
- Prefer configuration-cache-compatible task implementations and avoid patterns
  that capture script object state in task actions.
- Treat warnings as errors when practical (for build logic, Gradle/Kotlin DSL,
  and compiler/tooling surfaces) so issues are surfaced early in local and CI
  feedback loops.

### Task Reference Patterns for Configuration Cache Compatibility

When referencing tasks from build configuration, use Provider APIs to avoid
realizing tasks at configuration time:

**Correct patterns (lazy, configuration-cache safe):**
```kotlin
// Access task outputs lazily via Provider
val task = tasks.named<ProcessResources>("jsProcessResources")
from(task.map { it.destinationDir })  // Provider stays lazy

// Or with property delegation (still lazy)
val task by tasks.named<ProcessResources>("jsProcessResources")
from(task.map { it.destinationDir })  // Provider stays lazy
```

**Incorrect patterns (eager, breaks configuration cache):**
```kotlin
val task by tasks.named<ProcessResources>("jsProcessResources")
from(task.destinationDir)  // BAD: realizes task at configuration time

val task = tasks.named<ProcessResources>("jsProcessResources").get()
from(task.destinationDir)  // BAD: .get() forces eager realization
```

**Key principle**: Never access task properties directly at configuration time.
Always wrap access in `.map {}`, `.flatMap {}`, or similar Provider transformations.

See: [Gradle Configuration Cache: Task access](https://docs.gradle.org/current/userguide/configuration_cache_requirements.html#config_cache:requirements:task_access)

### Cross-Project Dependency Patterns

For sharing build outputs between projects, prefer configuration-based artifact
sharing over direct task references:

**Correct pattern (configuration-based, fully lazy):**
```kotlin
// Producer (sdk/build.gradle.kts)
val jsResourcesElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}
artifacts {
    add(jsResourcesElements.name, tasks.named("jsProcessResources").map { 
        (it as ProcessResources).destinationDir 
    })
}

// Consumer (client/build.gradle.kts)
val sdkJsResources by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    sdkJsResources(project(":sdk"))
}
tasks.register("copyResources", Copy::class) {
    from(sdkJsResources)  // Gradle handles task dependencies automatically
    into(layout.buildDirectory.dir("resources"))
}
```

**Avoid:**
```kotlin
// Anti-pattern: cross-project task reference
from(project(":sdk").tasks.named("jsProcessResources").map { ... })

// Anti-pattern: hardcoded paths
from("$rootDir/sdk/build/processedResources/js/main")
```

Cross-project task references can fail with configuration cache, and hardcoded
paths bypass Gradle's dependency tracking.

See: [Gradle: Sharing outputs between projects](https://docs.gradle.org/current/userguide/cross_project_publications.html)

## Validation Ladder
- Run the smallest sufficient check first (for example `./gradlew :module:task`).
- Then run affected module checks.
- Run broader checks (`./gradlew check`, or `build`/`test` as needed) when change
  risk crosses module boundaries.

## Change Coupling Rules
- Keep build logic updates and required consumer updates in one change set.
- If conventions change, update canonical context docs in `agents.d/context/`
  within the same change.
- Do not place durable Gradle conventions only in `AGENTS.md` or generated files.

## Agent Completion Reporting for Gradle Changes
- List Gradle-related files changed and why.
- List Gradle commands executed and summarized outcomes.
- State residual risks (for example CI-only behavior, platform-specific behavior,
  config-cache implications, or checks intentionally deferred).
