# CPL-51 — Outcome

## Goal and Constraints

Apply the Google `modern-web-guidance` review to the Coupling web client in
small, independently releasable slices. Improve third-party resource safety,
browser security posture, accessibility, and measured loading performance while
preserving the existing Kotlin/JS client behavior and supported browser
compatibility.

Scope revision (2026-11-17, user authorized): finish the bounded remediation
below. Measurement-driven performance work and the broader accessibility audit
have separate follow-up cards; their transfer is not a claim of completion.

Confirmed baseline finding: the deterministic modernization scan found one
missing SRI policy on the CodeMirror stylesheet in
`client/src/jsMain/resources/index.html`. The scan covers only a subset of the
available guidance; the remaining items below are review-backed areas requiring
implementation and/or runtime validation.

Constraints:

- Keep client/server and SDK boundaries unchanged unless evidence requires a
  cross-layer change.
- Do not remove analytics, Auth0, GraphiQL, or integration functionality as a
  shortcut; make their loading and security requirements explicit.
- Keep browser support compatible with the project's actual deployment policy;
  document that policy before adopting newly available browser APIs.
- Use the installed Testmints skill and the relevant module examples for every
  behavioral test created or materially changed on this card.
- Preserve Coupling's test and application architecture: client behavior belongs
  in `client/`; server and GraphQL contract behavior belongs in `server/` and
  `sdk/`; browser flows that depend on navigation, authentication, or the
  assembled application belong in `e2e/`. Do not bypass the client dispatcher
  or API boundary merely to make a test easier.
- Each implementation slice includes focused tests/checks and a full
  `./gradlew check` before its commit. Do not push or release from this card.
- Keep each commit one complete behavioral slice: tests, implementation, and
  any required cross-layer changes together. Do not create dependency-only,
  test-only, or layer-only commits unless the slice is independently useful
  and safe to release.
- When evidence is incomplete, record an investigation and its result rather
  than introducing speculative abstractions, browser fallbacks, CSP allowances,
  or performance budgets.

## Safe Slice Sequence

- [x] **External resource hardening**: secure and pin third-party scripts and
      stylesheets in the main document and embedded GraphiQL document. Test
      intent: at the client build/template boundary, build/render the client and
      verify generated HTML contains exact versions plus SRI, anonymous CORS,
      and referrer policy where applicable;
      expected current failure is the missing CodeMirror SRI and the unpinned
      GraphiQL React/CSS resources. Testmints shape: setup owns the rendered
      resource inputs and fixture, exercise invokes the actual render or
      packaging boundary, and verify asserts emitted resource attributes; keep
      the subject call outside retrying assertions and confirm the intended red
      failure before production edits. Design: centralize immutable external
      resource metadata where the existing Vite/template boundary permits it;
      use `sha384` digests generated from known-good assets or vendor-published
      digests. This is independently safe because it changes resource
      verification and version pinning without changing product behavior.

- [x] **Referrer policy and report-only CSP starting point**: added at the
      existing deployment boundary, preserving application/resource behavior.
      Report-only CSP exposes unresolved source requirements; it does not block
      injection. Enforcement, CDN-loader compatibility, violation collection,
      and authenticated/integration policy validation are transferred to
      [CSP hardening](../work/CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md), not completed here.

- [x] **Accessible application shell and interaction audit (bounded scope)**: establish named
      landmarks, skip navigation, page-heading conventions, accessible iframe
      naming, form labels/descriptions, status announcements, and visible
      `:focus-visible` states. Test intent: use client component tests for
      rendered semantics and dispatched actions, plus E2E scenarios for
      keyboard/navigation behavior that depends on the assembled application;
      cover popup focus return, route/loading/error
      announcements, and GraphiQL iframe naming; expected failure is at least
      one inaccessible current interaction or missing accessible name.
      Testmints shape: setup arranges the page and initial focus/resource state,
      exercise performs one user-visible keyboard or route action, and verify
      checks the accessible name, focus target, announcement, or interaction
      result. Keep separate scenarios for distinct transitions and use the
      narrowest boundary that proves each contract. Design:
      use native HTML semantics first, preserve existing React/Kotlin wrappers,
      and use native `<dialog>` only where it matches an existing modal contract.
      This slice is independently safe because it improves operability without
      changing business actions.

