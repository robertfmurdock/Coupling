# E2E Password from AWS SSM

## Goal and Constraints
Load the browser test password from `/local/COUPLING_E2E_TEST_PASSWORD`, stored
as an AWS SSM SecureString using the configured AWS identity. Follow the existing
Gradle ValueSource pattern; retain an explicitly exported environment override.
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
- An exported `COUPLING_E2E_TEST_PASSWORD` takes precedence over SSM.
- Missing or blank SSM credentials must report the parameter name without values.
- Update the credential instructions in `agents.d/context/TESTING.md` with the
  implemented loading behavior.

## Current State
2026-10-30: current commit aa869313a. No implementation edits for this slice yet.
The owner is running full check for the preceding ledger-schema slice. Its
changes remain uncommitted; see `TARGET_LEDGER_SCHEMA_COMPATIBILITY.md`.
SSM metadata inspected using the configured identity: no E2E password parameter
exists. Asked the owner for a secure source for its current value, not the value
in chat. Do not substitute the SDK accounts' passwords.

## Validation
Pending. Relevant files: `coupling-plugins/src/main/kotlin/com/zegreatrob/coupling/plugins/util/AwsSsmHelpers.kt`,
its test in `coupling-plugins/src/test/kotlin/`, and `e2e/build.gradle.kts`.
