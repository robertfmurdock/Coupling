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
- **Acceptance evidence:** observable proof of completion.
- **Safe slice sequence:** delivery order and each slice’s behavior, owner, design,
  and independent release value. For behavior changes, name test scenarios and
  expected failure. Include required cross-layer changes in the same slice.

Choose useful headings. Link shared procedures; record decisions affecting scope,
design, or validation.

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
