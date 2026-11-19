# CPL-54 — Welcome performance baseline and payload cleanup

## Outcome and Closeout Scope

Closed the bounded first performance pass: repeatable Welcome measurement,
verified dead-loading removal, and lossless artwork compression. The original
draft also proposed authenticated routes, broader calibration and interaction
traces. Those are not complete; they are transferred to
[CPL-62](../work/CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md).
That card also owns the route-scoped CDN loading work.

Keep Auth0, GraphQL/WebSockets, GraphiQL and integrations functional; preserve
visual placement, artwork and the eclectic game-show palette. No CSP enforcement,
self-hosting migration, production deployment or field-performance certification.
[CPL-52](../work/CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md) retains CSP ownership.

## Completed Slices

- [x] `e8badbcf4`: remove obsolete reactjs-popup loading, bindings/package and CSS.
- [x] `cf235ba80`: opt-in Gradle Welcome baseline with pre-navigation observers,
  repeated fresh-profile/warm samples, explicit cache/viewport/readiness conditions.
- [x] `3c3969426`: browser-protocol network totals with fixed provider categories,
  bounded tracking, unknown/pending/failure/abort counts and no raw request metadata.
- [x] `e10d870fe`: losslessly compress six Welcome portraits, saving 134,289 bytes
  (8.8% combined). Scanline data, all non-IDAT chunks, color profiles and dimensions
  remain byte-identical. Production-emitted PNGs match the recompressed sources.
- [x] `d503f84d5`: remove unused root date-fns CDN namespace; retain the actual
  locally bundled formatDistance binding and npm packages.
- [x] `440a99984`: remove unused eager Lodash, Nivo Line, direct Popper Core and
  AutoSizer namespaces. Preserve active packages, consumers and surviving URLs.
- [x] Final review and clean-commit measurement; retain explicit follow-ups for
  work not completed rather than treating draft scope as measured evidence.

## Verified Findings

Initial measured Welcome CDN graph: 601 module responses. After unused namespace
removals: 323 (278 fewer, about 46%). These are observed module responses, not all
page requests. The same three-pair/six-sample method retains readiness, LCP/CLS, long
tasks, actual viewport and protocol/resource-timing counters.

[Controlled bundle comparison](../research/WEB_CDN_BUNDLE_SIZE_COMPARISON.json):
all 115 emitted JS/MJS files, including lazy chunks, were counted. Total local JS
was 2,333,969 bytes before date removal, 2,333,913 after, and 2,333,607 after the
four-entry cleanup. Only the startup bridge group shrank; no other chunk group
grew. Every surviving CDN URL was unchanged. These changes did not shift package
bodies into local chunks. Offline gzip estimates are labelled separately.

Welcome chooses random portraits: Rosie/Wendy initially totalled 860,089 bytes,
Grayson/Wayne 356,854. This explains much of the static-payload variation and means
unmatched aggregate byte/timing differences cannot all be attributed to code.
[PNG verification](../research/WEB_WELCOME_PNG_COMPRESSION.json) records exact
before/after source sizes and preserved scanline/metadata hashes. Each selected
pair saves about 34–65KB of PNG payload, without visual changes.

## Final Baseline and Limits

[Closeout baseline](../research/WEB_PERFORMANCE_CLOSEOUT_BASELINE.json) was captured
from clean commit `440a99984` (workingTreeDirty false). Three fresh Chrome profiles,
each followed by a warm refresh: unthrottled local app plus live external CDNs,
1280x900 outer window, actual viewport recorded, two-second post-readiness window.
All six samples completed with zero failed fetches and 323 module responses.
Observed readiness: 764–961ms cold, 190–212ms warm. This is bounded lab evidence,
not a field metric or broad speed guarantee. No timing budget was introduced.

Some prior runs had transient module failures/ERR_ABORTED events; preserved in the
network artifacts, not silently discarded or claimed fixed. Warm LCP can remain
null despite observer support. Page Resource Timing hides many cross-origin byte
sizes; BiDi counters are browser-reported completed responses, not wire captures
or final page-lifetime totals. Zero/unknown, pending and truncated data remain
explicit. Random content, external caches and unthrottled host conditions limit
comparability. No authenticated performance baseline or INP claim is made here.

## Validation and Review

Latest implementation full gate: `fish -lc './gradlew check --no-daemon'` passed
(680 tasks), `/tmp/coupling-unused-namespaces-check.log`. Existing component tests
(137) and application browser tests (52), including formatter, popup and layout
contracts, pass. Six new metric/network scenarios live in the Node suite (21 total).
Testmints review keeps aggregation below browser flows and actual observer/network
integration in the opt-in measurement task. Mechanical asset/dead-loading changes
used existing regressions plus artifact/bit comparisons, without fabricated reds.

Final `:e2e:measureWelcomePerformance` passed; log
`/tmp/coupling-performance-closeout-baseline.log`. Closeout changes only documents
and retained measurement evidence, so the unchanged successful full gate was not
rerun. Runtime design review confirms no visual/layout/interaction changes, no
package version upgrades or React-sharing changes. No push/release performed.

## Evidence and Handoff

[e2e/performance instructions](../../e2e/performance/README.md) describe invocation,
report scope and privacy. Research artifacts retain the initial Welcome baseline,
preliminary and final network captures, post-PNG/date/namespace measurements,
controlled bundle comparison and final clean-commit snapshot. No tokens, headers,
query URLs, bodies or party identifiers are captured by these reports.

CPL-62 owns non-resetting auth fixtures, authenticated routes, warm-observer/abort
calibration and interaction evidence. The normal E2E bootstrap clears test-account
parties and is not an appropriate profiling entry point. CPL-62 also owns further
active-library deferral, including local-bundle and first-use tradeoffs. CPL-56
continues the separate accessibility data/reflow work. None of those findings is
claimed resolved by this closeout.
