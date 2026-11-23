# CPL-21 — Make required SDK resource delivery explicit and reliable

**Work card protocol: 2**

## Goal and scope

Ensure required SDK resources reach the client, CLI, and E2E runtime or packaged
outputs through explicit Gradle inputs and producer-owned outputs. Preserve
existing application behavior, resource contents, and package layout. Remove
obsolete copies where evidence shows they serve no required delivery path.
Configuration-based artifact sharing is the preferred mechanism for necessary
cross-project sharing, rather than an acceptance requirement in its own right.

Scope includes `sdk/build.gradle.kts`, the resource wiring in
`client/build.gradle.kts`, `cli/build.gradle.kts`, and `e2e/build.gradle.kts`, and
existing convention/plugin code only where shared behavior belongs there.
One contributor owns the active slice across these boundaries; record their name
when implementation starts. No cross-card prerequisite is currently identified.

Follow [AGENTS.md](../../AGENTS.md), the
[work-card protocol](../context/WORK_CHECKLIST.md), and the
[Gradle playbook](../context/GRADLE_PLAYBOOK.md). This is planned as a
behavior-preserving build refactor. Do not invent failing tests for mechanical
rewiring or removal of proven redundant wiring. If investigation finds an actual
behavior defect, record its contract, scenarios, and expected failure before
focused tests, then the smallest implementation and data flow after confirming
that failure and before production edits.

## Starting evidence and open question

Source review on 2026-11-23 found these existing paths:

| Consumer | Current wiring | Delivery boundary to establish |
| --- | --- | --- |
| Client | `additionalResources` copies SDK processed resources to `build/additionalResources`; `jsProcessResources` depends on that task. | Which task consumes that staging directory and how required files reach the browser output. |
| CLI | `dependencyResources` copies SDK processed resources into the CLI `jsProcessResources` destination. | Whether normal dependency handling already supplies the files and which files the JS distribution needs. |
| E2E | `dependencyResources` copies SDK processed resources into the E2E resource-processing destination. | Which files the runnable E2E compilation needs and whether its existing dependency path supplies them. |

All three copies use `sdk/build/processedResources/js/main` and an explicit
`:sdk:jsProcessResources` dependency. These edges provide execution ordering but
leave the producer's output location encoded in consumers.

No checked-in SDK resource files were found in that review. Generated resources
may still be required; absence of source resources does not prove these tasks
are redundant. Before choosing migration or deletion, identify actual produced
files and their consumers. Record findings here, including generation conditions
and any missing evidence. The historical card assumed all three copies were
necessary; that assumption is now open.

## Design constraints

- Prefer existing Kotlin/JS dependency and resource handling if it already
  delivers the required files. Avoid parallel delivery paths.
- Where explicit sharing remains necessary, the SDK owns a consumable artifact
  backed by its actual resource-producing task output. Consumers resolve that
  artifact as an input; producer execution follows from artifact provenance.
  Consumers must not inspect SDK tasks or encode SDK build-directory internals.
- Define the smallest resource contract needed by real consumers, including
  contents, attributes, and transitivity as applicable. Inspect existing project
  configurations and conventions before adding another one.
- Keep shared configuration behavior in existing convention/plugin ownership
  where appropriate, with module-specific assembly local. Do not copy an entire
  configuration recipe into three modules or introduce a generic resource
  framework without demonstrated stable demand.
- Preserve deterministic resource ownership, collision handling, and final
  destinations. In CLI and E2E, inspect the existing multiple-task writes to a
  resource-processing destination before retaining or changing that arrangement.
- Do not introduce dev/prod resource variants, environment-specific application
  behavior, external artifact publication, or a broader configuration-cache
  cleanup. Use attributes for the demonstrated resource contract only.
- Scope removal of explicit dependencies to edges replaced by artifact input
  wiring or obsolete work; this card does not prohibit legitimate orchestration
  dependencies elsewhere.
