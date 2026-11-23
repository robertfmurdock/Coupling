# CPL-65 — Load SDK test passwords from AWS SSM

**Work card protocol: 2**

## Goal and scope

Make Gradle-run SDK integration tests use the same AWS SSM credential-loading
system as browser E2E tests, removing the local dependency on fish exporting SDK
passwords. Both Kotlin/JS Node and JVM SDK suites must work from an ordinary shell
with the configured AWS identity and normal integration-stack prerequisites.

The primary, alternate, and browser E2E accounts remain distinct. Proposed
Standard SecureString names, following the existing `/local/` convention:

- `/local/COUPLING_PRIMARY_TEST_PASSWORD`
- `/local/COUPLING_ALT_TEST_PASSWORD`

Reuse `fetchAwsSsmParameter` and the existing execution-time E2E wiring pattern.
Prefer Kotlin/Gradle and existing libraries; do not add a credential loader,
shell wrapper, or parallel secret-management abstraction. Preserve the test
process environment variable names so SDK test authentication helpers need no
source change. SSM should be the sole source for these Gradle-run tests, overriding
inherited password values as the existing E2E task does.

Do not rotate account passwords, substitute the E2E password for another account,
change Auth0 behavior, modify AWS profiles, or expand this into unrelated CI secret
cleanup. Parameter provisioning and required CI access must be coordinated with
the owner before switching consumers; this card authorizes planning, not cloud
credential writes or password disclosure.

## Motivation and current evidence

During [CPL-64](../work_completed/CPL-64-VISUAL_EVIDENCE_CAPTURE.md), full `check`
from zsh failed SDK authentication while browser capture authenticated successfully.
Presence-only inspection found the SDK passwords in fish, and the same full gate
passed through `fish -lc './gradlew check --no-daemon'`. SDK passwords currently
come from shell exports locally and GitHub Actions secrets in CI; the separate
E2E password already comes from SSM.

