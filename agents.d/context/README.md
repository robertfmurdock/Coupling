# Agent Context Pack

This directory is the canonical source for AI-agent execution context in this
repository.

## Layout
- `CONTEXT_CONTRACT.md`: required context categories every agent must consume.
- `ARCHITECTURE_CANONICAL.md`: canonical architecture and norms.
- `BOUNDARIES.md`: module boundaries and non-goals.
- `PLAYBOOK_GRAPHQL.md`: high-risk GraphQL change recipe.
- `TASK_CHECKLIST.md`: required pre-flight and completion checks.
- `context.json`: machine-readable context for scripted agents.
- `generated/`: generated indexes (repo modules and workflows).
- `adapters/`: short templates for agent-specific injection files.

## Update Flow
1. Update canonical docs in this folder.
2. Regenerate derived files:
   - `./gradlew syncAiContext`
3. Run the bootstrap reader to confirm read order:
   - `./gradlew agentBootstrap`
