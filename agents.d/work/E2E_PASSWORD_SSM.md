# E2E Password from AWS SSM

## Goal and Constraints
Load the browser test password from `/local/COUPLING_E2E_TEST_PASSWORD`, stored
as an AWS SSM SecureString using the configured AWS identity. Follow the existing
Gradle ValueSource pattern. SSM is the sole source for Gradle-run E2E tests.
Never print the password or place it in source. Do not interrupt the owner's
current full-check run.

## Safe Slice Sequence
- [ ] After the ledger-schema slice passes full check and is committed, add
      lazy SSM password loading and wire it to `:e2e:e2eRun`. Test parameter
      decoding and missing-value diagnostics, expecting today's implementation
      to lack this capability. Build logic belongs in `coupling-plugins/`;
      consumer wiring belongs in `e2e/build.gradle.kts`. Keep the optional E2E
      secret independent of the required Docker provisioning parameters so
      unrelated tasks do not acquire a new credential requirement.
      Provision the parameter from the owner's secure source before validating
      the fallback. This is independently useful for every future full check.

## Design Decisions
- Use SSM SecureString under `/local/`, matching local build credentials.
- After successful SSM authentication, the owner requested removal of the
  environment override. Gradle always supplies the SSM password to its child
  test process, overriding any inherited E2E password.
- Missing or blank SSM credentials must report the parameter name without values.
- Update the credential instructions in `agents.d/context/TESTING.md` with the
  implemented loading behavior.

## Current State
2026-10-30: base commit f8d0cd1bd; preceding ledger slice committed by owner.
Owner created the SSM parameter and requested build wiring. Implemented a
single-parameter ValueSource with shared response parsing; e2eRun resolves SSM
lazily in its existing execution action. Environment fallback removed at owner's
request after validating the SSM path.
No AWS writes or SDK password substitutions. Browser run now authenticates using
SSM. Full check is blocked by separate SDK-account authentication failures, even
from Fish with nonempty exported credentials and a fresh configuration. Keep the
slice uncommitted until the required full check passes.

## Validation
- New helper tests first failed compilation because the single-parameter API was
  absent. Prior full check also established the real E2E missing-password failure.
- `./gradlew -p coupling-plugins test --tests '*AwsSsmHelpersTest' formatKotlin`
  passed all four parser tests. Reverted formatter changes to unrelated files.
- `./gradlew :e2e:e2eRun` outside the sandbox completed using SSM: 55 tests pass,
  one fails (`PartyListPageE2ETest.canNavigateToTheNewPartyPage`, timeout finding
  "Party List"). Browser remained running through the suite. Log:
  `/tmp/coupling-ssm-e2e-unsandboxed.log`; detail:
  `e2e/build/reports/logs/e2e/run.log`.
- Full check and commit pending.
- Final configuration after removing the override passes:
  `./gradlew :e2e:e2eRun --dry-run`. The SSM execution path is the same one
  exercised by the completed browser run. `git diff --check` passes.
- `./gradlew -p coupling-plugins check` fails lint in unchanged deployment sources
  and tests (five files). No lint violations reported in the modified helper files.
  Log: `/tmp/coupling-ssm-plugin-check.log`. Preserve this pre-existing validation gap.
- Test review: fixtures check named selection, missing and blank secrets, and
  existing multi-parameter decoding. Production review: lookup occurs in task
  execution, and SSM errors name the parameter
  without including its value. Existing Docker parameter requirements unchanged.
- All build-plugin tests passed: `./gradlew -p coupling-plugins test`.
- First E2E attempt completed SSM lookup but Chrome exited before tests began
  under the sandbox. Restarted Gradle outside the sandbox per the playbook.
- First full check outside the sandbox failed SDK authentication because the
  calling zsh environment lacks SDK test passwords. Both are present in the
  owner's existing Fish environment. Full check reran from Fish; E2E still
  uses its distinct SSM password. No credential values printed.
- Fresh `./gradlew check --no-configuration-cache` from Fish also fails at
  `:sdk:jsNodeTest` with authentication failures. No further SDK changes in scope.
  Logs: `/tmp/coupling-ssm-full-check-fish.log`,
  `/tmp/coupling-ssm-full-check-fresh.log`.