- [x] **Image and motion behavior (bounded scope)**: make important image dimensions/aspect
      ratios explicit, lazy-load genuinely below-the-fold images, preserve
      eager loading for LCP candidates, and align animation behavior with
      `prefers-reduced-motion`. Test intent: verify image attributes/layout
      stability and reduced-motion behavior in browser tests; expected failure
      is a missing dimension/preference contract in the selected flows.
      Testmints shape: setup selects a representative image or motion
      preference, exercise renders the owning component or changes that
      preference, and verify observes emitted attributes, layout reservation,
      or animation behavior. Design:
      update the shared image/component boundary rather than adding ad hoc page
      rules. This is independently safe because image content and animations
      retain their visual purpose while gaining predictable fallback behavior.

- **Transferred, not completed — measured critical-path optimization**:
  [Initial performance pass](CPL-54-WEB_PERFORMANCE_MEASUREMENT_FOLLOWUP.md) completed
  Welcome measurement and verified payload cleanup. Remaining authenticated
  baselines, calibration and route-scoped CDN loading are in
  [CPL-62](../work/CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md).
  No broader performance improvement is claimed without comparable evidence.

## Bounded Finish Line

Target three remaining implementation slices, followed by one closeout review.
Investigation may establish that an existing behavior already meets a contract;
record that evidence instead of manufacturing a change or failing test.

- [x] **Shell announcements**: inspect the shared loading/error boundary and
  representative Welcome, party-list, and pair-assignment route transitions.
  Fix demonstrated missing error/status or route-context behavior at its owner;
  avoid announcing every transient loading state. Use component/router evidence
  unless the behavior actually requires the assembled application.
- [x] **Shared image behavior**: inspect the shared player/logo rendering used
  on those entry routes. Correct demonstrated missing layout reservation and
  inappropriate loading priority. Keep likely above-the-fold images eager;
  defer route-by-route image tuning to the performance follow-up.
- [x] **Reduced motion**: cover the Welcome entrance, notification/demo pulse,
  and pair-assignment animation owners. Honor reduced motion while preserving
  final content and usable controls; verify preference-dependent behavior.
- [x] **Closeout**: review existing evidence for shell landmarks/skip link,
  headings, GraphiQL naming, navigation, modal/focus behavior, and representative
  party/player/pin form labels. Fix regressions introduced by this card. Record
  other findings in the [extended accessibility follow-up](CPL-53-WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md),
  reconcile the broad checkboxes above, and move this card to work_completed.

After completion, perform the deferred agent-context review below. Do not extend
this card with unrelated polish, a complete screen-reader/browser matrix, all-route
heading/form cleanup, or speculative resource-loading changes. Newly found issues
that prevent the bounded contracts from working still belong in this card.

## Design Decisions

- Review correction (2026-11-17): earlier tab-order notes overstated the
  evidence. Most affected links contain native buttons; those buttons were
  already focusable, and removing the outer link's negative tab index can
  introduce duplicate tab stops. Passing general tests did not establish the
  claimed keyboard improvement. Correct the markup to one styled native link
  per navigation action and add rendered semantics regressions before extending
  the changes. Preserve destinations, names, and shared visual styles.
- Revised next slices: (1) introduce a styled router link and correct GraphiQL
  and Demo; (2) migrate the reviewed party navigation controls; (3) migrate
  client-page return and pin-creation controls; (4) audit notification popup
  behavior and remaining form/focus/motion contracts. Each implementation slice
  receives its own focused evidence and full check before commit.
- Notification modal decision (2026-11-17): replace this use of reactjs-popup
  with a native modal dialog, portaled outside the header. The old library owns
  Escape/focus return but cannot supply a named notification dialog through the
  current binding, and text-only notes leave focus outside. Native showModal
  owns modality, focus containment, Escape, and focus return; the close event
  owns seen-note bookkeeping. Preserve backdrop dismissal with coordinate-based
  press/release checks (including padding and drag guards) across browsers;
  do not rely on the newer, incompletely supported closedby attribute.

- Coupling boundary routing is explicit: `client/` owns the frontend and treats
  the server as external; `sdk/` owns GraphQL documents and dispatchers;
  `server/` owns schema and resolvers; `e2e/` owns assembled browser flows. A
  test stays at the narrowest boundary that fully observes its contract and
  adds a broader test only when behavior crosses that boundary.
- Reuse existing client component-test patterns and E2E page fixtures. For
  browser tests, follow the repository diagnostics rules, run browser-bearing
  Gradle tasks outside the sandbox with `--no-daemon`, and keep harness
  diagnostics distinct from product assertions.
