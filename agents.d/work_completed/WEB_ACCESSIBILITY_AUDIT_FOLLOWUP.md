# Extended Web Accessibility Audit

## Outcome

Completed 2026-11-19 following the owner's manual walkthrough and request to
close this card. This closes the bounded inventory, shared accessibility fixes,
and requested UI corrections. It is not a full-route accessibility certification.
The original broad manual matrix is explicitly deferred to the follow-ups below;
no unrecorded screen-reader, contrast or forced-colors result is inferred.

Follow-up to [web remediation](WEB_BEST_PRACTICES_REVIEW_REMEDIATION.md) and
[keyboard UX refinement](WEB_KEYBOARD_UX_REFINEMENT.md).

## Completed Scope

- [x] Inventory route/control ownership and distinguish source findings from
  component, browser, native keyboard and user-review evidence.
- [x] Deliver independently tested fixes for Demo focus containment, roster
  names, configured badge names, important field instructions and visible focus.
- [x] Resolve owner-reported About/header/news presentation and reflow issues,
  preserve established control placement, and remove unsaved-card navigation.
- [x] Review test placement, consolidate redundant coverage, perform the bounded
  walkthrough, and dispose of residual audit work through explicit follow-ups.

## Implementation and Validation

| Boundary | Result and commits | Evidence |
| --- | --- | --- |
| Demo illustration | Simulated forms inert; introduction and real return links usable (`b9bbef6b4`) | Real-Chrome DOM focus regressions, including final return |
| Player portraits and badges | Decorative portrait alternatives; configured native option names (`bac169bbb`, `08ee09bbf`) | Rendered accessible names; saved selection E2E retained |
| Form instructions | Email access, permanent party URL, pin identifier and badge assignment hints associated with fields (`650d8df3a`, `a07b7b4c4`, `264fc315a`, `3264ba154`) | Component/jsdom name, description and configured-value assertions |
| Test consolidation | Rendering and Close contracts moved to components/jsdom; redundant badge E2E removed (`2707821aa`, `0381d1e9f`) | Preserved assertions and assembled save/reload coverage; selected value checked |
| Established placement | Copy right rail and inline Party List title/buttons restored (`f38844854`) | Existing keyboard order retained; owner reviewed visuals |
| About and news | One centered peer title/Back group; shrinkable About frame; inset native news scrolling, long-text wrapping and Close hover feedback (`e7f6b0734`, `e9e64d0cf`, `709b2b30b`, `0e197a6ce`) | Chrome geometry regressions for header, 320px About overflow and dialog final-line reachability |
| Shared focus | Removed three outline suppression overrides (`6ffe87731`) | Three failing-then-passing Chrome computed-style regressions |
| Unsaved party previews | Both new-party cards static; saved cards retain links (`421fe94ca`) | Regression failed on link ancestor; jsdom checks both cards and saved destinations; fresh Chrome visual/AX review |

Implementation slices ran focused checks, formatting and full `./gradlew check`
before commit. Latest full validation at `421fe94ca` passed via
`fish -lc './gradlew check --no-daemon'`: 680 tasks. Latest focused components run:
128 tests passing. Last full-run log: `/tmp/coupling-static-card-check.log`.
Test review checked owner/runtime fit and retained behavioral assertions;
production review checked shared ownership, unchanged saved behavior and scoped
styles. No push or release performed.

## Manual Evidence and Owner Acceptance

Native Chrome fresh-build review confirmed About wraps within the window at 200%
zoom; Tab reaches Back and Enter returns to Party List. Recent information scrolls
to the final “– RoB” at 200%; Escape closes it and returns focus to its info trigger.
Zoom restored to 100%. The original window-specific clipping report was not
independently reproduced; the current tested conditions pass and owner accepted
the walkthrough. Both new-party previews retain their placement and render as
static content after the reported accidental-navigation fix.

Owner reported player configuration tabbing good, identified the unsaved-card
regression, then confirmed “everything else seems good” and requested closure.
Browser/OS versions, VoiceOver calibration and per-route spoken results were not
provided. Record this as owner acceptance of the walkthrough, not a calibrated
Safari/VoiceOver matrix pass. The [manual checklist](../research/WEB_ACCESSIBILITY_MANUAL_REVIEW.md)
retains the unverified conditions for future use.

## Residual Work and Explicit Disposition

The owner authorized follow-up cards to bound this effort and accepted closure.
The following work remains outside this completed card:

- [Pairing keyboard operations](../work/WEB_PAIRING_KEYBOARD_FOLLOWUP.md):
  inventory and reproduce keyboard alternatives to player/pin drag operations,
  including populated prepare/current/history flows and announcements.
- [Contributor popup](../work/WEB_CONTRIBUTOR_POPOVER_FOLLOWUP.md): test the real
  participant trigger and agree keyboard/focus/dismissal semantics.
- [Data, reflow and contrast](../work/WEB_ACCESSIBLE_DATA_AND_REFLOW_FOLLOWUP.md):
  semantic numeric data, chart/library runtime behavior, source contrast findings,
  shared form/Party List reflow (horizontal overflow observed at 200%), document
  titles/heading outline and query-error recovery. This card also owns disposition
  of remaining general manual-matrix conditions: populated saved routes,
  integration/status review without real connections, calibrated AT, runtime
  contrast and forced-colors checks. Scope those into separate deliverables before
  playing; do not silently treat this closure as passing evidence.

Completed About reflow and unsaved-card fixes remain here; they are not deferred.
Other new-party navigation controls still use party routes and should be evaluated
with the follow-up state/recovery work; this slice removed only card navigation.

## Final State

Implementation complete through `421fe94ca`; this closeout changes documentation
only. The existing passing full suite applies to unchanged executable sources;
no artificial failing tests or redundant full-suite run for documentation.
Testing-guidance lessons requested after completion follow as a separate docs
commit. No implementation work remains in this card.
