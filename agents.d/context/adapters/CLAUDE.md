# CLAUDE Instructions

This file is generated from `agents.d/context`. Do not hand-edit.

## Read First
- `agents.d/context/ARCHITECTURE_CANONICAL.md`
- `agents.d/context/TASK_CHECKLIST.md`

## Conditional Reads
Load the relevant playbook based on your task type:
{{PLAYBOOKS}}

## Execution Norms
- Start each session with `./gradlew agentBootstrap`.
- Use `./gradlew` for all tasks.
- Express repository automation as Gradle tasks, not ad hoc shell scripts.
- Start with module-scoped validation, then broaden as needed.