[CPL-45](../work_completed/CPL-45-E2E_PASSWORD_SSM.md) describes the earlier E2E migration. Its status
is historical; inspect current code and [Testing](../context/TESTING.md#local-integration-environment)
for the implemented behavior rather than replaying that work.

## Ownership and discovery

- `sdk/build.gradle.kts`: supply primary/alternate passwords to `jsNodeTest` and
  `jvmTest` at execution time, alongside the existing E2E-account connection tests.
- `sdk/src/commonTest/kotlin/com/zegreatrob/coupling/sdk/AuthorizedSdk.kt`: inspect
  account identities and environment consumption; preserve those contracts.
- `coupling-plugins/src/main/kotlin/com/zegreatrob/coupling/plugins/util/AwsSsmHelpers.kt`
  and its tests: reuse lazy named-parameter lookup, parsing, and diagnostics.
- `.github/workflows/main.yml` and the configured CI AWS role/IAM definitions:
  establish parameter availability/decryption access, then remove the redundant
  primary/alternate GitHub-secret environment injection in the same rollout.
  Discover the actual role owner before proposing any IAM change.
- `agents.d/context/TESTING.md`: replace fish-specific SDK-password instructions
  with the implemented SSM path, failure diagnosis, and local/CI prerequisites.

## Acceptance evidence

- Node and JVM SDK integration suites authenticate both existing accounts with
  primary/alternate password exports absent. An ordinary-shell full check passes
  without invoking fish or sourcing a custom password loader.
- Deliberately incorrect inherited SDK password values cannot override SSM.
- SDK password lookup is deferred until a consuming test task executes. Task
  discovery and unrelated tasks introduce no additional SDK-secret lookup;
  existing Docker/SSM requirements are not misreported as new regressions.
- Missing/blank parameters, access denial, and expired AWS credentials produce
  actionable diagnostics without printing secret values or auth payloads. Do not
  create cleartext password files, task inputs, or custom report fields.
- Configuration-cache reuse works with execution-time injection. Preserve existing
  test cache policy; inspect credential/task-environment behavior rather than
  disabling caching to avoid understanding it.
- CI uses the intended SSM parameters and configured AWS role, with the old SDK
  password environment injection removed only once the SSM path is ready. Do not
  delete remote GitHub secrets as an incidental cleanup step.
- Existing E2E/capture password loading and test-account isolation stay intact.

## Safe implementation slice

Deliver one complete test-plus-implementation slice covering SDK task wiring,
required CI changes, and canonical usage docs. Before tests, declare the credential
injection boundary and expected failure: current SDK tasks do not supply primary
or alternate passwords from SSM when inherited variables are missing or wrong.

Test the task-execution/injection behavior using the established Gradle build-test
patterns and controlled, non-secret lookup values. Reuse existing parser coverage;
do not merely duplicate its passing tests or assert build-script source text.
Cover lazy resolution, both account mappings for Node/JVM, override precedence,
and safe failure at the owning boundary. Use the Testmints skill for tests written
or reviewed in Testmints suites, keeping setup, exercise, verification, and cleanup
explicit. After meaningful red evidence, state the smallest implementation and
how SSM providers flow into child test environments before editing production code.

Provisioning/read access is a prerequisite for real integration evidence; never
fake a passing auth check. Run focused build-logic and SDK checks, verify
configuration-cache reuse, then `./gradlew check --no-daemon` from an ordinary
shell with the two inherited SDK password variables unset. Follow the
[Gradle playbook](../context/GRADLE_PLAYBOOK.md) for browser execution and AWS/daemon
diagnosis. Review tests and production wiring separately before committing.

## Status

Complete locally. The owner provisioned both SDK parameters on 2026-11-23 as
SecureStrings using the default SSM key. See completion evidence below. No agent
cloud writes, IAM changes, push, or release have been performed.

## Implementation evidence — 2026-11-23

- Confirmed the original failure at the actual SDK task boundary with a temporary
  Gradle init-script probe: `jsNodeTest does not inject
  COUPLING_PRIMARY_TEST_PASSWORD`. The probe executed the existing credential
  action without running integration tests or displaying credentials. Its first
  attempt needed an included-build guard; the corrected attempt reached the
  intended missing-injection failure.
- Added `useSdkTestPasswords` in the existing build-logic utility boundary. Both
  SDK task types now reuse one account-to-SSM mapping and the existing named
  parameter provider; each value resolves in `doFirst`, preserving test cache
  policy and avoiding password task inputs added by this change.
- Controlled `SdkTestPasswordsTest` scenarios pass for actual Gradle JVM and
  Kotlin/JS task types: no lookup during configuration, distinct primary/alternate/
  E2E mappings, override precedence, and lookup failure before test execution.
  Existing SSM parser scenarios retain missing/blank parameter coverage.
- Read-only AWS inspection found only the E2E parameter initially. The owner chose
  to provision the two SDK parameters personally; no cloud writes were performed.
  CI roles are `LocalDevelopment` (PR) and `CouplingDeploy` (master).
  `LocalDevelopment` attaches `LocalParameterRead` v4 granting `ssm:GetParameters`
  on `arn:aws:ssm:*:174159267544:parameter/local/*`; `CouplingDeploy` attaches
  `AmazonSSMReadOnlyAccess`. Proposed parameters use the default SSM key. No IAM
  change is indicated by this policy inspection; live CI execution is unverified.
- `./gradlew :coupling-plugins:test --tests '*SdkTestPasswordsTest' --console=plain`
  passed outside the sandbox (3 scenarios). Test compilation initially caught
  Kotlin DSL action syntax and deprecated eager task registration; both corrected.

`./gradlew :coupling-plugins:check --console=plain` passed, including all build-logic
tests, lint and plugin validation. This exposed existing style violations in five
deployment/dashboard files; Gradle formatting made only whitespace/import changes.
Formatting and checking were rerun sequentially before the passing result.

### Real integration and rollout evidence

- Owner confirmed provisioning; metadata inspection shows all three named
  parameters are SecureStrings. No password values were displayed or written to
  files. CI injection of only primary/alternate GitHub secrets has been removed;
  remote GitHub secrets and the existing E2E workflow entry remain untouched.
- `env -u COUPLING_PRIMARY_TEST_PASSWORD -u COUPLING_ALT_TEST_PASSWORD ./gradlew
  :sdk:jsNodeTest :sdk:jvmTest --console=plain` passed with actual task execution:
  133 Node and 132 JVM tests, zero failures/errors/skips. Configuration cache stored.
- A reuse probe after `:sdk:cleanJsNodeTest :sdk:cleanJvmTest` reported
  `Reusing configuration cache` but restored both test results `FROM-CACHE`.
  This proves compatible configuration/cache policy, not another authentication
  run. Task-specific `--rerun` validation follows to exercise the actions.
- Final build-logic check passed with configuration-cache reuse: 19 tests,
  zero failures/errors/skips. Test review made each injection trigger explicit;
  production review confirmed only provider maps are captured by execution actions,
  preserving account separation, deferred resolution, and existing SDK cache policy.

- Forced real execution with deliberately incorrect inherited SDK passwords passed
  for both suites (133 Node / 132 JVM; zero failures/errors/skips). The command was
  `env COUPLING_PRIMARY_TEST_PASSWORD=deliberately-incorrect
  COUPLING_ALT_TEST_PASSWORD=deliberately-incorrect ./gradlew :sdk:jsNodeTest
  --rerun :sdk:jvmTest --rerun --console=plain`.
- Repeating that exact forced command reported `Reusing configuration cache` and
  executed both test tasks again successfully (not `FROM-CACHE` or `UP-TO-DATE`).
  This establishes execution-time provider resolution after cache deserialization
  and override precedence with real authentication. No cache policy was disabled.

## Completion — 2026-11-23

- Full ordinary-shell gate passed outside the sandbox:
  `env -u COUPLING_PRIMARY_TEST_PASSWORD -u COUPLING_ALT_TEST_PASSWORD ./gradlew
  check --console=plain` — BUILD SUCCESSFUL in 3m 16s; 694 actionable tasks
  (351 executed, 2 from cache, 341 up-to-date). Includes real browser and E2E
  checks, structured-log validation and SDK checks. Separate forced SDK runs above
  establish real authentication and configuration-cache action execution.
- Canonical Testing and Gradle instructions now describe ordinary-shell SSM
  loading, account separation, required access, and failure diagnosis. The only
  workflow change removes the two SDK password environment entries; source review
  confirms both CI branches retain their existing AWS role setup before Gradle.
- Reviewed test intent separately from production data flow, then reviewed the
  entire slice. Five pre-existing deployment/dashboard files contain only formatter
  changes required for the build-logic lint gate. No application/authentication
  helper, password rotation, AWS profile, IAM policy, or test-cache policy changed.
- No visible UI change; no visual evidence issue is required. No inbound card
  links were found to update. Temporary probe code was not retained in the repo.
- Implementation and this completed card ship together in the commit titled
  `CPL-65: Load SDK test passwords from AWS SSM`; Git history identifies its SHA.
- Gap: live GitHub Actions execution has not been performed. Policy inspection and
  local real integration checks establish the available evidence; the workflow
  path awaits an authorized push. Remote GitHub secrets were not deleted.
