# Testing

Load before changing behavior or tests. The contribution sequence and commit gate
are defined in [AGENTS.md](../../AGENTS.md).

## Tests Express Intent

Declare the intended behavior and observable contract before writing the test.
Write one focused scenario, run it, and confirm it fails for the missing behavior
rather than an unrelated syntax/setup problem. Implement the simplest solution,
then refactor with tests green. Keep tests and implementation in the same safe commit.

A test describes a property of the system or component at its own boundary. Match
its language to that boundary. Anchor confidence at the highest affected level,
with variations at lower levels. Preserve existing behavioral constraints during
refactoring; do not add token tests for mechanical edits or documentation.

Search for existing module patterns and utilities with `rg --files` and reuse them
when they improve clarity. Verify the feature, not source text, types, or plumbing:
for example, test that a component renders a value rather than that a query contains
its field name. Related observations can share a test; distinct scenarios should
remain explicit. Avoid exhaustive imagined edge cases or passing tests with no
additional intent.

## TestMints Phase Discipline

- Use the module's existing TestMints patterns and `assertIsEqualTo` assertions.
- `setup` arranges scenario-specific data, collaborators, and the world enabling
  the behavior. Prerequisite actions belong here; do not hide the behavior being
  tested in setup.
- `exercise` contains the externally meaningful trigger, or the deliberate sequence
  that is itself the contract.
- `verify` contains assertions. Carry the exercise result and required observations
  forward explicitly.
- Name the bounded behavior under test. Split scenarios with different subjects
  instead of hiding them in a generic factory.
- Prefer equality assertions that explain the expected result over symbolic
  presence/type checks that do not establish behavior.

## Test Design Is Different from Production Design

Keep scenarios readable at the call site. Explicit data and some duplication can
communicate intent better than a shared abstraction. Introduce narrowly scoped
helpers only when they make the scenarios easier to understand; do not impose
production function-size or deduplication preferences mechanically on tests.

Use immutable data where it helps, but make scenario arrangement and observation
clear rather than disguising them as production transformation pipelines. Follow
local formatting, including breaking assertion chains before `?.` and assertions.

When adding an alternative API, cover both supported paths. Preserve legacy
behavioral assertions through migrations; do not weaken a test merely to make the
implementation pass. If a test reveals an incorrect contract, revise the declared
intent explicitly before changing it.

## Local Integration Environment

SDK integration tests authenticate test users using
`COUPLING_PRIMARY_TEST_PASSWORD` and `COUPLING_ALT_TEST_PASSWORD`; browser/e2e
login uses `COUPLING_E2E_TEST_PASSWORD`. These are separate from AWS credentials.
Use the configured shell/credential loader to export them to Gradle. Check presence
without printing values; do not copy secrets into the repository. If AWS works but
SDK tests receive HTTP 401, inspect this environment before changing auth behavior.

## Focused Contexts

Use the relevant tests and fixtures, not a wholesale dump of test suites or logs.
When delegating test investigation or review, pass a compact behavior/design brief,
relevant paths, constraints, and expected deliverables. An implementer must still
read the tests it needs to satisfy. Review tests for intent and production code for
design; neither review substitutes for the other.
