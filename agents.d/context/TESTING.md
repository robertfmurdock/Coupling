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
login uses `COUPLING_E2E_TEST_PASSWORD`. Gradle's `:e2e:e2eRun` reads the Standard
SecureString parameter
`/local/COUPLING_E2E_TEST_PASSWORD` from AWS SSM using the configured AWS identity.
The parameter is fetched only when the E2E task executes and supplied to the test
process; exported E2E password values are overridden. SDK passwords still
come from the configured shell/credential loader. These are separate from AWS
credentials. Check presence without printing values; do not copy secrets into the
repository. If AWS works but SDK tests receive HTTP 401, inspect this environment
before changing auth behavior.

## Browser Diagnostics

On this development host, SDK password variables are loaded by fish. Run local
validation through `fish -lc './gradlew check --no-daemon'`; CI supplies its own
secret environment. Do not request password values or bypass configured profiles.

The E2E runner installs browser capture before navigation. BiDi sessions collect
console/script events and failed-network/HTTP-error metadata; classic sessions
use browser-console polling. Browser WARN/ERROR entries fail bootstrap or the
associated application test, including errors reported by structured loggers.
There are no warning exemptions or blanket log purges. Genuine cancellation is
re-thrown and logged as cancellation, not an application error.

Failed tests write bounded, sanitized JSON evidence and best-effort page state
and screenshots under `e2e/build/reports/e2e/browser-diagnostics/`. Existing result
collection places these under
`build/test-output/e2e/results/e2e/browser-diagnostics/` for CI upload. An incomplete
bundle means the diagnostic deadline interrupted collection; counters disclose
truncated/dropped entries. Diagnostic failures must not replace the primary test
failure. Passing application tests do not take diagnostic snapshots/screenshots.

Harness verification tasks are `:e2e:browserDiagnosticsTest` (15 Kotlin/JS Node tests)
and `:e2e:browserDiagnosticsBrowserTest` (two real-Chrome Kotlin/JS tests). Both use
TestMints setup/exercise/verify/teardown and standard Gradle XML/HTML test reports.
`src/jsTest` and `src/jsDiagnosticsBrowserTest` are separate source sets and
compilations; no class/file-name filters select the browser tests. Shared test
bindings and fixtures live in `src/diagnosticsTestSupport`. The fast task never
launches a browser. The browser fixture deliberately
injects failures and writes its expected evidence under
`e2e/build/reports/browser-diagnostics-fixture/`, even when its assertions pass.
Run browser-bearing tasks outside the sandbox with `--no-daemon`, following the
Gradle playbook. Capture ownership, limits, and redaction live in
`e2e/diagnostics/browser-diagnostics.mjs`.

Both browser fixtures honor `WDIO_CHROME_BINARY`. If downloaded Chrome for Testing
crashes during session creation, use the installed Chrome for diagnosis, for example
`fish -lc 'env WDIO_CHROME_BINARY="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" ./gradlew :e2e:browserDiagnosticsBrowserTest --no-daemon'`.
The fixture owns ChromeDriver before requesting a session and stops it even when
session creation fails; the original startup error remains the test failure.

Use `FGREP` for focused E2E scenarios, for example
`fish -lc 'env FGREP=BrowserLogPolicyTest ./gradlew :e2e:e2eRun --no-daemon --rerun'`.
The filter is a declared task input and explicitly passed environment value, so
configuration/build-cache reuse cannot confuse focused and unfiltered results.
Clear the filter for the required full-suite gate; verify the report's test count.

## Focused Contexts

Use the relevant tests and fixtures, not a wholesale dump of test suites or logs.
When delegating test investigation or review, pass a compact behavior/design brief,
relevant paths, constraints, and expected deliverables. An implementer must still
read the tests it needs to satisfy. Review tests for intent and production code for
design; neither review substitutes for the other.
