# Testing

Load before changing behavior or tests. The contribution sequence and commit gate
are defined in [AGENTS.md](../../AGENTS.md).

## Tests Express Intent

Declare the behavior and observable contract before writing a focused scenario.
Confirm it fails for missing behavior, not syntax or setup. Implement the smallest
solution, refactor with tests green, and commit tests with implementation.

Test at the behavior’s owning boundary. Anchor confidence at the highest affected
level, with variations below it. Preserve existing constraints; do not invent
failing tests for documentation, mechanical edits, or behavior-preserving refactors.

### Resuming an Inherited Implementation

Inspect prior commits, tests, and red/green evidence before choosing the next test.
Reuse evidence that still applies to the current contract and source; record its
provenance and gaps. Do not remove working code merely to replay completed TDD.

Test uncovered contracts at their owning boundary. When test sensitivity is
uncertain, use a small reversible fault grounded in a plausible regression, confirm
the intended assertion fails, then restore and rerun. Avoid repeated fault checks
that add no evidence. Prior results do not replace the current full-suite gate.

## Choosing a Test Boundary

Choose the narrowest boundary that observes the behavior; add broader coverage
when the contract crosses it:

- **Unit:** in-process logic, data rules, adapters, or actions at their local
  boundary, with collaborator doubles where useful. Put platform-specific behavior
  in the matching source set.
- **UI/component:** rendered content, input, dispatched actions, and local navigation
  with stubbed dispatchers. A memory router can prove destinations, disclosure,
  and focus without services.
- **Endpoint/API:** SDK calls against the running service for network contracts
  and service-dependent behavior, without a browser.
- **End-to-end/system:** browser flows through the assembled client and server for
  route/auth/service wiring. Keep diagnostic harness tests separate from product flows.

Choose by the capabilities the assertion needs, not the suite’s label.

### Choose the UI Suite Before Writing the Test

In the delivery shot, name the owner, required runtime capabilities, and existing
suite. Read tests beside the owner before choosing an application/browser suite.
Accessibility or React Router involvement alone does not require Chrome or E2E.

- Start component-owned rendering and interaction contracts in
  `client/components/src/jsTest` (`:client:components:jsTest`, Node with jsdom).
  Accessible names/descriptions, labels, selected values, dispatched actions, and
  memory-router navigation can be observed here without running the application
  or services. Reuse its router/provider setup; keep application-owned contracts
  with their owner rather than moving production code merely to fit this suite.
- Use real-browser component tests (`:client:jsBrowserTest`) when the assertion
  needs browser behavior that jsdom cannot faithfully provide: geometry/reflow,
  computed visual styles, native dialog modality, or inert/focus enforcement.
  Identify that dependency explicitly; a DOM query or simulated user-event Tab
  sequence alone is not a reason to require a real browser.
- Use E2E for assembled route/auth/service contracts, such as saving a selection
  and loading it again. Keep rendering and input variations at the component
  level instead of repeating full authenticated flows for each variation.

When adding lower-level coverage, consolidate redundant broader checks while
preserving distinct integration evidence. Assert actual selection/state when that
is the claim; an option’s existence does not prove it is selected.

Find reusable patterns with `rg --files`. Verify behavior, not source text or
plumbing: assert a rendered value rather than a query’s field name. Group related
observations; keep distinct scenarios explicit. Avoid imagined edge-case matrices
or passing tests with no additional intent.

## Browser Interaction Evidence

For navigation/accessibility changes, inspect the rendered interactive structure
as well as the accessible name and destination. Native links and buttons already
participate in Tab order; changing `tabindex` alone does not remove a nested control
or prove there is one focus stop per action. Exercise the claimed focus or keyboard
regression at the owning component/router boundary.

Distinguish simulated user-event keyboard tests, native browser key input, and
calibrated screen-reader checks when reporting evidence. Running a component test
in a browser does not turn simulated Tab into native keyboard evidence. Likewise,
mocked media preferences prove application response to the preference contract,
not OS setting integration. Confirm visible rendering as well as final content
when bypassing animation; omitted animation events must not strand controls.

Treat missing browser APIs/providers, module-export mismatches, asset placeholders,
and fixture property shadowing as harness problems until the intended assertion
fails on the product behavior. Reuse real providers and image registries where
needed; placeholder image URLs do not establish successful loading or layout
stability. Keep test-only API fixtures in the harness, with the used event/state
contract, rather than weakening production behavior to accommodate their omissions.

### Browser Performance Evidence

Keep metric aggregation/privacy contracts in Node tests and platform delivery in
browser measurements. Label lab observations separately from field Web Vitals;
missing LCP is not zero, and cross-origin Resource Timing sizes may be hidden by
Timing-Allow-Origin. Report pending, unknown, truncated and failed/aborted samples
explicitly and retain anomalous runs. A completed measurement sequence need not
be error-free. Establish fixture, cache/content conditions and repeated-run
variation before proposing timing budgets or making before/after speed claims.

### Exercise the Production Composition

For trigger, focus and navigation regressions, render the actual owning
composition. A popup exercised through a test-created button proves the popup's
contract, but not that its production trigger is keyboard accessible. Likewise,
a shared card's link test does not prove its use as an unsaved form preview is
safe. Include the relevant new/saved state and inspect ancestors and descendants
for unintended interactive targets; a leaf's tabindex alone is insufficient.
Keep detailed child behavior in child tests and add only the composition evidence
needed to catch the real failure.

