# Agent Context Pack

This directory is the canonical source for AI-agent execution context in this
repository.

`context.json` is the machine-readable source of truth for required and
conditional context reads.

## Layout
- `CONTEXT_CONTRACT.md`: required context categories every agent must consume.
- `ARCHITECTURE_CANONICAL.md`: canonical architecture and norms.
- `BOUNDARIES.md`: module boundaries and non-goals.
- `GRADLE_PLAYBOOK.md`: Gradle build-logic and automation conventions.
- `GITHUB_ACTIONS_PLAYBOOK.md`: workflow authoring conventions that keep logic in Gradle tasks.
- `PLAYBOOK_GRAPHQL.md`: high-risk GraphQL change recipe.
- `TASK_CHECKLIST.md`: required pre-flight and completion checks.
- `context.json`: machine-readable context for scripted agents.
- `generated/`: generated indexes (repo modules and workflows).
- `adapters/`: short templates for agent-specific injection files.

## Update Flow
1. Start/refresh agent runtime context:
   - `./gradlew agentBootstrap`
2. Update canonical docs in this folder.
3. Re-run bootstrap to regenerate and validate derived outputs:
   - `./gradlew agentBootstrap`
4. Optional manual regeneration (without bootstrap readout):
   - `./gradlew syncAiContext`
