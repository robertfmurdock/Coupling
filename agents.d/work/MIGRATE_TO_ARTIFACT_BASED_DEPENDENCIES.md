# Task: Migrate Cross-Project Resource Dependencies to Configuration-Based Artifact Sharing

## Goal
Replace fragile task-reference-based resource sharing with Gradle's variant-aware configuration system for better configuration cache compatibility and more robust dependency management.

## Context
During configuration cache work, we found that cross-project task references (e.g., `project(":sdk").tasks.named("jsProcessResources")`) break configuration cache. Current workaround uses hardcoded paths like `"$rootDir/sdk/build/processedResources/js/main"`, which is fragile and bypasses Gradle's dependency tracking.

## Current State

### Resource Producers
- `:sdk` module produces JS resources via `jsProcessResources` task
- Output location: `sdk/build/processedResources/js/main`

### Resource Consumers
Three modules need SDK resources with inconsistent patterns:

1. **client/build.gradle.kts** (line 217-225):
   ```kotlin
   val additionalResources by registering(Copy::class) {
       dependsOn(":sdk:jsProcessResources")
       from("$rootDir/sdk/build/processedResources/js/main")  // Hardcoded path
       into(project.layout.buildDirectory.file("additionalResources"))
   }
   ```

2. **cli/build.gradle.kts** (line 119-123):
   ```kotlin
   register("dependencyResources", Copy::class) {
       dependsOn(":sdk:jsProcessResources")
       into(jsProcessResources.map { it.destinationDir })
       from("$rootDir/sdk/build/processedResources/js/main")  // Hardcoded path
   }
   ```

3. **e2e/build.gradle.kts** (line 90-94):
   ```kotlin
   val dependencyResources by registering(Copy::class) {
       dependsOn(":sdk:jsProcessResources")
       into(jsE2eTestProcessResources.map { it.destinationDir })
       from("$rootDir/sdk/build/processedResources/js/main")  // Hardcoded path
   }
   ```

## Target Pattern: Configuration-Based Artifact Sharing

### Producer Side (sdk/build.gradle.kts)
```kotlin
// Create a consumable configuration for JS resources
val jsResourcesElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "js-resources"))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
    }
}

// Publish the processed resources directory as an artifact
artifacts {
    add(jsResourcesElements.name, tasks.named("jsProcessResources").map { 
        (it as ProcessResources).destinationDir 
    }) {
        builtBy(tasks.named("jsProcessResources"))
    }
}
```

### Consumer Side (client/cli/e2e)
```kotlin
// Create a resolvable configuration to consume SDK resources
val sdkJsResources by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "js-resources"))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
    }
}

dependencies {
    sdkJsResources(project(":sdk"))
}

// Use in Copy tasks
val additionalResources by registering(Copy::class) {
    from(sdkJsResources)  // Gradle handles dependency wiring automatically
    into(project.layout.buildDirectory.file("additionalResources"))
}
```

## Benefits
1. **Configuration cache compatible**: No task instance references
2. **Dependency tracking**: Gradle automatically wires task dependencies
3. **Variant-aware**: Could support different resource variants (dev/prod, locales, etc.) in future
4. **Explicit contracts**: Clear producer/consumer relationship via attributes
5. **Robust**: No hardcoded paths that break if build output structure changes
6. **Maintainable**: Standard Gradle pattern, easier for new contributors

## Implementation Strategy

### Phase 1: Add Producer Configuration (sdk)
1. Create `jsResourcesElements` configuration in `sdk/build.gradle.kts`
2. Wire it to `jsProcessResources` task output
3. Verify configuration is visible: `./gradlew :sdk:outgoingVariants`

### Phase 2: Migrate Consumers One at a Time
For each module (client, cli, e2e):
1. Add `sdkJsResources` configuration
2. Add `dependencies { sdkJsResources(project(":sdk")) }`
3. Replace `from("$rootDir/sdk/...")` with `from(sdkJsResources)`
4. Remove manual `dependsOn(":sdk:jsProcessResources")`
5. Validate: `./gradlew :module:check`

### Phase 3: Verify Configuration Cache
1. Run `./gradlew check --configuration-cache`
2. Verify no new problems introduced
3. Confirm existing hardcoded-path workarounds are replaced

## Validation Strategy
After each phase:
1. Run module-specific checks: `./gradlew :module:check`
2. Verify resources are copied correctly (spot-check output directories)
3. Run configuration cache validation: `./gradlew check --configuration-cache`
4. Verify dependency graph: `./gradlew :module:dependencies --configuration sdkJsResources`

## Success Criteria
- SDK resources accessible via configuration API in all three consumer modules
- No hardcoded `$rootDir/sdk/build/...` paths remain
- Configuration cache problems do not increase
- All tests pass
- Dependency wiring is automatic (no manual `dependsOn`)

## Risks and Considerations
1. **Attribute matching**: Must use consistent attributes between producer/consumer or resolution fails
2. **Build script complexity**: Adds boilerplate, but follows Gradle conventions
3. **Learning curve**: Configuration API is less familiar than direct task references
4. **Migration scope**: All three consumers must migrate for consistency

## Documentation Updates Needed
After completion, update `GRADLE_PLAYBOOK.md` to document:
- When to use configuration-based artifact sharing vs. simple task outputs
- Example producer/consumer configuration setup
- Attribute conventions for this repo

## References
- Gradle docs: [Sharing outputs between projects](https://docs.gradle.org/current/userguide/cross_project_publications.html)
- Gradle docs: [Declaring dependencies between subprojects](https://docs.gradle.org/current/userguide/declaring_dependencies_between_subprojects.html)
- Configuration cache requirements: https://docs.gradle.org/9.5.1/userguide/configuration_cache_requirements.html
