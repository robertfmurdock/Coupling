# Persona (Extended)

Repository owner: RoB Murdock.

Load this when work has higher ambiguity, architectural risk, conflicting constraints, or collaboration friction. For normal bootstrap, use `agents.d/context/PERSONA.md`.

## About Me
- Software engineer since 2005 across many languages and stacks.
- Product value and team effectiveness are the north star.
- Comfortable with deep technical detail, but prioritize clarity over cleverness.
- Appreciate practical creativity and occasional light whimsy, as long as it serves communication.

## Working Values
- Show your work: make assumptions, tradeoffs, and risks explicit.
- Reality beats theory: run code, verify behavior, and measure outcomes.
- Tests are a primary expression of intent; they should fail for the right reason before they pass.
- Optimize after friction is visible; avoid premature optimization.
- Keep functions and files small, names clear, and boundaries intentional.
- Prefer functional/composable styles over inheritance-heavy designs.
- Duplication is usually a smell, but can be a strategic stepping stone when evolution is still unclear.
- Build and delivery systems are part of the product capability; keep them understandable and executable.

## Collaboration Preferences
- Confirm architecture/scope assumptions early, especially when they affect boundaries.
- Keep updates concise, concrete, and decision-oriented.
- Surface risks early with options and impact, not just warnings.
- When blocked, propose the best available next path immediately.
- Treat disagreement as design input; resolve through evidence, constraints, and user value.
- Make problems visible so the team can help solve them.

## Decision Heuristics
- Prefer established conventions unless measurable gains justify divergence.
- Choose boring, reliable solutions on critical paths.
- Minimize blast radius and favor small, reversible steps.
- Match test depth to risk, integration boundaries, and user-facing impact.
- Design seams around product concepts and ownership clarity, not org-chart convenience.
- Favor changes that improve integration flow, feedback speed, and team learning loops.
- Default to the simplest thing that could work; resist adding complexity for imagined edge cases or features that don't have a concrete near-term need.

## Anti-Goals
- Unwieldy build or release logic that increases cognitive overhead.
- New abstractions without clear near-term payoff.
- Metrics that do not support concrete decisions.
- Process theater ("activity without achievement").

## Done Criteria
- Behavior aligns with intended outcomes and stated constraints.
- Tests capture intent and include meaningful failure coverage.
- Changes are scoped, readable, and easy to review.
- Cross-boundary impacts are addressed (or explicitly deferred with rationale).
- Risks, follow-ups, and operational concerns are surfaced clearly.
