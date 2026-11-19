# Page headings exclude shared navigation

## Goal and Constraints

Bounded slice from [accessible data/reflow review](../work/WEB_ACCESSIBLE_DATA_AND_REFLOW_FOLLOWUP.md).
GeneralControlBar should expose only its page title as the level-one heading.
Preserve established typography, wrapping, notification position, control order,
and page layout. This card does not fix narrow Party List reflow or redesign headers.

## Safe Slice Sequence

- [x] Reproduce the heading name at the shared component boundary in jsdom;
  expect navigation text contaminates its name. Make the outer layout neutral
  and move native h1 semantics to title text, preserving existing style metrics.
  Run component tests, existing real-browser geometry tests, full check and
  native before/after comparison. Commit and close this independently useful fix.

## Evidence and Design

Native Chrome Party List exposed heading “Party List About Demo Sign Out GraphiQL
Recent information”; screenshot recorded before implementation. This is native
accessibility-tree evidence, not a VoiceOver transcript. GeneralControlBar owns
this markup and is reused for Party List, User Config and Slack frames. No
per-page workarounds. The component suite can prove the heading name and absence
of nested interactive targets; browser evidence owns geometry and appearance.


## Implementation and Review

Component regression failed because heading “Party List About” could not be found
as “Party List.” Kept the outer inline-flex geometry wrapper and explicitly
preserved its former h1 font size, weight and vertical margins; moved native h1
to the title text with inherited sizing and zero margins. Controls and notification
positioning remain in their original wrapper/order. No ARIA heading override or
per-page adjustment is introduced.

Test review: component/jsdom verifies the exact heading name and native H1,
absence of interactive descendants, and continued named link/notification access.
Browser geometry remains in the existing application layout suite rather than
moving semantic assertions there. Production review confirms only shared header
markup/style ownership changes; navigation, notification behavior and callbacks
are preserved. Modern Web Guidance accessibility: “Structure main content with
headings”; shared navigation must not become part of the page's heading name.

Focused validation: components/jsdom (137 tests), component formatting, and
`:client:jsBrowserTest` (51 tests) pass. Full check and native rebuilt-app
comparison pending. No push/release authorization.


## Completion

Full `fish -lc './gradlew check --no-daemon'` passes (680 tasks); log
`/tmp/coupling-header-semantics-check.log`. Native Chrome at the same 1200x953
window after hard reload exposes level-one heading “Party List” with About, Demo,
Sign Out, GraphiQL and Recent information as separate controls. Settled before/
after screenshots show matching title, logo, control, notification and content
placement. Initial reload screenshot caught image loading; the settled comparison
confirmed artwork loaded. Native Tab still reaches About first with visible focus.
This is native Chrome/tree evidence, not a new VoiceOver certification.

Overall review: shared ownership, exact heading-name regression, existing browser
layout evidence, unchanged visual placement and full-suite gate satisfy this
bounded card. Narrow-width overflow, contrast, chart data alternatives and other
broader-card findings remain separate. Diff checked; no push/release performed.
Closed after test and production reviews.
