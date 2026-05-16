# Copilot Instructions

This file is generated from `agents.d/context`. Do not hand-edit.

## Project Context
- Multi-module Gradle Kotlin project (JVM + JS).
- Primary areas: `client/`, `server/`, `libraries/`, `sdk/`.

## Required References
- `agents.d/context/ARCHITECTURE_CANONICAL.md`
- `agents.d/context/BOUNDARIES.md`
- `agents.d/context/TASK_CHECKLIST.md`
- `agents.d/context/PLAYBOOK_GRAPHQL.md` (GraphQL work)

## Commands
- `./gradlew test`
- `./gradlew build`
- `./gradlew check`
- `./gradlew :module:task`

## Rules
- Use the Gradle wrapper only.
- Express repository automation as Gradle tasks, not ad hoc shell scripts.
- Keep changes small and consistent with local patterns.
- For GraphQL API changes, run `scripts/graphql-ref-check.sh`.
- Keep schema + server + SDK + tests aligned in the same change.
