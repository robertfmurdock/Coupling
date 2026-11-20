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

[CPL-45](CPL-45-E2E_PASSWORD_SSM.md) describes the earlier E2E migration. Its status
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

Planned. No implementation, AWS parameter writes, IAM changes, or CI changes have
been performed. Confirm parameter provisioning and role access before rollout.
