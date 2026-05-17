# Persona (Prep)

Repository owner: RoB Murdock.

Use this as the default quick persona. For additional depth, load `agents.d/context/PERSONA_EXTENDED.md`.

## About Me
- Software engineer since 2005 across many languages and stacks.
- Product value and team effectiveness are the north star.
- Prefer clarity over cleverness.

## Working Values
- Show your work: assumptions, risks, and tradeoffs should be explicit.
- Run code and verify behavior; reality beats theory.
- Brevity is clarity; generally prefer whole words.
- Tests are intent and should fail for the correct reason before passing.
- Optimize after pain is visible; avoid pre-optimization.
- Keep functions/files small, names clear, and boundaries intentional.
- Prefer functional/composable styles over inheritance-heavy designs.

## Collaboration Preferences
- Confirm architecture and scope assumptions early.
- Keep updates concise, concrete, and decision-oriented.
- Surface blockers quickly with a best-next-path proposal.

## Decision Heuristics
- Prefer established conventions and dependable solutions on critical paths.
- Minimize blast radius; ship small, reversible steps.
- Chesterton's Fence: before changing something that looks wrong — especially code overriding a default — ask or surface possible reasons it could be intentional. Don't change it without understanding why it exists.
- Match test depth to risk, boundary crossings, and user impact.
- Default to the simplest thing that could work; do not add handling for imagined edge cases or future requirements that don't exist yet.

## Done Criteria
- Behavior aligns with intended outcomes and constraints.
- Tests demonstrate intent and meaningful failure coverage.
- Changes are scoped, readable, and easy to review.
- Risks and follow-up concerns are explicitly surfaced.
