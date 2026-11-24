# Owner Values

Repository owner: RoB Murdock, software engineer since 2005. Consult when intent is
ambiguous or design choices compete; this is not mandatory startup reading.

- Product value and team effectiveness are the north star. Architecture and process
  serve the product and the people maintaining it.
- Safe incremental sharing is a core value: completed slices can be committed,
  pushed, and released while the larger feature remains in progress.
- Design for continuous release. Migrations and operational transitions should be
  safe to repeat, resume and recover across releases; do not assume a one-time
  operation ran perfectly or that all callers switch versions together. Keep
  compatibility and catch-up capabilities until evidence supports their removal,
  then remove them deliberately. Match the mechanism to the actual risk rather
  than assuming high throughput or requiring uninterrupted operation by default.
- Tests are durable, readable expressions of intent. State the desired result
  before implementation; verify against reality and show meaningful evidence.
- Prefer clarity over cleverness, whole words, small composable functions, and
  intentional boundaries. Keep human review possible by limiting unnecessary output.
- Reuse established capabilities; avoid speculative abstractions and optimization
  before pain is visible. Duplication can be a temporary choice while semantics evolve.
- Understand why an unusual override exists before changing it (Chesterton's Fence).
  Investigate its history and constraints; surface uncertainty rather than blindly
  copying or removing it.
- Make assumptions and consequential tradeoffs visible early. Keep updates concise,
  concrete, and decision-oriented. Treat disagreement as design input.
- Match test depth to risk, boundary crossings, and user impact. Build/delivery
  systems are product capabilities and should remain understandable and executable.
- Minimize blast radius and favor reversible changes. Optimize integration flow,
  feedback speed, and team learning rather than activity counts or process theater.
- When blocked, explain the evidence and propose the best next path. Report outcomes,
  validation gaps, and material residual risks without exhaustive status inventories.

## Collaboration Metrics and Team Agency

Coupling anchors leadership and team decisions in the reality of what happened.
A primary reader is a team lead comparing observed collaboration with expectations
based on the team's own decisions and context. The observations might confirm those
expectations or be surprising enough to prompt questions. Interpretation and
coaching are useful; the app does not supply the verdict or require a response.
Stats and observations must never feel like chains dictating a team's decisions.

Trustworthy observations depend on people giving Coupling accurate information.
Tone and incentives are therefore part of data integrity: do not make honest
reporting feel penalized, or give people a reason to withhold, relabel or distort
information, or avoid appropriate work, to make the statistics look acceptable.
A design that weakens honest reporting undermines the evidence the app needs.

- Comprehensibility is a product requirement. People should understand what an
  observation or suggestion means, why it appears and where its limits are. Favor
  a clear mental model and concise, well-placed explanation over opaque
  sophistication or a wall of prose. Explain the data and model where useful;
  express respect for team agency through behavior and neutral wording rather
  than repeatedly announcing that teams have agency.
- Distinguish observations, comparison models and interpretation. Show actual
  counts, time windows, coverage and assumptions. An even split is a descriptive
  mathematical comparison, not the app's prediction, goal or standard of good work.
  Use precise language; “normal distribution” is a particular statistical model,
  not a synonym for even participation or desirable collaboration.
- Expectations belong to the people who know the team's decisions. Deliberate
  repetition, unequal pairing frequencies, pins, absences and membership changes
  can all be appropriate. A difference from a model alone does not establish
  productivity, engagement, collaboration quality, avoidance or knowledge isolation.
- Help people notice and investigate patterns. A lead might wonder whether pairing
  is being avoided, or notice two people have not worked together for a while and
  consider making space for that. Observations anchor that inquiry; explanations
  and suggested responses remain provisional and depend on the team's context.
- Apply this distinction to copy, visual emphasis and recommendations. Playful
  presentation is welcome; it must not turn observations into grades or obligations.
  Colors can show counts and make patterns visible. Approval badges, quality
  scores, warning labels or outlines around an app-selected numerical band can
  instead imply that one pattern is correct. Do not make contextual choices look like errors merely
  because they differ from an even split. If comparing against an explicit
  team-chosen goal, make the goal and its ownership clear.
- Suggestions solve a practical coordination problem: coming up with pairings can
  be an annoying chore. The default should provide a natural, understandable
  starting point that people can anticipate for themselves and explain. The normal
  pairing algorithm's nudge toward people who have not paired recently serves
  that purpose. Favor predictable, explainable defaults; opaque optimization is
  not a benefit by itself.
- Suggestions are freely overridable: no required justification, compliance
  penalty, or extra confirmation gate merely for choosing differently. A team
  conversation may be useful, but it belongs to the team; the app does not require
  proof it happened.
- Preserve the choice of whether and how to respond; team agency is a requirement,
  not permission granted by the app. A lead or team may investigate, change an
  arrangement, create opportunities to pair, or decide no action is appropriate. Supporting an informed decision is the product outcome; moving a
  metric toward a preferred shape is not an outcome by itself.

Read existing labels and work-card proposals through this principle. Historical
wording about “health,” “risk” or “ideal” patterns is not an endorsed interpretation;
resolve conflicting product intent with the owner before implementing it.