- If a recommendation exposes a GraphQL or SDK contract issue, synchronize
  schema, resolver, SDK document, dispatcher, and contract tests in one slice;
  do not make client tests pass by silently bypassing the API.
- Test scenarios follow Testmints phase discipline: setup arranges fresh data,
  collaborators, and resources; exercise visibly calls the subject and awaits
  its completion; verify asserts observable behavior; teardown releases
  scenario-owned resources. Test names must describe the bounded behavior, and
  each scenario must have evidence that would fail if its behavior were
  removed. Use the project's configured runner, `assertIsEqualTo` conventions,
  and existing Testmints helpers rather than source/token assertions.
- Every slice follows this delivery gate before its checkbox is marked complete:
  (1) call the shot with behavior, boundary, contract, scenarios, and expected
  failure; (2) add one focused Testmints scenario or focused browser/build
  check and confirm the intended red failure; (3) call the smallest production
  design and data flow; (4) implement and run focused green checks; (5) review
  test clarity separately from production design; (6) run `./gradlew check`
  using the repository's browser/daemon guidance; (7) inspect the complete
  diff and run `git diff --check`; (8) record command results, remaining gaps,
  rollback/compatibility notes, and the commit ID before starting the next
  slice.
- Browser-bearing validation is a release gate for browser-facing slices, not
  an optional follow-up. If the browser harness or required credentials are
  unavailable, leave the slice incomplete and record the precise blocker.
- Security changes require negative evidence where practical: verify the
  intended resource succeeds and a changed digest, disallowed origin, or
  missing required policy is rejected or reported. Do not treat a successful
  build as proof of browser enforcement.
- Performance work must preserve a before/after baseline for the same route,
  viewport, authentication state, and network conditions. A metric change is
  not a valid optimization claim without comparable evidence and a check that
  user-visible readiness still succeeds.
- Treat the main HTML template and the GraphiQL `srcDoc` as separate resource
  ownership boundaries; both need equivalent third-party dependency hygiene.
- Prefer self-hosting critical third-party assets where practical, but do not
  self-host mutable analytics without first deciding the product/privacy
  requirements.
- Do not adopt CSS `url()` integrity modifiers as the primary solution yet:
  browser support is incomplete. Prefer HTML `<link>` SRI and document-level
  policy for cross-browser coverage.
- Markdown inserted through `dangerouslySetInnerHTML` is a security review
  item. Confirm the parser/sanitizer contract before changing rendering code.
- Runtime audits must distinguish product failures from the existing browser
  diagnostics harness and must follow the repository's browser execution
  guidance.

## Current State

Date: 2026-11-17

Completed against the bounded finish line. Final implementation slices:
`6ea31a1dd` (shell feedback), `29744a642` (shared image inspection), and
`9890ba386` (reduced motion). Full check passed before implementation commits;
closeout changes are documentation only. No push or release performed.
CSP enforcement, measured performance work, and extended accessibility auditing
have follow-up cards. Agent-context testing guidance is a subsequent task, now
unblocked by this card's completion, per the user's explicit sequencing.

Browser walkthrough: user accepted Get started, Demo final view, and About.
Chrome observed native Tab focus on the skip link/Welcome action and About's
single recovery link; Enter activated Welcome and About navigation. Authenticated
local account has no existing parties. The unsaved new-party form supplies the
shared header: native Tab visited Party Settings, Secrets, Contributions, and
Escape returned focus to Settings. The notification dialog is visibly rendered
with Close focused; Tab reached its first content link and Shift+Tab returned to
Close. Native Escape dismissed the dialog and returned focus to its trigger.
GraphiQL loaded in its named frame and Tab reached Documentation Explorer.
Early screenshot/input observations were intermittently stale or unavailable;
later captures agree. Screenshots of the form/dialog/editor were offered for
feedback; only Get started, Demo, and About have explicit user acceptance at
closeout. No form was saved or query executed. Saved-party/player/pin walkthrough,
OS-preference testing, and calibrated assistive-technology validation remain gaps.

