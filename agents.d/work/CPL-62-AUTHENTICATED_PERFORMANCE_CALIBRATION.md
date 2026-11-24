# CPL-62 — Explain authenticated route performance with application logs

**Work card protocol: 2**

## Goal and Constraints

Make Party List and saved current-pairs readiness measurable and explainable from
ordinary application logs, then use matched evidence to assess one route-only CDN
dependency group. Reuse application signals for support, reliability diagnosis and
release comparisons as well as performance. Preserve behavior, visuals, animations,
authentication and ordinary E2E coverage. No timing budgets until variability is
understood. The route-observability baseline is independently deliverable.

This continues [CPL-54](../work_completed/CPL-54-WEB_PERFORMANCE_MEASUREMENT_FOLLOWUP.md).
Its Welcome lab baseline remains historical evidence, not field certification.
[CPL-52](CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md) owns CSP independently.
[CPL-66](CPL-66-APPLICATION_LOG_PERFORMANCE_OBSERVABILITY.md) owns general runtime
command log quality. Neither card must finish first: reuse existing contracts or
ship a necessary shared change with this card's complete slice and share evidence.

## Evidence Policy and Scope

Start with existing client, SDK and server logs and their actual destinations.
Document what a person with log access can answer before proposing instrumentation.
Route readiness belongs in the application's routing/data/action lifecycle, not
solely in a WebDriver polling loop. Define readiness as fixture data plus the usable
destination action, never just a header/loading shell. Name what that semantic
signal cannot establish about paint, layout or user-perceived responsiveness.

Use bounded structured route/interaction summaries through existing logging paths.
Audit browser-log availability: console output alone does not establish centrally
accessible telemetry. If existing collection cannot carry a required signal, make
that gap and the smallest delivery choice explicit before implementation. Do not
silently add an ingestion service, vendor SDK or browser-to-server transport.
A material infrastructure/scope change must be resolved with the user; keep any
useful local-log slice independently deliverable and central coverage labeled open.

Browser tooling remains appropriate for evidence logs cannot establish: cold/warm
cache control, rendering metrics, protocol bytes, generated imports and first-use
loading behavior. Reuse `e2e/performance/` and ordinary application tests. Do not
create a parallel recorder, observer framework, fixture system or experiment runner.
Apply [tooling purpose and lifetime](../context/GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime).

## Acceptance Evidence

- Ordinary route logs identify route template, elapsed duration, terminal outcome,
  navigation correlation and available release/environment context. Dynamic party
  IDs, user content, tokens, raw URLs and request bodies are excluded. Define units,
  readiness boundary and sample/collection limitations; missing samples are not zero.
- Supply standard log queries for route count, p50/p95 readiness, failure/cancellation
  rate and correlation with relevant command/request work. Report counts and missing
  coverage; do not sum overlapping child durations as total route time. Show actual
  emitted records through the intended logging path, not only synthetic fixtures.
