# CPL-66 — Diagnose runtime command performance from ordinary logs

**Work card protocol: 2**

## Goal and Constraints

Make existing application command/request logs sufficient for routine latency,
failure and cancellation investigation using standard log queries, without a
profiler, custom parser or dedicated performance runner. Reuse the same signals
for operational diagnosis, release comparisons and performance measurement.

Own runtime command timing, structured serialization and the minimum correlation
needed to follow an operation through existing client/SDK/server paths. Preserve
business behavior, cancellation/exception propagation and existing consumers.
Do not turn this into a new tracing platform, logging backend, dashboard project,
all-operation instrumentation sweep or mandatory deployment. No vendor dependency,
new ingestion endpoint or changes to retention/access policy are implied.

[CPL-5](CPL-5-TEST_SUITE_PERFORMANCE_IMPROVEMENT_TASK.md) owns test runtime
improvement; [CPL-62](CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md) owns semantic
route readiness and CDN assessment. This card delivers a useful runtime-log
contract independently. Either may ship a necessary shared change first; reuse its
committed contract and evidence rather than waiting or duplicating implementation.

## Starting Evidence and Ownership

- `libraries/action/.../ActionLoggingSyntax.kt` already emits action, phase,
  trace ID and successful-command duration. Exception/cancellation terminal events
  omit duration. A trace ID alone must not be assumed unique per command invocation.
- `libraries/logging/.../JsonFormatter.kt` currently converts payload values to
  strings. Test JSONL normalization is not proof that runtime logs retain numeric
  types. Inspect the actual emitted and collected shape before selecting a migration.
- Client `routing/PageProps.kt`, SDK request construction, server tracing/request
  middleware and command dispatch already carry correlation. Inspect and reuse
  those paths; establish actual propagation rather than infer it from field names.

One contributor owns a slice end to end. Shared command semantics belong to
`libraries/action`, serialization to `libraries/logging`, and request context to
existing SDK/server boundaries. Keep runtime/test consumers compatible in the same
slice. Consult architecture, testing and production-style guidance on resume;
use Testmints and current Modern Web Guidance where the implementation requires them.

## Acceptance Evidence

- Define a small documented event contract with operation name, invocation identity,
  trace/request correlation, phase, numeric monotonic elapsed milliseconds and
  terminal outcome (success/error/cancelled). Define nesting/concurrency semantics;
  parent linkage is required only where a stated query needs it. Reuse existing
  fields, documenting compatibility for additions or type changes.
- Every supported terminal path emits one terminal event with duration, including
  failure and cancellation. An interrupted process may have an unmatched start;
  queries must expose this instead of reporting success or zero duration.
- Preserve numeric/boolean fields through actual runtime formatting and the
  existing collection path. Existing downstream/test consumers continue to work.
  Identify log level, filtering, sampling, retention and volume limitations that
  affect coverage. Include available service/environment/release metadata through
  existing context, without assuming it already exists.
- Provide copyable `jq` and destination-native queries over ordinary records for
  counts, p50/p95 latency by operation/outcome/release where available, error and
  cancellation rates, missing terminal/duration coverage, and a correlated request.
  Specify time window, denominator and sample size. Do not mix client/server elapsed
  boundaries or sum overlapping child durations into request latency.
- Show sanitized records from representative success, error and cancellation flows
  through normal application execution. Validate one correlated client/request/server
  flow where collection is available. Console-only and server-only coverage must be
  labeled; centrally collected client telemetry is not assumed. If access or delivery
  is unavailable, record the exact gap and leave that acceptance item open.
- Keep payloads bounded and free of tokens, headers, bodies, user content and dynamic
  resource IDs. Use stable operation names. Record event volume for representative
  work and check for duplicate/error double counting using existing paths.
- Focused contract/consumer tests and `./gradlew check` pass per implementation
  slice. Normal CI/deployment health cannot alone prove log typing or completeness;
  verify these with actual records. Deployment still requires user authorization.

## Safe Slice Sequence

- [ ] Inspect existing emission, formatting, consumers and destinations. Retain a
  small sanitized inventory and queries showing the current gaps. Choose one
  representative operation and document the contract and compatibility strategy.
  Keep probes temporary; no maintained analyzer is needed to establish the gap.
- [ ] Complete command terminal timing in `libraries/action`. Before edits, test
  success, thrown error, cancellation and overlapping/nested invocations at the
  existing boundary. Expect absent duration or ambiguous/duplicate terminal events
  to fail. Measure elapsed time with a monotonic clock on all terminal paths,
  preserving results, original exceptions and cancellation semantics. Include
  required test-normalizer/validator consumers in this independently useful slice.
- [ ] Preserve structured runtime types in the existing logging formatter. Test
  numeric duration, booleans, strings, null/unsupported payload handling and existing
  message compatibility; expect current string coercion to fail the selected typed
  contract. Update affected consumers together. Demonstrate ordinary queries against
  emitted application records, not just normalized test JSONL. Do not introduce a
  second performance-only serializer or extend payload scope beyond need.
- [ ] Close the demonstrated correlation gap for one existing request path, if any.
  Test context preservation across asynchronous/concurrent work and isolation between
  requests; expect lost or leaked context to fail. Reuse current tracing and SDK
  headers, preserve trust boundaries and avoid raw URL/resource identifiers. If
  current propagation already suffices, retain evidence and make no redundant change.
- [ ] Verify actual destination coverage and publish queries in existing logging
  documentation. State supported boundaries, completeness, volume and remaining
  collection gaps. Use existing access and delivery evidence; if a new transport is
  necessary, resolve that concrete scope choice with the user before implementing.
  Do not hide an unavailable destination behind a synthetic acceptance report.

Each production slice follows the repository's call-shot, red/green where behavior
changes, focused validation, full-check and commit protocol. Do not invent a red
for investigation or documentation. Follow
[tooling purpose and lifetime](../context/GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime):
standard queries are the supported analysis path; retained automation requires a
concrete recurring need and an existing owner, expressed through Gradle where applicable.

## Resume Note

No implementation or deployment is claimed. Begin with actual runtime event shape
and collection coverage, then the smallest terminal-timing slice. Broad browser
field metrics and infrastructure changes remain outside this card's default scope.
