### Boundary Check Protocol

This strategy identifies code that violates the module ownership rules defined in
`agents.d/context/ARCHITECTURE_CANONICAL.md` (Module Ownership Rules section).

#### Discovery

1. Read `agents.d/context/ARCHITECTURE_CANONICAL.md` — focus on the Module Ownership Rules section.
2. Scan the focus module for patterns that violate these rules:
   - **Client violations:** server-side policy embedded in `client/` code; direct use of server
     implementation details (not contracts); server/schema/sdk changes driven by a client-only concern.
   - **Library violations:** app-specific policy or single-consumer abstractions in `libraries/`; shared
     abstractions added before multi-module demand was demonstrated.
   - **SDK violations:** dispatcher mappings with silent fallback behavior that masks schema/contract drift.
   - **Test placement violations:** tests that verify internals at the wrong architectural level (e.g., a
     high-level behavioral test placed at unit level, or a cross-boundary concern tested only internally).
3. For each candidate violation, record it as `queued` in cleanup-history.md before investigating it.

#### Investigation

For each queued violation candidate:
- Confirm the violation is real (not just naming similarity): trace the import/usage to verify it crosses
  the ownership boundary stated in `ARCHITECTURE_CANONICAL.md`.
- Determine the action path:
  - If the offending code is also dead (no consumers): treat as dead code — use `tcr-delete.sh`.
  - If the offending code is live but locally contained: move it to the correct owner module, then run
    `./gradlew check -q --console=plain 2>&1 | tail -100` to validate. Only proceed if all of the
    following are true: (a) the move touches ≤ the run's `__MAX_FILES__` file limit, (b) no behavior
    changes are required, (c) all tests pass after the move.
  - If the scope is too large or behavior changes would be required: record as `boundary-violation-queued`
    and stop — do not attempt a partial fix.

#### History Verdict Shape

```
- <FileName.kt>: boundary-violation-fixed — <one-line description of what was moved/deleted and why>
- <FileName.kt>: boundary-violation-queued — <one-line description of violation; too large to fix this run>
- <FileName.kt>: verified-clean — no boundary violation confirmed after investigation
```

#### Limits

- Maximum violations investigated per run: **3**.
- Do not attempt moves that require changes to public API signatures or GraphQL schema.
- Do not attempt moves across `client/` ↔ `server/` boundaries; these require explicit cross-layer planning.