- Reproduce `/parties/` and `/:partyId/pairAssignments/current/` with four players,
  two saved pairs and three cold/warm pairs per route. Record fixture counts,
  auth/cache/animation state, actual viewport, host/browser and source revision.
  Compare application readiness logs to the lab observation and explain differences.
  Retain aborts, failures, null metrics and collection limitations.
  Record `ENABLE_PRERELEASE_FEATURES` as enabled or disabled and relevant
  integration configuration. Match those conditions when comparing samples under
  the [application configuration contract](../context/ARCHITECTURE_CANONICAL.md#build-and-deployment).
  Do not introduce environment-specific measurement or feature configuration.
- For a selected CDN group, retain matched startup/first-use request, byte and full
  emitted-JS comparisons with action/recovery evidence. A runtime inventory that
  rules out a safe candidate can close the loading investigation. Logs alone cannot
  prove actual CDN bytes or visual correctness.
- Focused contract tests and `./gradlew check` pass for each implementation slice.
  Existing deployment/log access may validate destination coverage when available;
  this card does not authorize deployment. Explicitly label unverified field coverage.

## Safe Slice Sequence

One contributor carries each slice across its required layers through completion.

- [ ] Inventory existing route/command signals and log destinations; retain concise
  query examples, missing fields and coverage findings. Choose the smallest useful
  route first. Record the exact semantic readiness/outcome contract and owner in
  the existing client routing/data lifecycle before tests.
- [ ] Deliver that route's structured lifecycle summary. Test successful data plus
  usable action, failed load, superseded navigation/cancellation, repeat navigation
  and duplicate-render protection at the owning boundary. Expect missing, duplicate
  or premature terminal records to fail before edits. Emit once per defined
  navigation outcome; preserve existing behavior and correlate existing commands.
  Test formatter/forwarding preservation in the same slice. Bound log volume and
  exclude sensitive values. This provides useful diagnosis without the lab runner.
- [ ] Apply the proven contract to the second route with its own data/action and
  cancellation scenarios. Reuse the same application logging path and document
  queries against actual emitted logs. Do not duplicate a lab-only event schema.
- [ ] Reuse/extract non-resetting Auth0/fixture setup only as needed for matched lab
  calibration. Test preservation of unrelated parties and cleanup limited to the
  owned named fixture. Ordinary E2E bootstrap deletes test-account parties and is
  unsuitable. Preserve configured AWS/SSM credentials and normal login; keep tokens
  in memory. Account for auth asset warming and coexistence with ordinary E2E runs.
- [ ] Collect the repeated baseline with existing Gradle measurement capabilities.
  Explain warm refresh, missing LCP and aborts using bounded experiments; preserve
  anomalous samples. No dedicated observer suite or normal timing CI gate. Add an
  interaction trace only for a concrete unanswered question; distinguish lab
  observations from field INP. Field Web Vitals expansion is not required here.
- [ ] Assess and, if safe, deliver one CDN deferral using the sequence below.
  Compare the application route signals alongside necessary browser evidence;
  publish queries and remaining limits for future investigations.

## Route-Scoped CDN Loading

`client/src/jsMain/resources/index.html` eagerly imports every configured CDN
namespace and assigns it to window. Meanwhile, emitted lazy route chunks have
real ESM imports. This suggests possible redundant startup loading of active
chart, clipboard or drag/drop libraries. It does not prove that removing every
namespace assignment is safe: audit globals, side effects, peers, GraphiQL and
first-use behavior first. Preserve inline/server-injected configuration timing.

The retained final Welcome baseline has 323 CDN module responses per load. Removing
unused entries reduced requests without local bundle growth; remaining packages
have active consumers. Do not apply a blanket CDN bundling or self-hosting change.

### Safe Slice Sequence

- [ ] Inventory generated startup/route imports and global/side-effect consumers.
  Choose one active route-only package group. Declare Welcome readiness and the
  affected action/route contract; consult current Modern Web Guidance.
- [ ] Add production-built browser evidence showing that the chosen package is
  absent from startup and loads when actually needed, while the feature works.
  Keep detailed component behavior below E2E. Confirm a meaningful failure before
  changing loading. A source-string test or lower request count alone is insufficient.
- [ ] Defer that group with existing module/route mechanisms. Retain import maps
  and externals where required so code is not simply rebundled locally. Preserve
  singletons and server/dev configuration; obtain approval for any visible changes.
- [ ] Compare all emitted JS including lazy chunks and network bytes/request counts
  under matched conditions. Validate first use, repeat use, and error/loading
  recovery on affected routes; retain before/after evidence and full-suite results.

Reuse the generated import map and runtime request inventory from
[CPL-52](CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md) where conditions match. Coordinate
script and header changes there if this work affects their compatibility. CSP
implementation is not a prerequisite for performance investigation, and CDN
deferral is not a prerequisite for CSP. Avoid blanket bundling or self-hosting changes.

## Resume Note

Start by inspecting existing runtime logs and identifying the first route's semantic
readiness boundary and actual collection coverage. Signed-in measurements and CDN
deferral remain pending. Do not replay CPL-54 or make a new measurement platform
an implicit prerequisite. Any shared emitter improvements reuse or update CPL-66's
evidence without introducing reciprocal dependencies.
