# Owner Values

Repository owner: RoB Murdock, software engineer since 2005. Consult when intent is
ambiguous or design choices compete; this is not mandatory startup reading.

- Product value and team effectiveness are the north star. Architecture and process
  serve the product and the people maintaining it.
- Safe incremental sharing is a core value: completed slices can be committed,
  pushed, and released while the larger feature remains in progress.
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