Closeout review: rechecked shared main/skip markup, heading and GraphiQL naming
changes, native link structure, Settings/modal tests, shared focus rules, and
party/player/pin label owners. configInput connects label for/id; party dropdowns,
player selectors, and pin Target have explicit associations. This is representative
source/component evidence, not an all-route form audit. Original broad checklist
items are complete only to the bounded scope above; unmeasured performance,
all-route accessibility, and CSP enforcement are explicitly transferred. Recorded
skip-link/landmark design, generic portrait alternatives, Demo simulated-control
semantics, loader distinctions, and local CSP notices in their follow-up cards.
Reviewed test clarity separately from production ownership; no unresolved
introduced regression found. Final full gate: 680 tasks, 65 executed; focused
browser scenarios: 33; existing component suite: 118. Documentation diff check
passed; unchanged successful code checks were not repeated for closeout prose.

Relevant paths:

- `client/src/jsMain/resources/index.html`
- `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/graphql/GraphiQL.kt`
- `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/Main.kt`
- `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/ImagePath.kt`
- `client/src/jsMain/resources/com/zegreatrob/coupling/client/style.css`
- `client/src/jsMain/resources/com/zegreatrob/coupling/client/animations.css`
- `client/vite.config.mjs`
- `Caddyfile`
- deployment module configuration under `deploy/`

## Validation

Planning review completed:

- Google `modern-web-guidance` `legacy-scan client --json`: one finding,
  missing SRI on the CodeMirror CDN stylesheet (baseline red evidence).
- Retrieved and reviewed the `secure-external-subresources`, `accessibility`,
  `performance`, and `security` guidance.
- Slice 1 implementation: pinned React to 18.3.1 and GraphiQL to 3.8.3,
  added SHA-384 SRI from known-good vendor bytes, anonymous CORS, no-referrer,
  and a strict-origin-when-cross-origin document policy to the main document
  and GraphiQL `srcDoc`. Mutable Font Awesome and Google Fonts remain deferred
  to the security-policy slice.
- Focused validation: `legacy-scan client --json` passed with
  `totalFindings: 0`; `./gradlew :client:jsBrowserProductionVite
  --no-daemon` passed and emitted the hardened production HTML; `git diff
  --check` passed.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox, including client browser tests and E2E (`690` tasks).
- Review outcome: the resource-template boundary and production packaging
  observe the changed contract; no behavioral Testmints scenario was added
  because this slice is covered by the deterministic security scan and build
  output rather than a user interaction.
- Testmints review is a completion requirement for each behavioral test: review
  setup/exercise/verify ownership, expected red evidence, observable contract,
  cleanup, and scenario naming before recording the slice as complete.
- The work card and implementation pass `git diff --check`; this slice is
  complete and release-safe after its commit. Rollback is limited to the slice
  commit and preserves the existing HTML loading architecture.
- Commit: `3a429e00419f48c21b0fde7665f9273122a78324`.

Slice 2 — staged deployment security policy (2026-11-17):

- Call-the-shot red evidence: Caddy responses had no security headers.
- Design review: applied companion headers and a report-only CSP at the Caddy
  frontend/server boundary for `localhost` and `static.localhost`; retained
  OAuth, CDN, same-origin API/WebSocket, and GraphiQL-compatible sources while
  deferring enforcement until real violation evidence is collected.
- Focused validation: `docker compose config --quiet` and Caddy validation
  passed; live HTTPS `curl -I` responses on both origins included
  `Content-Security-Policy-Report-Only`, `Referrer-Policy`,
  `X-Content-Type-Options`, `X-Frame-Options`, and `Permissions-Policy`;
  `git diff --check` passed.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Review outcome: report-only rollout is deliberate because the repository has
  no configured violation reporting endpoint; enforcement remains a follow-up
  after observing authenticated, OAuth, integration, and GraphiQL traffic.
- Commit: `29d5c337745038586863b4a25dfd10f6becbd4c6`.

Accessibility progress (2026-11-17):

- Confirmed defect: the GraphiQL iframe had a browsing name but no accessible
  title. Added `title="GraphiQL query editor"` at the component boundary.
- Focused validation: `fish -lc './gradlew :client:jsBrowserTest
  --no-daemon'` passed.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `1bafb2ba3e30d16c74117d7796007d973fde5a13`.
- The broader accessibility slice remains open for landmarks, skip navigation,
  headings, focus-visible states, announcements, and keyboard transitions.

Accessibility progress — shell navigation (2026-11-17):

- Added a native skip link targeting a focusable `<main id="main-content">`
  around the existing React mount, with a visible `:focus-visible` state.
- Focused validation: `fish -lc './gradlew :client:jsBrowserProductionVite
  :client:jsBrowserTest --no-daemon'` passed and emitted the updated HTML.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `d4b7029371f5e351f3a5fc768aa50dbf49a5ac81`.