- Reuse existing Gradle tasks and normal delivery evidence. Any one-off probe
  remains temporary, with findings retained here; new maintained tooling requires
  the recurring need and owner described in the playbook.

## Safe slice sequence

### 1. Establish the resource contract before implementation

The active contributor traces SDK resource generation and each consumer's final
assembly path using source, existing task reports, and bounded output inspection.
For each consumer, record required files, their producer, their final destination,
and whether existing dependency handling already delivers them. Inspect a fresh
or otherwise controlled output state so stale files do not establish necessity
or successful delivery.

Stop the investigation when each copy has a supported disposition: retain and
rewire, remove as redundant, or a concrete unresolved delivery question. Do not
create a separate producer-only implementation commit. These findings select the
smallest useful implementation and are not a new permanent verification system.

### 2. Deliver the complete resource-wiring refactor

Default to one implementation slice covering all three consumers, with the same
contributor responsible for producer, consumers, necessary convention changes,
and canonical documentation. Its independent value is reliable required resource
delivery with obsolete copies and hardcoded SDK output paths removed.

If the established contracts justify multiple slices, revise this sequence before
implementation: the first slice must include a real consumer and any producer
contract it needs; later slices must each deliver an independently useful consumer
migration or removal. Each slice must preserve the unmigrated consumers and pass
`./gradlew check` before its tests and implementation are committed together.
Do not defer compatibility or configuration-cache validation to a final repair
phase.

Review test coverage separately from production/build design. Update the existing
cross-project-sharing guidance in the Gradle playbook in the same slice if the
implementation changes or clarifies the canonical pattern; do not duplicate it
here. No visible application change is intended, so visual evidence is not an
acceptance requirement for this refactor.

## Acceptance evidence

Record exact commands, outcomes, implementation commits, and concrete output
observations. Prefer existing test/build results; use bounded additional checks
only for gaps those results cannot establish.

| Outcome | Evidence and pass condition | Limits |
| --- | --- | --- |
| Required resources remain available | For each retained delivery path, run the existing consuming assembly/package task from a controlled output state and identify the required files in its final output. Compare contents and locations with the established contract. | A copy task succeeding or a staging directory existing does not establish final delivery. |
| Removed copies were unnecessary | Record why each removed copy has no required files or is covered by an existing path, plus successful affected assembly/runtime checks without that copy. | An empty incremental directory alone is insufficient. |
| Dependencies follow data flow | Invoke the consumer without manually requesting SDK resource production and verify required output is produced or restored before consumption. For necessary sharing, make one bounded resource-input change and verify it reaches the consumer; restore temporary changes. | A dependency report or dry run alone does not establish file propagation. Mark this check inapplicable when no shared resource contract remains. |
| Configuration cache supports the changed path | Run the same affected task invocation twice with configuration cache enabled; record successful entry storage and reuse, and inspect reported problems attributable to the changed wiring. | `org.gradle.configuration-cache.problems=warn` is currently configured, so exit status alone does not establish compatibility. Record unrelated pre-existing problems separately; do not claim reuse if it did not occur. |
| Existing behavior remains covered | Run focused affected checks, then `./gradlew check` before each implementation commit. Record failures and validation gaps accurately. | Full checks may not assemble every distributable; include the relevant existing packaging task above where needed. |
| Scope and ownership are clean | Review the diff for removal of the three hardcoded SDK output paths and obsolete edges/tasks, producer-owned outputs for retained sharing, and unnecessary duplicated build logic. | Source review complements execution evidence. |

Use `./gradlew :sdk:outgoingVariants` and consumer dependency reports only when
new artifact configurations exist and the reports answer a concrete resolution
question. No deployment is required for this build-only outcome. Run Gradle
outside the agent sandbox per the playbook; do not weaken tests or change AWS
profiles to obtain a green result.

## Progress and closeout

