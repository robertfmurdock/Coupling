# CPL-63 — Load route-only CDN dependencies on demand

## Goal and Constraints

Investigate remaining eager startup imports after
[CPL-54](../work_completed/CPL-54-WEB_PERFORMANCE_MEASUREMENT_FOLLOWUP.md), then
ship one measured, independently safe loading improvement. Prefer playing after
[CPL-62](CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md) supplies a stable signed-in
baseline. Preserve visuals, game-show energy, shared React identity and all actions.
Coordinate script/header changes with [CPL-52](CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md).

## Evidence and Starting Points

`client/src/jsMain/resources/index.html` eagerly imports every configured CDN
namespace and assigns it to window. Meanwhile, emitted lazy route chunks have
real ESM imports. This suggests possible redundant startup loading of active
chart, clipboard or drag/drop libraries. It does not prove that removing every
namespace assignment is safe: audit globals, side effects, peers, GraphiQL and
first-use behavior first. Preserve inline/server-injected configuration timing.

The retained final Welcome baseline has 323 CDN module responses per load. Removing
unused entries reduced requests without local bundle growth; remaining packages
have active consumers. Do not apply a blanket CDN bundling or self-hosting change.

## Safe Slice Sequence

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

## Completion and Validation

End after one justified group is safely deferred and its startup/first-use tradeoff
is measured, or after investigation rules it out with evidence. Further groups get
explicit slices rather than an unbounded app loading redesign. Run focused checks,
full `fish -lc './gradlew check --no-daemon'` outside the sandbox, then commit tests
and implementation together. This is a draft investigation/implementation card;
no loading change or performance improvement is claimed yet. No push/release.