- The broader accessibility slice remains open for route-specific headings,
  status announcements, popup focus return, and keyboard transitions.

Accessibility progress — visible focus (2026-11-17):

- Added a global `:focus-visible` outline with offset, preserving native
  keyboard focus indication across client controls.
- Focused validation: `fish -lc './gradlew :client:jsBrowserProductionVite
  :client:jsBrowserTest --no-daemon'` passed.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `4748f286e13b0fa4d5967fa30dd915bb2fafeea2`.

Accessibility progress — GraphiQL loading status (2026-11-17):

- Wrapped the authorization loading message in a typed `role="status"` and
  `aria-live="polite"` live region. The loader currently collapses errors to an
  empty state, so error announcement remains deferred until that contract is
  clarified.
- Focused red evidence: plain string ARIA assignments failed Kotlin/JS type
  checking; typed wrapper values with localized `unsafeCast` compile correctly.
- Focused validation: `fish -lc './gradlew :client:jsBrowserProductionVite
  :client:jsBrowserTest --no-daemon'` passed after the correction.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `7024b321110ac36e9a0d0c146be25b797c8338d2`.

Accessibility progress — welcome page heading (2026-11-17):

- Promoted the visual “Coupling!” welcome title from a generic `div` to a
  native `<h1>`, preserving the existing fitty sizing and animation refs.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  --no-daemon'` passed. The attempted `:client:components:jsBrowserTest`
  task does not exist in this module.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Review outcome: the welcome screen now contributes a page-level heading to
  the document outline without changing its visual behavior.
- Commit: `aacbc8013`.

Accessibility progress — welcome chooser heading (2026-11-17):

- Added a native `<h2>` naming the login chooser’s two continuation actions,
  without changing the existing login or demo controls.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  --no-daemon'` passed (`208` tasks, `26` executed).
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Review outcome: the chooser now has a semantic section name; focus-return
  behavior remains deferred because the current interaction replaces the
  trigger and exposes no close action or return target.
- Commit: `f74ca0d86`.
- Follow-up wording correction: changed the chooser heading to the concise
  text “Get started” after review.
- Focused and full validation passed again; full check completed with `680`
  tasks, including browser/E2E tasks.
- Commit: `b1bccf5a8`.

Accessibility progress — GraphiQL navigation name (2026-11-17):

- Added the accessible name `GraphiQL` to the icon-only GraphiQL link and
  marked its image decorative with empty alt text.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  --no-daemon'` passed (`208` tasks, `26` executed); existing React `act` and
  chart-size warnings remain non-failing.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `627265b47`.

Accessibility progress — settings control name (2026-11-17):

- Added the accessible name `Settings` to the gear-only settings button via
  its existing button attribute boundary.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  --no-daemon'` passed (`208` tasks, `26` executed). An initial direct ARIA
  assignment failed the Kotlin DSL boundary and was corrected through
  `buttonProps`.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `854443795`.

Accessibility progress — notification trigger semantics (2026-11-17):

- Replaced the custom clickable notification `div` with a native `<button>`
  trigger and added the accessible name `Recent information`, preserving the
  existing third-party popup behavior.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  --no-daemon'` passed (`208` tasks, `26` executed); focused lint also passed
  after correcting import ordering.
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `4ae0f09c9`.

Accessibility progress — GraphiQL keyboard navigation (2026-11-17):

- Removed the explicit `tabIndex = -1` from the named GraphiQL navigation link,
  restoring the native link’s keyboard reachability.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  --no-daemon'` passed (`208` tasks, `26` executed).
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `6f5dacfd4`.

Accessibility progress — Demo keyboard navigation (2026-11-17):

- Removed the explicit `tabIndex = -1` from the visible-text Demo navigation
  link, restoring its native keyboard reachability.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  --no-daemon'` passed (`208` tasks, `26` executed).
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `e86bb1553`.

Accessibility progress — standard navigation tab order (2026-11-17):

- Removed explicit `tabIndex = -1` exclusions from nine standalone, visibly
  named links: Add Player, Retirees, Party Select, Statistics, Pin Bag,
  History, Logout, About, and Gravatar. Dropdown links and composite controls
  remain deferred for separate focus-management review.
- Focused validation: `fish -lc './gradlew :client:components:jsTest
  :client:jsTest --no-daemon'` passed (`255` tasks, `40` executed).
- Full validation: `fish -lc './gradlew check --no-daemon'` passed outside the
  sandbox (`680` tasks, including browser/E2E tasks).
