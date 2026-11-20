# Work Cards and Safe Slices

Use a work card for multi-commit work or a handoff. For a small single-slice
change, state the design in the conversation. Follow the contribution protocol
in [AGENTS.md](../../AGENTS.md).

## Open a Card

In `agents.d/work/`, name the file `CPL-N-<description>.md` and start the title
with `# CPL-N — Specific outcome`. For new or substantively revised cards, put
`**Work card protocol: 2**` below the title.

Allocate the next number above the highest in `work/` and `work_completed/`.
Coordinate allocation with other contributors. The ID and filename stay with
the card when it moves.

## Define Done and Plan the Slices

- **Goal and constraints** cover the outcome, scope, and compatibility needs.
- **Acceptance evidence** states what someone can observe to verify the outcome.
- **Safe slice sequence** gives the delivery order. For each slice, name the
  behavior, owning boundary, proposed design, test and expected failure for
  behavior changes, and why the slice can ship independently. Include any
  cross-layer changes needed to make it work on its own.

Use headings that fit the task. Link shared procedures and record decisions that
affect scope, design, or validation.

## Record Visual Evidence

Capture the affected state before editing when a visual change is expected. If
the work confirms a visual change, open one GitHub issue for the card and link
it under **Visual evidence** on the card. Include the card ID and a link to the
card in the issue. Cards without a confirmed visual change do not require an issue.

Post labeled **Before** and **After** screenshots in the issue for each visual
slice. Match the viewport, theme, and sample data where practical, and include
the implementation commit SHA. Add short clips when motion or timing matters.
Reuse the same issue for all captures related to the card.
If reviewing the affected states confirms there is no visible change, you can
omit before-and-after screenshots.

## Deliver and Close

1. State the slice plan before tests, then state the implementation approach
   after confirming the expected failure and before editing production code.
   Update the plan when the design or slice boundary changes materially.
2. Review test quality and implementation design separately. Resolve findings,
   pass `./gradlew check`, and commit the slice before starting the next one.
   Record meaningful validation evidence, gaps, and the implementation commit.
3. If paused or blocked, keep the card active and record the blocker, relevant
   uncommitted work, and next action. On resume, verify Git state and whether the
   recorded checks still apply.
4. When the result meets the goal and acceptance evidence, move the card to
   `agents.d/work_completed/` and update inbound links. If the card has a visual
   evidence issue, verify the screenshots are present, update the issue's link
   to the completed card, and close the issue as part of closing the card.
   For consolidation, move the superseded card there as a redirect to the
   active owner. Push or release only with user authorization.
