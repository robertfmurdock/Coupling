# Agent Context Template Deduplication

## Problem

The adapter template files (`agents.d/context/adapters/*.md`) duplicate content that already exists in `context.json`:

1. **Required reads** — hard-coded in both templates, also in `context.json.required_reads`
2. **Commands** — copilot template lists them explicitly, also in `context.json.commands`  
3. **Rules/execution norms** — prose versions in templates, structured versions in context.json

Only the playbooks use the `{{PLAYBOOKS}}` placeholder pattern correctly (single source in context.json).

## Goal

Make `context.json` the single source of truth. Templates should be minimal structural wrappers that expand placeholders from context.json.

## Approach

### 1. Extend Placeholder System

Add new placeholders to the template files:
- `{{REQUIRED_READS}}` — expand from `context.json.required_reads[]`
- `{{COMMANDS}}` — expand from `context.json.commands`
- `{{EXECUTION_NORMS}}` or `{{RULES}}` — expand from `context.json.rules` + execution norms

### 2. Update Templates

**CLAUDE.md template:**
```markdown
## Read First
{{REQUIRED_READS}}

## Conditional Reads
Load the relevant playbook based on need:
{{PLAYBOOKS}}

## Execution Norms
{{EXECUTION_NORMS}}
```

**copilot-instructions.md template:**
```markdown
## Required References
{{REQUIRED_READS}}

## Conditional Reads
Load the relevant playbook based on need:
{{PLAYBOOKS}}

## Commands
{{COMMANDS}}

## Rules
{{RULES}}
```

### 3. Update syncAiContext Task

Modify the Gradle task in `scripts/` (or wherever it lives) to:
1. Parse `context.json`
2. Replace `{{REQUIRED_READS}}` with bullet list from `required_reads[]`
3. Replace `{{COMMANDS}}` with bullet list from `commands`
4. Replace `{{EXECUTION_NORMS}}`/`{{RULES}}` with formatted rules

### 4. Handle Template Differences

The two templates have slightly different prose/structure for the same concepts:
- CLAUDE uses "Read First" and "Execution Norms"
- Copilot uses "Required References", "Commands", and "Rules"

Options:
- A) Keep different prose per template, but all content comes from context.json
- B) Add template-specific config to context.json (more complex)
- C) Make templates even more similar (loses agent-specific flavor)

Recommend option A: templates control headers and formatting, context.json controls content.

## Validation

- Run `./gradlew syncAiContext`
- Verify generated `CLAUDE.md` and `.github/copilot-instructions.md` are unchanged (content-wise)
- Update `context.json` with a test change, regenerate, verify it propagates
- Run `./gradlew agentBootstrap` to confirm no breakage

## Checklist

- [x] Find and read the syncAiContext task implementation
- [x] Design placeholder expansion logic for REQUIRED_READS, COMMANDS, RULES
- [x] Update both adapter templates to use new placeholders
- [x] Implement placeholder expansion in syncAiContext task
- [x] Run syncAiContext and verify generated output matches current content
- [x] Test: add a dummy command to context.json, regenerate, verify it appears
- [x] Run agentBootstrap to confirm no breakage
- [x] Move this file to agents.d/work_completed/

## Design Notes

### Placeholder Expansion Strategy

**{{REQUIRED_READS}}** — from `context.json.required_reads[]`:
```kotlin
manifest.path("required_reads")
    .takeIf { it.isArray }
    ?.mapNotNull { it.asText(null) }
    ?.joinToString("\n") { "- `$it`" }
    ?: ""
```

**{{COMMANDS}}** — from `context.json.commands`:
- Extract `agent_bootstrap` as primary command
- Extract `default[]` array as validation commands  
- Extract `module_task_pattern` as scoped convention
```kotlin
val commands = manifest.path("commands")
val agentBootstrap = commands.path("agent_bootstrap").asText(null)
val defaults = commands.path("default").takeIf { it.isArray }?.mapNotNull { it.asText(null) } ?: emptyList()
val modulePattern = commands.path("module_task_pattern").asText(null)
```

**{{EXECUTION_NORMS}}** and **{{RULES}}** — synthesized from rules object:
- Both templates need the same content, just different headers
- Extract key rules and format as bullet points
- Rules like "use_gradle_wrapper_only", "keep_changes_scoped" map to prose
