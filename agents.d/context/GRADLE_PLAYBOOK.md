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

## Tooling Purpose and Lifetime

Treat committed tooling as maintained product code: it adds dependencies,
execution paths, tests and future work. Before adding or expanding a tool, name
its recurring user/workflow, the gap in existing capabilities, its owning module
and its supported contract. An investigation, a work-card evidence requirement,
or wrapping a script in a Gradle task does not by itself justify permanent code.

- Reuse existing application paths, test suites, Gradle tasks and external tools
  first. Extend the owner of the behavior rather than creating a parallel runner,
  configuration path, fixture framework or reporting subsystem.
- Keep one-off probes and experimental scripts in temporary locations outside
  tracked source. Retain concise findings, sanitized evidence, conditions and
  limitations. Remove experimental source and wiring before the slice is committed;
  do not archive executable source in research directories for hypothetical reuse.
- Promote an experiment to maintained tooling only when the recurring need is
  concrete and within the agreed scope. Describe its users, owner, invocation,
  inputs/outputs, side effects, cleanup and maintenance cost in the slice design.
  If that changes the task's scope materially, resolve it with the owner first.
- For retained repository automation, use Gradle as the execution and dependency
  model, with logic in the appropriate module and established project language
  and conventions. A Gradle wrapper around an ad hoc program does not establish
  reuse, ownership or correctness; apply the task conventions below as well.
- Test application behavior at its existing owning boundary. Add tests of a tool
  when that tool's supported behavior is itself an intended maintained deliverable;
  proving a new probe works is not progress on the application's contract.
- Remove superseded tooling and its exclusive tests, dependencies and wiring.
  Passing tests and dormant/opt-in status do not establish ongoing value. In work
  cards, distinguish historical experiments from current implementation instructions.

## Task and Automation Conventions
- Express repository automation as Gradle tasks executed via `./gradlew`.
- Use the same task graph and artifact generation locally and in CI. CI metadata
  may enrich outputs; its absence must not skip core build or reporting behavior.
  Keep GitHub-specific integration in the workflow; see [GitHub Actions](GITHUB_ACTIONS_PLAYBOOK.md).
- Prefer typed Gradle tasks and Kotlin DSL configuration over ad hoc shell calls.
- Prefer lazy configuration APIs (`register`, providers) over eager task creation.
- For custom tasks, declare inputs/outputs when applicable to preserve
  incremental and cache-friendly behavior.
- Prefer configuration-cache-compatible task implementations and avoid patterns
  that capture script object state in task actions.
- Treat warnings as errors when practical (for build logic, Gradle/Kotlin DSL,
  and compiler/tooling surfaces) so issues are surfaced early in local and CI
  feedback loops.

### Build Cache Policy

- Keep build-cache reuse enabled for deterministic, input-complete tasks,
  including expensive test tasks. Re-running an unchanged deterministic test is
  wasted work; make every behavior-affecting value an input instead.
- Do not remove an explicit `outputs.cacheIf { true }` merely to satisfy a
  generic audit. First inspect the task's history, task type, declared
  inputs/outputs, and cache behavior. External task types may require an
  explicit opt-in even when their work is safe to reuse.
- Cacheability does not define execution order. Preserve ordering and output
  availability with task dependencies and input/output wiring; a cache hit must
  restore declared outputs before their consumers execute.
- For owned task types, prefer `@CacheableTask` when every instance of that
  type is deterministic. Use a separate cacheable subtype when only selected
  task instances meet that contract.

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

First establish which files the consumer needs and whether its existing plugin
or dependency handling already delivers them. Remove obsolete copies when the
producer has no required resources; do not introduce an artifact configuration
for an empty contract. Resource-processing tasks should own their destinations;
avoid separate copy tasks writing into those same directories.

For necessary sharing, expose a producer-owned output as a consumable variant.
Give producer and consumer matching attributes that identify its contents and
format. Define whether dependencies belong in that contract; a directory of only
the producer's resources should resolve non-transitively and should not extend
runtime configurations. Keep shared attribute definitions in convention code
when real consumers reuse them.

**Example for a demonstrated resource-directory contract (not currently needed
by the SDK):**
```kotlin
// Producer (sdk/build.gradle.kts)
val resourceKind = Attribute.of("com.zegreatrob.resource-kind", String::class.java)
val jsResourcesElements = configurations.consumable("jsResourcesElements") {
    attributes {
        attribute(resourceKind, "sdk-js-main-directory")
    }
}
val processedJsResources = tasks.named<ProcessResources>("jsProcessResources")
artifacts {
    add(jsResourcesElements.name, processedJsResources.map { it.destinationDir }) {
        builtBy(processedJsResources)
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
}

// Consumer (client/build.gradle.kts)
val resourceKind = Attribute.of("com.zegreatrob.resource-kind", String::class.java)
val sdkJsResourceDependencies = configurations.dependencyScope("sdkJsResourceDependencies")
val sdkJsResources = configurations.resolvable("sdkJsResources") {
    extendsFrom(sdkJsResourceDependencies.get())
    isTransitive = false // This contract includes only the SDK's own directory.
    attributes {
        attribute(resourceKind, "sdk-js-main-directory")
    }
}
dependencies {
    add(sdkJsResourceDependencies.name, project(":sdk"))
}
tasks.named<ProcessResources>("jsProcessResources") {
    from(sdkJsResources) // Artifact provenance schedules the producer.
}
```

