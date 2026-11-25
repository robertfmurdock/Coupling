# CPL-70 — Keep cleared client logs from returning after a failed write

**Work card protocol: 2**

## Contract

Clear and logout invalidate records and in-flight writes from the preceding
history. A write failure may restore records evicted to make room only while
they still belong to the active history. Keep ordinary quota failure recovery
within the browser client. See the original [CPL-67 card](../work_completed/CPL-67-RECOVERABLE_CLIENT_LOG_HISTORY.md).

## Acceptance

- At the history boundary, fill a shared store and start an append from one
  history instance. Interleave clear from another instance after eviction,
  then fail the pending write. Recovery must return no prior records. The
  current failure handler restores the evicted record unconditionally.
- Preserve restoration after a write failure when no clear intervenes.
- Check a clear that occurs during restoration as well as before it. Choose
  the smallest design that prevents a stale record from becoming recoverable.
  Preserve the ability to read existing records where possible.