- Active contributor: Codex (single owner across all three consumers).
- Disposition: remove all three copies. The configured SDK main resource roots
  are only `src/commonMain/resources`, `src/webMain/resources`, and
  `src/jsMain/resources`; none contains files. A temporary init-script inspection
  found `jsProcessResources.source.files = []` and no producer dependencies.
  SDK Apollo generation produces Kotlin sources/data builders and metadata through
  Apollo dependencies, not processed JS resources. No conditional resource
  generation is configured in the SDK or its conventions.
- Client: `additionalResources` had no downstream file consumer (repository search
  and Vite configuration review). Client-owned resources reach the npm Kotlin
  directory and Vite production output through existing Kotlin/JS tasks.
- CLI: the removed task wrote to the same directory as `jsProcessResources`.
  There are no required SDK files; CLI-owned resources and compiled SDK code
  continue through the existing compilation/package pipeline into `jsCliTar`.
- E2E: the removed task similarly shared the resource-processing destination.
  Required `browser-diagnostics.mjs` is owned by E2E's diagnostics directory and
  remains an explicit input of `jsE2eTestProcessResources`. SDK code and GraphQL
  operations retain their Kotlin/JS and Apollo dependencies.
- Implementation: removed copies, hardcoded SDK output paths, and their obsolete
  orchestration edges. No SDK artifact or shared convention is needed for an
  empty resource contract. Clarified this decision rule in the Gradle playbook.
- Validation on 2026-11-23 (all Gradle commands outside the sandbox):
  - `./gradlew :sdk:clean :sdk:jsProcessResources --info --no-configuration-cache`
    passed; SDK processing reported `NO-SOURCE` from a clean SDK output state.
  - `./gradlew :sdk:jsProcessResources -I /tmp/cpl21-resources.init.gradle --no-configuration-cache --console=plain`
    passed and established the configured roots, empty input files and absent
    producer dependencies above. Initial probe failed on the included build;
    guarding absent SDK projects fixed the temporary probe. No probe is retained.
  - `./gradlew :client:clean :cli:clean :e2e:clean --console=plain` initially
    failed because new Apollo files appeared while deleting client output;
    repeating the same invocation passed, establishing controlled module outputs.
  - `./gradlew :client:jsBrowserProductionVite :cli:jsCliTar :e2e:compileE2eTestDevelopmentExecutableKotlinJs :e2e:jsE2eTestProcessResources --configuration-cache --console=plain`
    passed twice: first entry stored (260 tasks: 43 executed, 23 restored from
    cache), then entry reused (250 tasks: 22 executed). The stored configuration
    report contained 5,400 input diagnostics and zero problem entries; no
    configuration-cache problems were reported for this invocation.
  - Output inspection: `client/build/dist/js/production/index.html` and hashed
    assets exist; `client/build/additionalResources` is absent. CLI tarball
    `cli/build/distributions/coupling-cli-js.tgz` has 2,236 entries, including
    `kotlin/bin/coupling` and `kotlin/Coupling-cli.mjs`. E2E diagnostics exist in
    `e2e/build/processedResources/js/e2eTest/browser-diagnostics.mjs` and the
    executable npm project `build/js/packages/Coupling-e2e-e2eTest/kotlin/`.
    SDK processed resources remain absent. Cache restoration is normal delivery
    evidence; assemblies did not depend on stale module outputs.
  - Resource mutation/propagation check: inapplicable, since no shared resource
    contract remains. No claim is made about hypothetical future SDK resources.
  - Full `./gradlew check --console=plain` passed in 55 seconds: 694 tasks
    (78 executed, 39 from cache, 577 up-to-date), including E2E execution.
- Separate reviews: test coverage remains existing application/runtime coverage;
  no artificial failing tests were added for obsolete wiring removal. Build design
  review confirms single ownership of processing destinations, unchanged real
  dependency paths, no new configurations or duplicated logic, and removal of all
  three consumer references to SDK build internals. `git diff --check` passed.
- Implementation commit: pending full validation.

When all dispositions are implemented and acceptance evidence is complete, move
this card to `agents.d/work_completed/` with its existing ID and filename and
update inbound links. Push and release remain subject to user authorization.
