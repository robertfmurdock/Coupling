# Jev / TypeSafe AI Research

Researched: 2026-09-18

## Purpose

Record the potential fit of TypeSafe AI's Jev for Coupling. This is a product
research note, not an adoption decision or an implementation plan.

## What Jev is

Jev is TypeSafe AI's early-access "System One" model. It evaluates application
state against questions with a bounded answer shape rather than generating
prose. The public primitives are:

- **Choice**: select one known option, returning the choice, its full
  probability distribution, and confidence.
- **Score**: select a position on an ordered, defined rubric, also with a
  distribution and confidence.
- **Noul**: return the probability that a clearly defined yes/no statement is
  true.

Independent questions over the same state can be batched in one request. The
questions are evaluated independently; code must combine their results. The
design is therefore a better fit for bounded semantic judgments than for
writing, explanation, open-ended planning, or multi-step reasoning.

## Coupling context

Coupling automatically rotates pairs based on pairing history, with manual
overrides and Slack/Discord integrations. It also records contribution and
pairing-history data, including cycle time, ease, story, labels, commits, and
participant information. Existing statistics and visualizations make team
collaboration patterns visible.

The core pairing rule must remain deterministic and reviewable: pair people who
have been apart longest, breaking equal candidates randomly. Jev may provide
bounded input to a decision, but it must not silently replace that policy.

## Five opportunities

### 1. Natural-language pairing constraints

Allow a coordinator to express a temporary intent, such as pairing a teammate
with somebody who knows a deployment area, or respecting a short-term working
preference. Jev would score the finite candidate pairs against the declared
intent. Coupling would continue to enforce roster eligibility, fairness, and
the normal rotation algorithm.

**Guardrail:** display the constraint and recommendation; retain manual
override. Do not make AI-derived constraints permanent policy without explicit
human action.

### 2. Slack and Discord pairing-command interpretation

Interpret messages such as "Sam is out this week", "Pat is mornings only", or
"make an exception for the payments handoff" as a typed intent and proposed
existing action. Present a preview and require confirmation before invoking
Coupling's canonical command or mutation path.

**Why start here:** the output space is known, the user sees the result, and
there is no need to let the model perform a write directly.

### 3. Team-health conversation starters

Evaluate narrowly defined, team-level signals from existing contribution and
pairing data: possible pairing silo, sustained change in cycle time, uneven
participation, or insufficient evidence. Turn the signals into an
evidence-linked prompt for a coach to investigate.

**Guardrail:** this is for collaborative inquiry, never individual ranking,
performance judgment, or automated personnel decisions. Always preserve raw
metrics and allow an explicit insufficient-evidence outcome.

### 4. Assisted contribution intake and tagging

Classify imported PR, commit, or ticket data into Coupling's known labels;
detect probable missing pairing participants; and check whether a story
reference appears present. This improves downstream visualizations without
requiring perfect manual metadata.

**Guardrail:** assemble candidate values deterministically from source systems
and ask Jev to select or flag uncertainty. Do not have it invent a story,
person, date, or other durable record.

### 5. Confidence-gated engineering triage

Add a non-blocking development report that categorizes CI/test evidence into
known buckets: probable flaky test, compile/API-contract failure,
dependency/toolchain problem, browser/E2E environment problem, or unknown. A
second use could flag a likely incomplete GraphQL change across schema,
resolver, SDK, and tests.

**Guardrail:** suggestions do not replace tests, static checks, code review, or
the repository's GraphQL synchronization requirements. Unknown must remain a
first-class result.

## Recommended first experiment

Prototype opportunity 2 as a server-side, confirmation-first Slack/Discord
workflow. Define a small fixed intent vocabulary and evaluate it using
sanitized fixtures from real command shapes. Measure intent accuracy,
confidence calibration, fallback frequency, latency, and cost before expanding
the vocabulary. Existing canonical mutations remain the only path that changes
party data.

## Integration boundaries

- Keep the TypeSafe API key server-side. Never expose it to the Kotlin/JS
  client, browser source maps, URLs, or logs.
- Coupling is Kotlin/JVM plus Kotlin/JS. TypeSafe's publicly documented SDKs
  are JavaScript/TypeScript and Python, so use a small server-side HTTP adapter
  unless an official Kotlin SDK appears.
- Model output is type-safe, not factually correct. Validate performance and
  confidence thresholds on Coupling-specific representative data.
- Treat provider speed, cost, calibration, and workflow-performance claims as
  early-access vendor claims until reproduced for the intended workload.
- Send only the minimum necessary state. In particular, avoid exposing private
  team information for a convenience feature without a clear data-handling
  decision.
- Conservative thresholds should route uncertainty to a preview, clarification,
  or a human—not an automated write.

## Sources

- TypeSafe AI, [Introducing System One Models & Jev](https://typesafe.ai/blog/introducing-system-one-models-and-jev), accessed 2026-09-18.
- TypeSafe AI, [Introduction](https://docs.typesafe.ai/introduction), accessed 2026-09-18.
- TypeSafe AI, [Primitives (Questions)](https://docs.typesafe.ai/primitives), accessed 2026-09-18.
- TypeSafe AI, [Confidence](https://docs.typesafe.ai/confidence), accessed 2026-09-18.
- TypeSafe AI, [Patterns](https://docs.typesafe.ai/patterns), accessed 2026-09-18.
- TypeSafe AI, [official JavaScript SDK](https://github.com/typesafe-ai/typesafe-sdk-js), accessed 2026-09-18.

## Revisit triggers

Reassess this note before implementation if TypeSafe exits early access, changes
its API/model contract or pricing, publishes a Kotlin SDK, or Coupling obtains
an evaluated command corpus and data-governance requirements.
