# Code Style Playbook

Use this playbook when a task involves writing or modifying source code in any
language used in this repository.

## Few-Shot Examples

For concrete examples of preferred code style patterns from this codebase, see
`agents.d/context/FEW_SHOT_CODE_STYLE.md`. Load this file when refactoring to
understand what good code looks like in this project.

## Function Design
- Prefer short, well-named functions: target fewer than 10 lines per function.
  Break this rule only when the alternative is less readable — clarity wins.
- Name functions to express intent, not implementation detail.

## Comments
- After writing a comment, take a refactor pass to embed its content into the
  code itself — better names, extracted functions, clearer structure. A comment
  that survives this pass is one whose WHY genuinely cannot be expressed in code.

## Data and Control Flow
- Prefer immutable data structures and functional transformations (`map`, `filter`,
  `fold`, etc.) over mutable accumulators and imperative loops. Avoid loops whose
  exit path depends on `break`, `continue`, or accumulated mutable state — these
  obscure intent and complicate reasoning. When a loop is necessary, make its
  termination condition and output unambiguous.

## General Style
- Keep edits minimal and limited to task scope.
- For a feature or bugfix task, "task scope" includes any method or function that
  contains or references the touched lines — not just the touched lines themselves.
- For arefactoring task, "task scope" includes any file containing the touched lines — not just the touched lines themselves.
- Preserve existing behavior unless the task explicitly changes it.
- Follow existing patterns and module ownership.
