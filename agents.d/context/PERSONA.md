# Owner Values

Repository owner: RoB Murdock, software engineer since 2005. Consult when intent is
ambiguous or design choices compete.

- Architecture and process serve product value, team effectiveness and maintainers.
- Deliver small, useful slices that can be released while larger work continues.
- Design for continuous release. Make migrations and operational transitions safe
  to repeat, resume and recover across releases and overlapping application
  versions. Retain compatibility and catch-up until evidence supports removal.
- Write readable tests that express intent. State the desired behavior before
  implementation and verify it at the affected boundary. Match depth to risk.
- Prefer clear names, whole words, small composable functions and explicit ownership.
- Reuse established capabilities. Add abstractions and optimizations for demonstrated
  needs; allow temporary duplication while semantics evolve.
- Investigate unusual overrides before changing them. Understand their history and
  constraints; surface unresolved assumptions.
- Share consequential assumptions and tradeoffs early. Treat disagreement as design
  input. Report decisions, outcomes, validation gaps and concrete blockers concisely.
  Omit self-evaluation, process narration and repeated assurances.
- Favor reversible changes and a small blast radius. Optimize feedback, integration
  and learning. Keep build and delivery systems understandable and executable.

## Collaboration Metrics and Team Agency

Coupling helps team leads compare observed collaboration with expectations grounded
in their team's decisions and context. Observations support inquiry and coaching;
people decide what they mean and whether to act.

Accurate reporting is essential. Wording, visuals and incentives must leave people
free to report what happened without pressure to withhold, relabel or distort it,
or avoid appropriate work to improve a statistic.

- Make observations and suggestions understandable. Explain what they mean, why
  they appear and their limits where that helps a decision. Use concise copy and
  neutral wording; let controls and behavior express team agency without repeated
  assurances, disclaimers or narration of the app's intentions.
- Distinguish observations, comparison models and interpretation. Show counts,
  time windows, coverage and assumptions. Label an even split as a mathematical
  comparison, never an implied goal or standard of good work. Reserve “normal
  distribution” for that specific statistical model.
- Ground interpretation in team context. Deliberate repetition, unequal pairing
  frequencies, pins, absences and membership changes can all be appropriate.
  Deviation from a model alone establishes neither productivity, engagement,
  collaboration quality, avoidance nor knowledge isolation.
- Help people investigate concrete patterns, such as two people who have not paired
  recently. Keep explanations and suggested responses provisional.
- Keep presentation playful. Use color to reveal counts and patterns. Avoid
  app-assigned grades, approval badges, quality scores, warning labels or numerical
  bands that portray contextual choices as mistakes. When comparing against a
  team-chosen goal, identify the goal and its ownership.
- Make pairing suggestions a predictable, explainable starting point for a practical
  coordination chore. The normal algorithm's nudge toward people who have not paired
  recently serves this purpose. Add complexity only for a demonstrated benefit.
- Allow free overrides without justification, penalties or extra confirmation.
  Teams own any conversation about their choices. Investigating, changing an
  arrangement and taking no action are all valid responses. Success is an informed
  decision; a metric moving toward a preferred shape alone does not establish it.

Historical labels such as “health,” “risk” or “ideal” may conflict with these values.
Resolve conflicting product intent with the owner before implementing it.
