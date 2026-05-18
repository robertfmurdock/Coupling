# Task: Add automatic agent restart mechanism to agentBootstrap when context files are regenerated

## Goal
When `./gradlew agentBootstrap` generates new context files (CLAUDE.md, repo-index.md, workflows.md), the agent should automatically restart its session to pick up the fresh context from disk, rather than continuing with stale context. Additionally, reduce redundant output in agentBootstrap to eliminate double-loading of information already in CLAUDE.md.

## Problem Statement
Currently when an agent runs `./gradlew agentBootstrap`:
1. The `syncAiContext` task may generate new versions of CLAUDE.md and other generated files
2. The agent continues working with the old context already loaded at session start
3. The `agentBootstrap` task outputs a list of files to read that duplicates what's already in CLAUDE.md
4. The agent must manually recognize context has changed and reload files, which is error-prone

This creates redundancy and staleness issues.

## Hard Constraints
- No behavior changes to `syncAiContext` - it must continue generating files correctly
- `agentBootstrap` must still ensure generated files are up-to-date before reporting
- Must work in all Claude Code environments (CLI, web, desktop, IDE extensions)
- Should not break non-interactive/CI usage of agentBootstrap
- Must preserve the dependency: agentBootstrap depends on syncAiContext

## Desired Behavior

### When syncAiContext generates no new content:
```
> Task :syncAiContext UP-TO-DATE
> Task :agentBootstrap
AI context is up-to-date. No restart needed.
```

### When syncAiContext generates new content:
```
> Task :syncAiContext
Synced AI context files:
- CLAUDE.md
- .github/copilot-instructions.md
- agents.d/context/generated/repo-index.md
- agents.d/context/generated/workflows.md

> Task :agentBootstrap
Generated AI context files have changed. Restart your session to load fresh context.
```

The agent should then recognize this signal and automatically restart (if possible) or prompt the user to start a new conversation.

## Technical Approach

Since `/clear` reloads CLAUDE.md and other context from disk, the solution is straightforward:

1. **Detect if syncAiContext regenerated files** (not UP-TO-DATE)
2. **Minimize agentBootstrap output** to eliminate redundancy with CLAUDE.md
3. **Output clear restart instruction** when files are regenerated

### When syncAiContext is UP-TO-DATE:
```
AI context is up-to-date.
```

### When syncAiContext regenerates files:
```
AI context files regenerated. Use /clear to start a fresh conversation with updated context.
```

Remove the "Required reads" and "Conditional reads" output since that information is already in CLAUDE.md - agents should read CLAUDE.md at session start per the "At session start, immediately read these files" instruction.

## Implementation Details

1. **Detect if syncAiContext regenerated files**
   - Check if syncAiContext task was UP-TO-DATE vs executed
   - Could use Gradle's task outcome tracking

2. **Simplify AgentBootstrapTask output**
   - Remove the "Required reads" and "Conditional reads" lists
   - Remove the line "`./gradlew agentBootstrap` already refreshes generated AI context files."
   - Output only a single line based on whether files were regenerated

## Success Criteria
- When `syncAiContext` regenerates files, agent automatically picks up new context (either via restart or clear signal)
- No redundant output between agentBootstrap and CLAUDE.md
- `agentBootstrap` output is minimal and actionable
- Agents don't work with stale generated context
- Manual file reading by agents is minimized

## Checklist
- [ ] Modify AgentBootstrapTask to detect when syncAiContext task executed vs was UP-TO-DATE
- [ ] Remove redundant file list output from AgentBootstrapTask (already in CLAUDE.md)
- [ ] Output minimal message: "AI context is up-to-date." or "AI context files regenerated. Use /clear to start a fresh conversation with updated context."
- [ ] Test that output is clear and minimal
- [ ] Run `./gradlew check` to verify
- [ ] Move this file to agents.d/work_completed/