For geometry/reflow tests, establish a named, test-owned viewport/container and
assert against that boundary. Avoid locating it through a chain of production
parents. Check the region that must remain reachable, including inner scroll
content when appropriate: a bounded outer shell can conceal overflow inside it.
Use content representative of the reported failure rather than asserting exact
pixel details that are incidental to the intended layout.

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

Keep scenarios readable at the call site. Prefer explicit data and some duplication
to helpers that obscure intent; production function-size and deduplication rules
do not transfer mechanically to tests. Use immutable data where helpful without
hiding arrangement or observations in transformation pipelines. Follow local
formatting, breaking chains before `?.` and assertions.

When adding an alternative API, cover both supported paths. Preserve legacy
behavioral assertions through migrations; do not weaken a test merely to make the
implementation pass. If a test reveals an incorrect contract, revise the declared
intent explicitly before changing it.

## Local Integration Environment

Gradle-run SDK integration tests (`:sdk:jsNodeTest` and `:sdk:jvmTest`) read
Standard SecureString parameters `/local/COUPLING_PRIMARY_TEST_PASSWORD`,
`/local/COUPLING_ALT_TEST_PASSWORD`, and `/local/COUPLING_E2E_TEST_PASSWORD`
from AWS SSM using the configured AWS identity. The primary, alternate, and E2E
accounts are distinct. Browser/E2E tasks use only the E2E parameter.

Passwords are fetched when a consuming test task executes and injected through
its existing environment variable names; inherited password values are overridden.
Task discovery and unrelated tasks do not fetch these SDK passwords. Run
`./gradlew check` from an ordinary shell; no fish password exports are required.
Keep existing Docker, local certificate, and integration-stack prerequisites.

Local and CI identities need `ssm:GetParameters` on the named parameters and
decryption access to their encryption key. Missing/blank parameters fail with the
parameter name; AWS lookup failures ask you to check credentials and access.
Follow the [AWS diagnosis](GRADLE_PLAYBOOK.md#aws-sso-and-gradle-daemons) before
changing profiles or authentication behavior. If lookup succeeds but SDK tests
receive HTTP 401, verify that the named parameter belongs to the correct test
account. Do not print passwords, copy them into files, or substitute the E2E
password for a primary/alternate password. Test-account passwords are separate
from AWS credentials.

## Browser Diagnostics

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
Run browser-bearing tasks outside the sandbox with a daemon started outside it,
following the Gradle playbook. Capture ownership, limits, and redaction live in
`e2e/diagnostics/browser-diagnostics.mjs`.

The fixture owns ChromeDriver before requesting a session and stops it even when
session creation fails; the original startup error remains the test failure.

Use Gradle's `--tests` option for focused E2E scenarios, matching the TestMints
test method name, for example
`./gradlew :e2e:e2eRun --tests 'canSaveEditsCorrectly'`. The E2E runner maps this
filter to Mocha's substring filter; class-qualified Gradle patterns can select no
TestMints scenarios because the runner's displayed test title is the method name.
Confirm the focused report contains the intended scenario, then omit the filter
for the full-suite gate.

### Chrome for Testing startup failures

If `:e2e:jsNodeDiagnosticsBrowserTest` or `:e2e:e2eRun` reports `tab crashed` or
`session not created` on `POST /session`, first confirm Gradle ran outside the
agent sandbox and used a daemon started outside it, following the Gradle playbook.
On macOS, look for matching
`Google Chrome for Testing*.ips` reports in `~/Library/Logs/DiagnosticReports/`.
The WebDriver error or test output gives the browser version. For the diagnostic
fixture, the XML report is under `e2e/build/test-results/jsNodeDiagnosticsBrowserTest/`.
A crash during session creation can happen even when the cached executable
prints its version.

After the tests stop, list the WebdriverIO browser cache and move aside just
the Chrome for Testing directory for the platform and version that crashed.
Run this from a POSIX shell and replace the example directory name with the
one from your cache.

```sh
cache_root="${WEBDRIVER_CACHE_DIR:-${TMPDIR:-/tmp}}"
ls "$cache_root/chrome"
entry="$cache_root/chrome/mac_arm-157.0.8103.38"
backup=$(mktemp -d "$cache_root/chrome-recovery.XXXXXX") &&
  mv "$entry" "$backup/" &&
  printf 'Saved the old download in %s\n' "$backup"
```

With `WDIO_CHROME_BINARY` unset, force the browser test task to run so
WebdriverIO downloads Chrome again. This task starts its own local fixture
and can check browser startup without bringing up the application stack.

```sh
fish -lc 'env -u WDIO_CHROME_BINARY ./gradlew :e2e:jsNodeDiagnosticsBrowserTest --rerun'
```

Once the focused check passes, retry the original build command.

If the fresh download still crashes, compare with installed Chrome. Both
browser runners honor `WDIO_CHROME_BINARY`.

```sh
fish -lc 'env WDIO_CHROME_BINARY="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" ./gradlew :e2e:jsNodeDiagnosticsBrowserTest --rerun'
```

## Focused Contexts

Use the relevant tests and fixtures, not a wholesale dump of test suites or logs.
When delegating test investigation or review, pass a compact behavior/design brief,
relevant paths, constraints, and expected deliverables. An implementer must still
read the tests it needs to satisfy. Review tests for intent and production code for
design; neither review substitutes for the other.
