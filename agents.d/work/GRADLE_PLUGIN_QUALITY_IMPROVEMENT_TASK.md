# Task: Improve custom Gradle plugin quality

## Goal
Refactor and improve the custom Gradle plugins in `coupling-plugins/` to follow Gradle conventions, keep code clean and simple, and maximize reuse. The plugins have accumulated complexity and inconsistencies over time and would benefit from systematic cleanup and standardization.

## Hard constraints
- No behavior changes - all existing functionality must work identically
- All module builds must continue to work
- All tests and checks must pass
- Plugin APIs consumed by modules must remain stable or use deprecation cycles
- Preserve all existing task registrations and their configurations

## Scope
The `coupling-plugins/` directory contains:
- ~1,689 lines of custom plugin code
- 10 convention plugin scripts:
  - `jvm.gradle.kts`
  - `jstools.gradle.kts`
  - `mp.gradle.kts`
  - `testLogging.gradle.kts`
  - `versioning.gradle.kts`
  - `reports.gradle.kts`
  - `weekly-cleanup.gradle.kts`
  - `linter.gradle.kts`
  - `deploy.gradle.kts`
- Multiple custom task classes (e.g., `AgentBootstrapTask`, `SyncAiContextTask`, `WeeklyCleanupPlanTask`, etc.)
- Helper classes and extensions (e.g., `AwsSsmHelpers`, `NodeExec`, `TestLogHelpers`)

## Areas for improvement

### 1. Follow Gradle conventions
**Issues to address:**
- Use lazy configuration APIs (`register`, `Provider` types) instead of eager task creation
- Declare inputs/outputs on custom tasks for proper incremental build and cache behavior
- Use configuration-cache-compatible patterns
- Follow Gradle task naming and organization conventions
- Use typed tasks and proper extension APIs

### 2. Code quality and simplicity
**Issues to address:**
- Reduce duplication across convention plugins
- Extract common patterns into reusable utilities
- Simplify complex logic and reduce nested scopes
- Improve naming clarity for tasks, functions, and types
- Remove dead code or unused functionality
- Keep functions focused and single-purpose

### 3. Reuse and composition
**Issues to address:**
- Identify repeated patterns that can be extracted into shared utilities
- Create base convention plugins or extension functions for common behavior
- Consolidate scattered helper functions into cohesive utility classes
- Apply DRY principle without premature abstraction

### 4. Documentation and intent
**Issues to address:**
- Add minimal inline documentation for non-obvious behavior
- Document inputs/outputs for custom tasks
- Clarify plugin responsibilities and boundaries
- Make configuration DSL more discoverable and type-safe

## Gradle best practices to follow
- Keep plugin code declarative where possible
- Avoid configuration-time execution of expensive operations
- Use lazy configuration (`Provider` types, `register` over `create`)
- Separate concerns into focused plugins
- Prefer composition over inheritance
- Use proper Gradle APIs over workarounds
- Make tasks cacheable and incremental where applicable
- Follow Kotlin coding conventions in `.gradle.kts` files
- Keep plugin files focused on single responsibilities

## Success criteria
- All custom tasks properly declare inputs/outputs
- Lazy configuration used throughout
- Duplication reduced by at least 20% (measured in lines or repeated patterns)
- Code is more readable with clearer intent
- All existing functionality preserved
- `./gradlew check` passes
- `./gradlew build` passes
- All module builds work identically
- No configuration cache warnings from plugin code

## Validation strategy
- Start with smallest sufficient validation (e.g., `:coupling-plugins:build`)
- Run affected module checks after plugin changes
- Run `./gradlew check` before completing work
- Verify no new configuration cache warnings
- Spot-check that common tasks still work (e.g., `agentBootstrap`, test tasks, deploy tasks)

## Rollout approach
Work incrementally in focused slices, keeping the repository in check-in-ready state after each slice:
1. Audit and catalog current plugin code structure and issues
2. Extract common utilities and reduce duplication
3. Convert to lazy configuration patterns
4. Add proper inputs/outputs to custom tasks
5. Simplify complex logic and improve readability
6. Final pass: verify all conventions and run full validation

## Checklist
- [x] Audit current plugin code structure and identify specific improvement opportunities
- [ ] Review changes against applicable playbooks and verify compliance
- [ ] Move this file to agents.d/work_completed/
