# CLAUDE Instructions

This file is generated from `agents.d/context`. Do not hand-edit.

## Read First
- `agents.d/context/ARCHITECTURE_CANONICAL.md`
- `agents.d/context/BOUNDARIES.md`
- `agents.d/context/TASK_CHECKLIST.md`
- `agents.d/context/PLAYBOOK_GRAPHQL.md` for GraphQL tasks

## Execution Norms
- Use `./gradlew` for all tasks.
- Express repository automation as Gradle tasks, not ad hoc shell scripts.
- Start with module-scoped validation, then broaden as needed.
- Keep edits minimal and limited to task scope.
- Preserve behavior unless task explicitly changes it.

## GraphQL-Specific Norms
- Use `scripts/graphql-ref-check.sh <pattern>` for impact mapping.
- Update schema, resolver path, SDK documents, SDK dispatchers, and tests
  together.
- Route deprecated fields through canonical mutation/command paths.