`ProcessResources` above is `org.gradle.language.jvm.tasks.ProcessResources`;
`ArtifactTypeDefinition` is `org.gradle.api.artifacts.type.ArtifactTypeDefinition`.
The role-specific configuration APIs separate declarations, consumption and
resolution. Resolve through task inputs, not during configuration. Attach the
artifact to the actual producing task and preserve its build dependency; do not
add a consumer-side `dependsOn(":sdk:jsProcessResources")`. The consumer's normal
resource task owns final assembly and its existing collision policy must remain
explicit and deterministic. If resources become transitive, define and validate
that expanded contract before enabling traversal.

**Avoid:**
```kotlin
// Anti-pattern: cross-project task reference
from(project(":sdk").tasks.named("jsProcessResources").map { ... })

// Anti-pattern: hardcoded paths
from("$rootDir/sdk/build/processedResources/js/main")
```

Cross-project task references can fail with configuration cache, and hardcoded
paths bypass Gradle's dependency tracking.

See: [Gradle: Sharing outputs between projects](https://docs.gradle.org/current/userguide/how_to_share_outputs_between_projects.html)

## Validation Ladder
- Run the smallest sufficient check first (for example `./gradlew :module:task`).
- Then run affected module checks.
- Run `./gradlew check` before each safe slice commit, as required by
  [AGENTS.md](../../AGENTS.md). Use `build`/`test` additionally when the task needs them.

### Browser Bundle and CDN Delivery Changes

Removing a CDN import or external declaration can move code into local bundles.
Compare the full emitted production JS graph, including lazy chunks, and actual
browser network evidence with app source/assets held fixed. Record any tradeoff;
fewer CDN requests alone does not establish a smaller delivered payload. Inspect
emitted consumers and surviving peer/external URLs as well as source references.
Use the [performance harness](../../e2e/performance/README.md) where applicable;
keep measured byte/request changes separate from timing claims.

### AWS SSO and Gradle Daemons

- Before treating an AWS failure as an expired login, verify the active shell
  with `aws sts get-caller-identity`.
- If that succeeds but Gradle reports it cannot start `aws`, stop the daemon
  with `./gradlew --stop` and retry once. Gradle daemons retain the environment
  from when they started, including `PATH` and AWS profile variables.
- If the identity check reports missing or expired SSO credentials, ask the
  repository owner to run `aws sso login`; do not modify AWS profiles or try to
  bypass the configured credentials.

### Browser Tests and Sandboxed Daemons

Chrome launched by Gradle inherits the daemon's execution restrictions. On this
macOS agent host, browser startup has failed inside the sandbox and succeeded
outside it. Do not spend an initial run rediscovering this restriction.

- Run Gradle outside the agent sandbox from the first attempt, including focused
  Node/JVM tasks: they can otherwise start a sandboxed daemon that a later browser
  task reuses. Use the execution tool's outside-sandbox mode and existing matching
  permissions (such as `gradle-docker`) without asking for redundant confirmation.
  For `exec_command`, use `sandbox_permissions = "require_escalated"`; the active
  execution policy determines whether additional approval is actually needed.
- Reuse daemons started outside the sandbox. Do not routinely add `--no-daemon`:
  browser tests require an unsandboxed daemon, not a fresh or single-use daemon.
- If a daemon was accidentally started inside the sandbox, run `./gradlew --stop`
  outside the sandbox before retrying browser-bearing tasks, then run normally.
  If provenance is unknown, establish it or stop idle daemons once before the
  browser run. Avoid stopping another contributor's active build.
- Invoke `./gradlew` directly when the required environment is already available.
  Prefer the execution tool's output/session capture to shell redirection and
  task-specific log filenames, which can prevent existing command approvals from
  matching. Use a shell wrapper only when needed for environment setup (for example,
  fish-loaded SDK credentials); reuse an existing matching approval when available.
- If outside-sandbox execution is denied, report that blocker. Do not retry the
  same sandboxed browser launch, weaken tests, disable Chrome's own sandbox, or
  change global agent permissions to work around it.

Distinguish startup failures (`session not created`, Chrome exits before any
tests) from assertion failures after a browser session starts. A completed browser
suite with a navigation/assertion failure is not evidence to repeat sandbox
diagnosis. Record the actual failure and investigate its boundary.
For crashes in downloaded Chrome after an outside-sandbox run, see
[Chrome for Testing startup failures](TESTING.md#chrome-for-testing-startup-failures).

## Change Coupling Rules
- Keep build logic updates and required consumer updates in one change set.
- If conventions change, update canonical context docs in `agents.d/context/`
  within the same change.
- Keep detailed Gradle conventions here; link from `AGENTS.md` rather than duplicating them.

## Experimental Kotlin Gradle APIs

`ExperimentalKotlinGradlePluginApi` is intentionally used only for Kotlin
compiler-option configuration and the CLI npm-project directory wiring. Keep
the opt-in as narrow as the Kotlin DSL allows, document the consuming code's
reason, and re-evaluate each usage when upgrading Kotlin or Gradle.

## Agent Completion Reporting for Gradle Changes
- List Gradle-related files changed and why.
- List Gradle commands executed and summarized outcomes.
- State residual risks (for example CI-only behavior, platform-specific behavior,
  config-cache implications, or checks intentionally deferred).