- Commit: `0d63e0fec`.

Accessibility progress — demo return keyboard access (2026-11-17):

- Removed the explicit `tabIndex = -1` from the demo’s native return link.
- Focused client tests passed; full validation passed with `680` tasks,
  including browser/E2E tasks.
- Commit: `ab970d30c`.

Accessibility progress — About navigation keyboard access (2026-11-17):

- Removed the explicit `tabIndex = -1` from the About page’s native back link.
- Focused client tests passed; full validation passed with `680` tasks,
  including browser/E2E tasks.
- Commit: `750c3ff26`.

Accessibility progress — pin creation keyboard access (2026-11-17):

- Removed the explicit `tabIndex = -1` from the Pin List “Add a new pin” link.
- Focused client tests passed; full validation passed with `680` tasks,
  including browser/E2E tasks.
- Commit: `13d4316e2`.

Review correction — single-target GraphiQL and Demo navigation (2026-11-17):

- Two Testmints component regressions failed on the nested `HTMLButtonElement`
  before implementation. They now verify one named native link, the expected
  destination, and no interactive descendants or tab-index exclusion.
- Added `CouplingLink` using existing shared visual rules; migrated GraphiQL
  and Demo. This supersedes the earlier tab-index-only fixes for these controls.
- Focused `:client:components:jsTest` passed (117 tests). Full
  `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 81 executed),
  including browser/E2E validation. An interface-formatting lint failure was
  fixed before the successful full check. `git diff --check` passed.
- Scope of evidence: rendered semantics and existing navigation flows; no
  claim of a complete assistive-technology audit.

Review correction — party navigation semantics (2026-11-17):

- Previous correction commit: `5638c2a6b`.
- Migrated Add Player, Retirees, Party Select, Statistics, Pin Bag, History,
  Sign Out, and Prepare to Spin to a single styled native link each. Updated
  E2E role selectors for the intentional button-to-link semantic change.
- Added a Testmints scenario covering names, destinations, native tab order,
  and absence of nested controls. Confirmed failure on `HTMLButtonElement`
  before migration; focused `:client:components:jsTest` then passed (118 tests).
- Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 78 executed),
  including E2E navigation flows. `git diff --check` passed.

Review correction — client-page navigation and test level (2026-11-17):

- Previous correction commit: `c99ae1bec`.
- Migrated About, About back, New Party, Add Pin, and demo return to the
  shared styled native link. The demo return has an explicit accessible name
  and decorative logo; it reuses the established image registry.
- Added component tests using React Router's memory router and Testing Library.
  They cover accessible names, hrefs, absence of nested controls, and Tab/Enter
  activation reaching `/parties`, without application server or authentication.
  Existing E2E flows remain; their New Party role selector now expects a link.
- Focused `:client:jsBrowserTest --no-daemon` passed (9 tests, including four
  new scenarios). A temporary nested About button then made the semantic test
  fail as intended; the fault was removed before final validation.
- Test harness uses named Testing Library imports because the existing wrapper's
  default import is incompatible with this browser bundler. Minimal local bindings
  expose cleanup, rendering, role queries, and keyboard interaction.
- Full `fish -lc './gradlew check --no-daemon'` passed (690 tasks; 72 executed,
  5 from cache), including the four green navigation regressions and unfiltered
  E2E flows. `git diff --check` passed. Test review confirmed the semantic fault
  is detected; production review confirmed shared styles and destinations remain
  intact. Simulated keyboard coverage does not replace assembled-route/auth E2E
  coverage or a complete assistive-technology audit. Image registry placeholders
  produce image 404s in isolated tests; these tests make no image-loading claims.

Notification trigger form safety (2026-11-17):

- Previous client-navigation correction commit: `6f028131e`.
- Opening recent information inside a form must not submit it. The browser
  component regression failed with one submission, then passed after explicitly
  setting the trigger's native button type. Popup behavior is unchanged.
- Focused `:client:jsBrowserTest --no-daemon` passed (10 tests). Full
  `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 75 executed),
  including E2E flows. A missing Kotlin extension import and test wrapping lint
  were corrected before the successful gate. `git diff --check` passed.
- Separate reviews confirmed an actual submission regression, fixture cleanup
  for Markdown/storage state, and a production change owned solely by the trigger.

Named native recent-information modal (2026-11-17):

