# Work Cards and Safe Slices

Use a card for multi-commit work or handoffs; declare a single-slice design in the
conversation. Follow [AGENTS.md](../../AGENTS.md) for delivery and validation.

## Open a Card

Create `agents.d/work/CPL-N-<description>.md` with title
`# CPL-N — Specific outcome`. New or substantively revised cards put
`**Work card protocol: 2**` below the title.
Allocate above the highest number in `work/` and `work_completed/`; coordinate
with other contributors. Keep the ID and filename when moving the card.

## Define Done and Plan the Slices

- **Goal and constraints:** outcome, scope, and compatibility requirements.
- **Acceptance evidence:** observable proof of completion, favoring objective
  checks already produced by normal delivery. State which outcome each check
  covers and any essential behavior it cannot establish.
- **Safe slice sequence:** delivery order and each slice’s behavior, owner, design,
  and independent release value. For behavior changes, name test scenarios and
  expected failure. Include required cross-layer changes in the same slice.

Evidence requirements do not authorize new maintained tooling. Apply
[tooling purpose and lifetime](GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime)
when a slice proposes investigation scripts, harnesses or automation; state the
recurring need and owner, or keep the experiment temporary and retain findings.

Choose useful headings. Link shared procedures; record decisions affecting scope,
design, or validation.

Describe feature incubation in terms of the shared flag being on or off, then
unconditional behavior where relevant. Follow the [application configuration
contract](ARCHITECTURE_CANONICAL.md#build-and-deployment); do not plan named
environment rollout steps or introduce environment-specific feature configuration.

## Keep the Writing Focused

Write for the next contributor deciding what to build, why, and how to know it
works. Include the product contract, relevant findings, consequential decisions,
delivery slices, evidence and concrete blockers or next actions. Scale detail to
the work; a small feature does not need an exhaustive implementation manual.

Omit commentary about writing or reviewing the card: what was rewritten, which
standards were consulted, why headings or filenames were retained, or how the
proposal was modernized. Record the resulting decision and its reason when they
affect the work, rather than narrating the editing process.

Link shared delivery, testing, approval and closeout procedures instead of
restating them. Include task-specific constraints or exceptions where needed.
Keep each contract or evidence requirement in one place; avoid repeating it in
the goal, slice plan and acceptance list. Retain concise status and handoff facts
that change the next action, without a running diary of routine activity.

## Keep Cards Independently Deliverable

Prefer one card per independently deliverable outcome. Link related cards for
context rather than maintaining an active umbrella that only duplicates their
status. An umbrella is justified only when it owns a concrete integration or
acceptance outcome that no other card owns; state that outcome explicitly.

Minimize or avoid co-ownership of a card. Give each card a clear primary outcome
and one contributor responsible for carrying its current slice through completion.
That contributor may make all changes needed for the outcome across module and
layer boundaries. Focused investigation or review can help without dividing
implementation into reciprocal handoffs.

Different cards may need the same change on the way to different primary outcomes.
Either contributor may deliver it first; the other reuses the committed change
and records the applicable evidence. Overlap alone does not require exclusive
ownership, a shared prerequisite card, or waiting for the other contributor.
Coordinate concurrent edits when needed, but avoid plans where each card waits
for the other to supply part of its implementation. If that split is unavoidable,
keep the dependent work together under one owner or state a concrete prerequisite
as described below.

Minimize ordered work across cards. A requirement to finish another card first
is a last resort, not the default planning structure. Prefer a smaller useful
outcome, an existing contract, or keeping tightly dependent slices in one card.
Do not introduce speculative abstractions or duplicate implementations merely
to remove an ordering dependency.

When a cross-card prerequisite is unavoidable, name the exact missing capability,
why the outcome cannot safely ship without it, and the observable condition that
unblocks the card. A preferred sequence or shared topic alone is insufficient.
On resume, remove satisfied prerequisites rather than preserving obsolete gates.

## Prefer Existing Delivery Evidence

For changes whose failures would be detected by normal delivery, pushed
implementation commits, a passing build for those commits, and a successful
deployment with the site still live are strong completion evidence. Prefer
existing CI, deployment and health results; record commit/run links and the
relevant health observation rather than creating follow-up verification work.
Use only the signals relevant to the change: a build-only refactor need not
acquire a deployment requirement. Never infer a passing build or live site
merely from elapsed time.

Define additional acceptance checks only for outcomes those signals cannot
establish, such as authorization boundaries, data correctness, accessibility,
visual behavior or measured performance. Name that gap concretely. A live site
alone does not establish those outcomes. Prefer a bounded check with an explicit
pass condition over an open-ended review or investigation.

When closing older cards, reconcile stale checklist items against subsequent
delivery evidence. Do not replay completed work or keep a card open solely for
obsolete plans or administrative steps. Record the evidence or explicit owner
disposition and archive the card; leave historical gaps labeled accurately.
These closeout rules do not replace the full-check gate for new implementation
slices or authorize pushing or releasing.

## Record Visual Evidence

Capture affected states before visual edits. For a confirmed visual change, open
one GitHub issue with the card ID and card link, then link it under **Visual
evidence** on the card. Reuse that issue for every visual slice. No visible change
means no evidence issue or screenshot pair is required.

Use `gh` to create, update, and close issues. Upload labeled captures with the
CLI’s attachment support, for example:

```sh
gh issue comment <issue-number> --body 'Implementation: <commit-sha>' \
  --attach './before.png#Before — desktop' \
  --attach './after.png#After — desktop'
```

Record the implementation SHA and actual image and viewport dimensions; a requested
window size does not prove the viewport size. Match viewport, theme, and sample data
where practical; disclose differences. Add clips when motion or timing matters.
Verify uploaded attachments before closing the issue. Use the opt-in
[`captureVisualEvidence` task](../../e2e/visual-evidence/README.md) for named local
fixtures, labeled PNGs and manifests with measured viewport/image dimensions.

## Resume, Deliver, and Close

1. On resume, inspect Git state, prior commits, tests, and recorded evidence.
   Reuse applicable red/green evidence under [Testing](TESTING.md#resuming-an-inherited-implementation);
   record gaps rather than replaying completed work.
2. State the slice plan before tests and the implementation approach after the
   expected failure, before production edits. Revise materially changed plans.
3. Review tests and production design separately, resolve findings, pass
   `./gradlew check`, and commit before starting another slice. Record validation,
   gaps, and the implementation commit.
4. If paused or blocked, leave the card active with the blocker, uncommitted work,
   and next action.
5. Once acceptance evidence is complete, move the card to `agents.d/work_completed/`
   and update inbound links. Verify visual uploads, update the issue’s card link,
   record the implementation SHA, and close the issue. For consolidation, move the
   superseded card there as a redirect to its active owner. Push or release only
   with user authorization.
