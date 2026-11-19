# Distinct browser titles for integrations and contributions

## Goal and Constraints

Split from [accessible data/reflow review](../work/WEB_ACCESSIBLE_DATA_AND_REFLOW_FOLLOWUP.md).
Correct Integrations being titled Pin List and distinguish contribution Overview,
List and Visualization in browser tabs. Preserve page headings, controls, layout,
and the existing app-name prefix. No route/auth/service behavior changes.

## Safe Slice Sequence

- [x] Render affected production route definitions through a memory router and
  observe document.title on navigation; use an inert page body to isolate the
  route-owned metadata from authenticated queries. Expect the existing titles to
  fail. Correct the four route labels, run focused and full checks, native review,
  then commit. This is independently useful without other accessibility work.

## Design and Validation

CouplingRouter owns route labels; CouplingRoute owns the document-title effect.
Expose the existing authenticated route factory internally for the application
suite without changing runtime behavior. Test substitutes only the page body,
retaining the actual route label and title effect. Tests stay in client because
this contract is application-owned. Its existing suite runs in Chrome; no new
E2E or service dependency is warranted. No screen-reader certification claimed.


## Completion

The regression failed on actual document titles: Pin List for Integrations and
Contributions for all three contribution routes. Corrected the four labels to
Integrations, Contributions Overview, Contributions List and Contributions
Visualization. The existing app-name prefix and shared title effect are retained;
no page headings, controls, CSS or service behavior changed.

Test review: one navigation sequence observes each resulting document.title and
preservation of the app prefix. It uses actual authenticated route labels and the
real CouplingRoute effect, replacing only page bodies so queries/authentication
are outside this contract. The client application suite is the owning existing
suite; it runs Chrome, but the behavior itself needs only DOM/memory-router
capabilities. No E2E scenario added. Test-only act binding matches the suite's ESM
import style. Initial binding and shadowed fixture-property failures were harness
issues, corrected before confirming the meaningful title failure.

Production review: four route labels plus private-to-internal visibility of the
existing authenticated route factory; no new metadata layer or title mechanism.
Modern Web Guidance accessibility: “Provide unique page titles” and “Update
single-page application (SPA) titles during page transitions.”

Validation: `:client:jsBrowserTest :client:formatKotlin` passes (51 browser tests).
Full `fish -lc './gradlew check --no-daemon'` passes (690 tasks), log
`/tmp/coupling-route-title-check.log`. Native Chrome on the disposable local party
confirmed the old Integrations title before rebuilding, corrected Integrations
after hard reload, then Overview/List/Visualization titles through navigation.
Existing rendered headings remained Integrations!/Contributions. No integration
was connected or data changed. Diff checked; no push/release performed. Closed.
