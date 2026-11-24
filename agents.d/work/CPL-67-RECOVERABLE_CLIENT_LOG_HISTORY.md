# CPL-67 — Recover bounded client log history across browser sessions

**Work card protocol: 2**

## Goal and Constraints

Retain useful structured client logs locally in the browser for approximately
30 days, subject to a small explicit storage budget, and make them recoverable
without an automation session or custom parser. Support performance investigation,
correctness diagnosis and support after the original console session has gone.
Preserve existing console output and automated browser-log collection.

Thirty days is the maximum age and target history window, not guaranteed storage:
capacity eviction may shorten it. Keep storage, serialization and write costs
cheap enough for normal application use. This is browser-local recovery, not
central telemetry, cross-device history, guaranteed crash durability or automatic
upload. No backend ingestion, vendor SDK or general log-viewer product is implied.

Follow the [observability principle](../context/ARCHITECTURE_CANONICAL.md#observability)
and [tooling purpose and lifetime](../context/GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime).
The recurring consumer is a user/developer recovering recent client activity for
investigation. This is application functionality, not a new measurement runner.

## Ownership and Starting Points

One contributor owns each complete slice. Browser persistence and recovery belong
to `client/`; reuse `libraries/logging`'s structured event/formatting boundary without
making shared logging depend on browser APIs or changing Node/server behavior.
Inspect `InitializeLogging.kt`, `JsonFormatter.kt`, current browser forwarding and
the existing client build-version source before selecting the integration seam.

[CPL-66](CPL-66-APPLICATION_LOG_PERFORMANCE_OBSERVABILITY.md) owns general runtime
log quality; [CPL-62](CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md) owns route
readiness signals. Neither is a prerequisite. Persist current useful signals and
reuse any delivered contract improvements without creating a second event schema.

## Storage and Recovery Design Decisions

- Evaluate `localStorage` first as the suggested simple option, against measured
  serialized volume and write cost. It is synchronous and shared by same-origin
  tabs; avoid rewriting an entire month of history on every event. Use IndexedDB
  if asynchronous bounded appends/transactions are materially simpler or cheaper
  for the measured workload. Record the choice, current browser guidance, byte
  accounting and supported failure behavior before implementation; do not build
  multiple storage backends without a demonstrated need.
- Define a hard serialized-byte budget, maximum record size/count, bounded memory
  queue and write batching policy. Select concrete limits from representative
  emitted logs and origin storage use; record estimated bytes/day, achieved days
  at the cap and measured write/flush cost. Prefer discarding oldest eligible logs
  over affecting application data. Never evict unrelated origin storage.
- Expire records older than 30 days on access/startup and bounded maintenance while
  active. Browser code cannot promise deletion while the app is closed. Do not
  expose expired records during recovery. Document timestamp/clock-change behavior.
- Recover via a small supported application action that exports structured JSONL
  and exposes retained range, client versions, dropped/expired counts where known,
  and storage status. Provide clear-history access. No DevTools-only function or
  bespoke decoder is the sole recovery path. Share/export is user initiated; do
  not automatically send logs anywhere. Decide the minimal accessible UI placement
  within established visuals, following current Modern Web Guidance.

Browser storage is best effort: clearing site data, private browsing, quota limits,
browser eviction or abrupt shutdown may lose history. See current platform guidance
on [Web Storage](https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API)
and [quotas/eviction](https://developer.mozilla.org/en-US/docs/Web/API/Storage_API/Storage_quotas_and_eviction_criteria).
Do not request persistent-storage permissions by default merely for diagnostics.

## Acceptance Evidence

- Each retained event includes its emission timestamp and **client version at
  emission**, using the existing release/build version source. Capture this before
  enqueue/persistence; do not stamp all exported or buffered records with the
  current version. Preserve old and new versions together across an application
  upgrade, including queued writes. Export retains per-record version unchanged.
  Missing version is explicitly unknown, never inferred from the exporting build;
  define development/legacy-record behavior and expose its coverage.
- Preserve useful structured fields/types, operation outcomes, durations and
  correlation through persistence/export. Include a storage schema version for
  compatibility independently of client version. Bound unsupported/oversized event
  handling and report loss without recursive logging or a new event per dropped log.
- Demonstrate real application logs surviving reload and browser restart, mixed
  client versions after upgrade, expiry and oldest-first capacity eviction. Export
  must be directly queryable with standard tools; include queries filtering by
  client version, time, operation and outcome and locating correlated activity.
- Persistence denial/quota exhaustion, corrupt or incompatible stored data, write
  failures and competing same-origin tabs do not break application actions or
  existing console logging. Define concurrency and recovery semantics so one tab
  cannot silently overwrite another's history. Handle partial buffered loss on
  abrupt closure honestly; recovery metadata states available coverage.
- Define an allowlist/redaction boundary before writing durable records. Exclude
  tokens, headers, bodies, user content and sensitive identifiers, including from
  freeform messages and exception text. Do not persist the console wholesale.
  Define account-switch/logout behavior for shared browsers; clear prior account
  history by default unless a documented non-sensitive alternative is justified.
  Do not store authentication identifiers just to partition diagnostics.
- Demonstrate the chosen byte/memory caps and representative event-volume/write
  costs with bounded experiments. Compare normal application interaction before
  and after; storage logging must not become a new performance bottleneck. Record
  the loss/retention tradeoff rather than silently relaxing budgets to reach 30 days.
- Focused owning-boundary tests, production browser integration and `./gradlew check`
  pass per implementation slice. Existing CI health does not prove retained history,
  version fidelity or storage cost; retain bounded evidence for those outcomes.
  Follow the work-card visual-evidence protocol for any visible recovery controls.

## Safe Slice Sequence

- [ ] Inspect current log fields, client-version source, destination and representative
  volume. Retain sanitized findings and choose limits, storage mechanism, event
  allowlist, logout policy and minimal recovery action. Keep probes temporary; no
  maintained benchmark framework. State exact contracts/scenarios before tests.
- [ ] Deliver bounded persistence plus usable export/clear in one independently
  useful slice, with client version captured per event from the start. At the
  owning boundary, test round-trip types/version, expiry, cap eviction, oversized
  records, redaction, denied storage and write failure. Expect absent history,
  incorrect version stamping or unbounded storage to fail before production edits.
  Wire the existing logger once, preserving console and server/Node behavior.
  Include the minimal accessible recovery control and full consumer changes here;
  do not ship an accumulating store users cannot inspect or clear.
- [ ] Harden cross-session/upgrade and concurrent-tab recovery as a complete slice.
  Test mixed-version records and delayed writes across upgrade, schema compatibility,
  corrupt data, tab interleaving, logout/account switch and clear versus queued
  writes. Expect overwritten history, relabeled versions or repopulation after
  clear/logout to fail. Keep any initial concurrency limitations safe and explicit;
  do not ship known cross-account leakage in an earlier slice.
- [ ] Verify real-browser restart, actual emitted-log export and measured cost under
  the selected caps. Use existing application/browser tests and Gradle capabilities;
  do not simulate waiting 30 days when a controlled clock proves expiry. Publish
  standard queries and concise recovery/retention instructions, including oldest
  available event, gaps and best-effort durability. Remove experimental code.

## Resume Note

The first implementation slice is complete below. Remaining work is listed in the
unchecked checklist, particularly restart/browser evidence and measuring synchronous
storage cost under representative user activity.

## Investigation Update — 2026-11-24

- Client logging is initialized in `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/App.kt` with `libraries/logging`'s `JsonFormatter`. The structured record contains level, logger name, message/payload, timestamp, marker, and throwable messages. JSON formatting is the console path; its output should not be persisted wholesale without deliberate field policy.
- The release/build version source is `rootProject.version`, used by `client/build.gradle.kts` for the deployed asset path. It is not currently injected into runtime client config. The client bundle should receive this same value at build time; blank/development values are represented as unknown.
- The main structured client signal is `ActionLoggingSyntax` in `libraries/action/.../ActionLoggingSyntax.kt`: action name, phase, random UUID trace correlation, successful numeric duration, error/cancellation outcome, and exception. Trace IDs are random correlation values, not account identifiers. Per the product decision, exception messages remain useful diagnostic data; if a source exception contains secrets, fix the source. Arbitrary logger messages and payloads still need review for actual user/auth/request data before durable storage.
- The browser client routes application actions through `LoggingActionPipe` from `client/src/jsMain/.../routing/PageProps.kt`. It does not install the DynamoDB repository logger, so repository log paths are outside this work card's client boundary. Normal action records contain action class, start/end phase, random trace UUID, successful duration, outcome flags, and exception details. Exception text and trace IDs are retained as requested; no actual sensitive client event field was found in this path.
- Action lifecycle logs emit two records per completed action, with an additional end/error record on failure. Client-wide event rate and storage-write latency are not measured, so the caps are conservative bounds rather than a retention-days guarantee.
- `Logout` currently invokes Auth0 without local cleanup. Shared same-origin history would cross account transitions unless cleared on logout. The default first-slice policy should clear history on logout, without storing account identifiers.
- Current MDN guidance: Web Storage operations are synchronous and can block the main thread; IndexedDB is asynchronous, append-friendly and transaction-based. History is best effort and browser quota/eviction still applies. The implementation uses localStorage for the first bounded slice; measured write cost remains open and may motivate moving to IndexedDB. Sources: [MDN Web Storage](https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API), [MDN storage quotas and eviction](https://developer.mozilla.org/en-US/docs/Web/API/Storage_API/Storage_quotas_and_eviction_criteria), [MDN IndexedDB](https://developer.mozilla.org/en-US/docs/Web/API/IndexedDB_API).
- A representative action record serialized to roughly 745 bytes, plus an approximately 88-byte key (about 833 bytes total). At 1 MiB that is roughly 1,258 records, or 629 completed two-record actions, before failures and metadata. This is a size estimate, not a real client event-rate or storage-write-latency measurement.

### Original first-slice proposal

### Implemented first slice — 2026-11-24

- Captures only `ActionLogger` events at the browser client logging boundary. DynamoDB is not started or installed in the client. The existing console appender still receives events; action structured fields now also populate the logging event payload while preserving the existing formatted console message and JSON `Message.properties` shape.
- Persists a versioned `client-log-event` record at emission with build version, timestamp, level/message, typed allowlisted action fields, trace ID, duration/outcome, and exception details. Arbitrary payload fields are filtered before persistence. Exception text is retained as agreed; unsafe exception content should be fixed at its source.
- Uses origin-local `localStorage`, with a 1 MiB combined serialized-record-and-key cap, 2,000-record cap, 8 KiB per-record cap, and 30-day expiration. Oldest records are evicted first. Writes are immediate and synchronous; failures are swallowed at the logging boundary and exposed through status/drop counts. This remains best-effort browser storage and may be evicted by the browser or lost with site data. Clock rollback can delay expiry until wall time advances; future-dated records remain until they age past the retention window.
- Clear is available in About and runs at logout. An epoch prevents a stale tab from repopulating history after clear. A stale tab reports that reload is needed. Export is user-triggered JSONL with a metadata line and event lines; counters explicitly cover only the current document, not other tabs or earlier sessions.
- About includes `jq` examples for version, time, operation/outcome, and trace ID. Development, blank, and snapshot versions are labeled `unknown`.
- TestMints browser tests cover version/type preservation, emission fields and types, expiry, byte eviction, oversized records, unrelated storage, denied writes, write rollback, corrupt records, clear across tabs, safe payload projection, reopened history, and accessible controls. The focused client browser test and action-library JS test pass; production Vite bundle generation passes.
- Real-device write latency and sustained event rates have not been measured. The approximate serialized-size calculation above suggests about 629 completed action sequences at the byte cap before failures; it is not a retention-days guarantee. The synchronous write cost remains a known limitation to measure before making a performance claim.