- Previous slice commit: `739fe5842`.
- Replaced this notification's custom popup with a native modal dialog, named
  by its visible heading and portaled outside the header. Added an initially
  focused Close button. Native close events preserve seen-note bookkeeping.
  The browser owns modality and focus return; no manual focus trap or Escape
  handler was added. Backdrop dismissal requires both press and release outside
  the dialog bounds, preserving padding clicks and text-selection drags.
- Two new scenarios first failed on the unnamed dialog/missing Close button.
  Focused `:client:jsBrowserTest --no-daemon` then passed (16 tests, including
  seven notification scenarios). Coverage includes native modal state, initial
  focus, close-button and platform close-request paths, seen-note storage, focus
  return, and backdrop/padding/drag behavior. The platform test invokes
  `requestClose()`; it does not simulate trusted Escape or mobile back gestures.
- Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 78 executed).
  `git diff --check` passed. Test review checked user-observable assertions and
  fixture cleanup; production review checked portal ownership, native focus
  lifecycle, bounded scrolling, and cross-browser backdrop handling. Scroll
  containment is progressive enhancement where the browser supports it.

Visible focus for light shared controls (2026-11-17):

- Previous slice commit: `586ac4f6d`.
- White/yellow color rules no longer override focused outlines with transparency.
  Existing shell focus-visible styling and browser defaults can now apply to
  both shared buttons and links. Background/text/hover colors are preserved.
- Two browser component regressions Tab to a white button and yellow link and
  inspect focus plus computed outline color/style/width. Both failed before
  removing the suppression, then passed. This checks visible outline presence,
  not a complete contrast audit or the shell's exact custom outline color.
- Focused `:client:jsBrowserTest --no-daemon` passed (18 tests). Full
  `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 72 executed).
  `git diff --check` passed. Reviewed test evidence independently from the
  shared color implementation; reused the client memory-router rendering helper.

Named and keyboard-reachable pair image control (2026-11-17):

- Previous slice commit: `a4d08d937`.
- Named the clipboard icon button "Copy pair image to clipboard" and removed
  its negative tab index. Existing ClipboardItem feature detection and screenshot/
  clipboard action are preserved. No new clipboard permission or write behavior.
- A browser component test renders PairSection and Tabs to the named control.
  Before implementation it failed on the unnamed button with tabindex=-1; after
  implementation focused `:client:jsBrowserTest --no-daemon` passed (19 tests).
- Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 72 executed).
  `git diff --check` passed. Test review confirmed focus is observed after actual
  component rendering, while production review confirmed the click action remains
  untouched. Clipboard-image correctness is outside this regression's evidence.

Settings dropdown keyboard access (2026-11-17):

- Previous slice commit: `a966a58a6`.
- Settings now exposes expanded state and its controlled region. Its three
  native links participate in Tab order. Escape dismisses and restores trigger
  focus; focus leaving the disclosure dismisses without stealing focus. Hover
  and click-open behavior remain, with mouse-leave dismissal guarded so that
  it cannot remove a keyboard-focused link. No menu roles or arrow-key model.
- Four component/router scenarios failed on missing state, skipped links, and
  missing dismissal before implementation. A fifth regression reproduced
  mouse-leave removal of a focused link, then passed with the focus guard.
- Focused `:client:jsBrowserTest --no-daemon` passed (24 tests). Full
  `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 77 executed).
  `git diff --check` passed. Test review checked focus order, destinations,
  controlled-region association, and dismissal. Production review confirmed
  dropdown links are used only by Settings and state remains component-owned.

Shared Close control form safety (2026-11-17):

- Previous slice commit: `29b7959f2`.
- The shared Close button now has explicit type=button. Its accessible name,
  appearance, and dismissal callback are unchanged.
- A browser component regression first reproduced one unintended enclosing-form
  submission while the dismissal callback ran once. After implementation,
  focused `:client:jsBrowserTest --no-daemon` passed (25 tests).
- Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 72 executed).
  `git diff --check` passed. Test and production reviews confirmed the control
  retains its dismissal contract and no longer performs an implicit form action.

Contributor email action naming and form safety (2026-11-17):

- Previous slice commit: `3f8cf72e3`.
- Contributor email action buttons now identify their action and visible player
  name instead of exposing UUIDs as labels. Explicit type=button prevents an
  incidental enclosing-form submission. The canonical save command is unchanged.
- Strengthened the existing ContributorMenu component/router test rather than
  adding E2E coverage. It first failed to find the meaningful action name; after
  that fix it reproduced one unwanted submission. The final focused
  `:client:components:jsTest --no-daemon` passed (118 tests), including the exact
  expected SavePlayerCommand and zero submissions from the same activation.
- Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 81 executed).
  `git diff --check` passed. Test review preserved the original command assertion;
  production review confirmed identity still uses player IDs and only accessible
  naming/button behavior changed.
- Seven-slice batch review: notification form safety and native modality, shared
  focus, clipboard-control access, settings keyboard navigation, Close form safety,
  and contributor actions each have focused failure/green evidence and a passing
  full gate before commit. No new E2E scenarios were needed. Shell announcements,
  image/motion behavior, and performance measurement remain open; the agent-context
  guidance review stays deferred until the card is completed.

## Deferred Agent-Context Review

Reduced motion (2026-11-17): shared useAnimationsDisabled combines the explicit
context flag with the live system preference, with subscription cleanup. Welcome
entrance/hover/pulse styles are added only when enabled. Notification and Demo
return pulses stop after three seconds; Welcome's automatic pulse stops after
four seconds. Pair assignments immediately render final children under reduced
motion, including a change during the spin; route content never waits for an
animation event. Loading renders its static initial frame under the same policy.
Six browser regressions cover final pair content, live preference, explicit flag,
Welcome styles, notification/Demo styles, and preserved resolved-route content.
Initial reduced preference and CSS checks reproduced the missing behavior; the
live-spin fixture needed its real DnD provider before supplying valid evidence.
Review caught and fixed an intermediate route-rendering regression with a failing
content-preservation test. jsdom received a non-matching event-capable matchMedia
fixture; its older empty function fixture was corrected rather than adding a
production fallback for a test-only API omission. Focused client browser tests
passed (33); existing component tests passed (118). Full
`fish -lc './gradlew check --no-daemon'` passed (680 tasks; 65 executed).
Test and production reviews plus `git diff --check` passed. This is simulated
preference evidence in a real browser, not an OS-settings or screen-reader audit.

Shared image review (2026-11-17; shell slice `6ea31a1dd`): PlayerCard's
portrait has explicit square CSS dimensions in em and its card reserves width
and height. CouplingLogo's SVG has explicit width/height and viewBox; the Demo
return logo has a fixed 38-by-36 CSS-pixel container. These shared entry-route
images can be above the fold and already use eager/default loading. No missing
reservation or inappropriate shared lazy loading was demonstrated. Preserve
these contracts; measured LCP selection/priority and route-specific offscreen
deferral remain in the performance card. This is inspection evidence, not a
measured CLS/LCP result. No production/test changes or invented failing test;
the preceding successful full check still applies unchanged.

Shell feedback (2026-11-17): named native indeterminate progress now exposes
loading without a noisy live announcement. The shared failed/null-result fallback
exposes an alert and one native Go back home link, removing nested interaction.
Existing CouplingRoute already updates document titles. Both new browser component
scenarios failed on missing semantics, then passed (27 client scenarios).
Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 70 executed).
Test review checked indeterminate state and the recovery destination/structure;
production review confirmed no data-loader or authentication contract changes.
Manual browser review remains incomplete; native UI control has been unreliable.

User request: only after this card is finished, review the testing pitfalls
encountered here and add durable guidance where it belongs. Do not update agent
context documents for this follow-up while the card remains open.

At completion, consider `agents.d/context/TESTING.md` first for test-boundary
selection and evidence requirements; use other authoritative documents only
where their ownership fits. Avoid duplicating existing instructions or turning
temporary harness workarounds into permanent rules. Candidates to assess:

- Component tests with a memory router can prove local link activation and
  destination changes; navigation alone does not require E2E. Retain broader
  coverage for contracts involving assembled routes, authentication, or services.
- Passing general checks do not prove an accessibility improvement. Inspect the
  rendered interactive structure and verify the claimed regression, including
  nested controls and duplicate tab stops when changing navigation semantics.
- Distinguish simulated keyboard interaction from actual browser keyboard and
  assistive-technology evidence; report the boundary and limitations precisely.
- Diagnose module exports, bundler-specific assets, and fixture property
  shadowing before treating a setup failure as meaningful behavioral red.
- Reuse image registries in isolated rendering tests, and avoid image-loading
  claims when fixtures supply placeholder URLs.

For each completed slice, record the focused command/result, full
`./gradlew check` result, review outcome, and commit ID here. At completion,
review the entire sequence against the goal and move this card to
`agents.d/work_completed/`.
