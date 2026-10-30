# Context Index

Start with [AGENTS.md](../../AGENTS.md), the canonical shared entry. Root `CLAUDE.md`
and `.github/copilot-instructions.md` are committed adapters to it. Guidance is
available before any build runs; edit Markdown directly without regeneration.

## Read When Relevant

| Document | When to read | Owns |
| --- | --- | --- |
| [Architecture](ARCHITECTURE_CANONICAL.md) | Ownership or cross-module design | Module boundaries and system-specific paths |
| [Testing](TESTING.md) | Behavior changes or test edits | Test-first development, TestMints, scenario design |
| [Production style](PLAYBOOK_CODE_STYLE.md) | Production source edits | Kotlin design, formatting, and interop conventions |
| [Examples](FEW_SHOT_CODE_STYLE.md) | A production style decision needs illustration | Illustrative patterns; verify against current code |
| [GraphQL](PLAYBOOK_GRAPHQL.md) | Schema, operation, or mutation changes | Cross-layer synchronization and validation recipe |
| [Gradle](GRADLE_PLAYBOOK.md) | Build/dependency/automation edits or build failures | Build conventions, cache policy, AWS diagnosis |
| [GitHub Actions](GITHUB_ACTIONS_PLAYBOOK.md) | Workflow edits | CI orchestration and safety gates |
| [Work cards](WORK_CHECKLIST.md) | Multi-commit work or handoffs | Safe slice planning and durable progress |
| [Owner values](PERSONA.md) | Ambiguity or competing design choices | Product, collaboration, and decision preferences |
| [Contribution visualizations](CONTRIBUTION_VISUALIZATIONS.md) | Contribution visualization work | Feature-specific design context |

Cleanup strategies (`agent-strategy-*.md`) are loaded by the weekly-cleanup prompt
for the selected strategy; they are not general startup instructions. Utilities and
their troubleshooting notes live in `agents.d/utilities/`.

## Maintaining Guidance

Keep each rule in one authoritative home; link to specialist guidance rather than
copying it. Add durable repository knowledge and decisions, not generic engineering
recipes or transcripts. Update active consumers when a reference changes. Historical
work cards and cleanup logs record past decisions and are not current instructions.

The design follows [OpenAI's guidance on task-relevant context](https://developers.openai.com/blog/rethinking-skills-and-prompts-for-gpt-6-astra)
and [Anthropic's guidance on concise repository instructions](https://code.claude.com/docs/en/best-practices).
Separate agent contexts are a selective tool, not a proven performance improvement
for this repository; no benchmark is implied by this structure.
