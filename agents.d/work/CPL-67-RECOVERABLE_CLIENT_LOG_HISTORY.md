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

Planning only; no persistence or recovery UI has been implemented. Start with the
existing client-version source and emitted event volume. Target 30 days when cheap,
with a firm small storage cap taking priority. Validate `localStorage` rather than
assuming it is the best medium for this log volume.
