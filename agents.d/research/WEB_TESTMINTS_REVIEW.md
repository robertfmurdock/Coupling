# Recent web accessibility test review

## Scope

Reviewed accessibility work from native contributor triggers (`76d930633`) through
shared heading semantics (`650fcb848`) using the installed Testmints skill and
`agents.d/context/TESTING.md`. Documentation-only CSP work adds no tests. This is
a test-design review; it does not replace the completed native/owner evidence or
claim a new accessibility audit.

## Assessment

| Tests/support | Decision |
| --- | --- |
| ContributionCardHeaderTest | Retain real-header keyboard activation, static no-handler behavior and real header/popup anchor composition. These catch different regressions. |
| ContributionPopUpMenuTest | Retain six popup contracts. Extract repeated trigger markup into a named fixture with a stable key; keep opening, refresh, dismissal and Tab actions visible in phases. Replace async find queries for settled content with immediate get queries. |
| ContributionOverviewContentTest | Retain both actual UpdatingPlayerList/overview compositions: save completion returning focus, and late completion respecting prior Tab exit. Name the repeated unknown-contributor render fixture. Start the pending save in setup so exercise visibly delivers the response in both cases. |
| GeneralControlBarTest | Keep semantic/name assertions in components/jsdom. The single rendering scenario is clear without a new abstraction. |
| PageLayoutTest | Keep geometry, reflow and computed focus style in real Chrome. The contributor geometry comparison adds evidence absent from jsdom activation tests. Preserve the existing About, header and copy-control layout regressions. |
| RouteTitleTest | Keep actual application route labels and title effect with inert query bodies; consolidate route/title expectations into one explicit table, name query-free route construction, and dispose the memory router in teardown. The application suite currently uses Chrome; adding another runtime for one tiny test is not justified by measured cost. |
| KeyboardUserEvent, MemoryRouter, NavigationTestingLibrary | Keep narrow typed bindings matching each module's existing import/runtime style. No production API or dependency changes needed. |

No duplicate scenario warrants deletion. In particular, artificial-trigger popup
unit tests cannot replace production-trigger coverage, and component save-focus
checks cannot replace distinct persistence/E2E contracts. No tests moved to E2E,
no arbitrary sleeps/timeouts added, and no production code changed. Test count
remains unchanged. The refactor removes 74 net lines across three test files.

## Validation

- Components: 137 tests and formatting pass.
- Application browser suite: 51 tests pass.
- Saved Gradle XML popup-suite time before: 1.008s for six scenarios (Tab-exit
  scenario 1.003s). Focused refactor run: 0.005s for the same six scenarios. These
  are individual report observations, not controlled benchmark or full-build gains.
- Full `fish -lc './gradlew check --no-daemon'` passes (680 tasks); log
  `/tmp/coupling-testmints-review-check.log`. Diff checked. No push/release performed.

This is a behavior-preserving refactor with existing regression contracts, so no
artificial red phase was introduced. Original regressions were confirmed red in
their implementation slices. Review retained each meaningful assertion and its
actual production composition where required.
