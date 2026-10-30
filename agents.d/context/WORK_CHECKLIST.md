# Work Cards and Safe Slices

Follow the contribution protocol in [AGENTS.md](../../AGENTS.md). Use a card in
`agents.d/work/` for a multi-commit feature or a handoff. A small single-slice change
can keep its design statements in the conversation instead.

## Call the Delivery Shot

Before implementation, state the intended outcome and an ordered sequence of
independently releasable slices. For each slice, declare:

- The meaningful behavior or capability it establishes.
- The tests that express the intent and their expected failure.
- The proposed contract, production design, and affected boundaries.
- Why releasing this slice before later slices is safe.

Prefer small complete capabilities. A slice must remain safe and useful indefinitely
if work stops. Include required cross-layer changes together; splitting by technical
layer is inappropriate when those layers cannot ship independently. Use compatibility
or feature gating only when needed for a safe release; avoid speculative scaffolding.

Update the sequence before material design or slice-boundary changes. Before test
edits and again before production edits, visibly call the next shot as described in
AGENTS.md; a card does not replace those timely updates.

## Commit and Handoff

Each slice ends with a review and a passing `./gradlew check`, followed by a commit
containing its tests and implementation. Record validation and the commit before
starting the next slice. Never commit a failing red phase. Pushing or releasing
follows the user's authorization; neither is required to record a completed slice.
If validation is blocked, record the blocker and leave the slice incomplete.

Before handing off or pausing, record the current commit, uncommitted work, remaining
slices, blockers, and relevant paths. On resume, verify git state and whether the
recorded validation still applies. Avoid rerunning unchanged successful checks unless
new evidence or the current working state requires it.

Review the full result against the goal at completion, then move the card to
`agents.d/work_completed/`. Keep notes to decisions and evidence, not a transcript.

## Template

```markdown
# Outcome

## Goal and Constraints
Intended result, scope, and compatibility requirements.

## Safe Slice Sequence
- [ ] Slice: meaningful capability; test intent and expected failure;
      proposed contract/design/boundary; why independently releasable.

## Design Decisions
Material discoveries and revisions to the sequence or contracts.

## Current State
Date, current commit, uncommitted changes, next slice, blockers, handoff paths.

## Validation
Commands, outcomes, and gaps per slice; completed commit IDs.
```
