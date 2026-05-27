# Work Checklist

**Work Card**: Task definition in `agents.d/work/` (NOT Claude Code's built-in tasks).

## Template

```markdown
# [Feature Name]

## Goal
One-sentence outcome.

## Constraints
- Hard boundaries from PERSONA, playbooks, or architecture

## Checklist
- [ ] Review card for template compliance
- [ ] [Broad feature slice 1]
- [ ] Final refactor pass
- [ ] Review against applicable playbooks
- [ ] Move to agents.d/work_completed/

## Implementation Notes
[Log discoveries, deviations, constraints]

## Validation
- Commands: [fill as you go]
- Results: [fill before completion]
```

## Intake
- `./gradlew agentBootstrap` (if AWS error: `unset AWS_PROFILE`)
- Read `context.json` required_reads and relevant playbooks
- Identify impacted modules, test scope, test-level intent
- Confirm constraints before coding

## Implementation

### TDD Cycle Per Slice
1. **Test**: Write one test. Confirm fail/pass reason.
2. **Implement**: Simplest thing that works.
3. **Refactor-light**: Clean up what you just wrote.
4. **Verify pushable**: Run scoped validation. Verify backward compatibility if adding alternatives.
5. **Commit**: When tests pass.
6. **Update card**: Mark item complete.

**Orchestrator pattern**: Spawn testing → implementation → refactor subagents per slice.

### Refactoring
- **Light** (during slices): names, duplication, structure
- **Final** (end): re-read all modified files, apply code style, run check

### Deprecation
1. Build and test new feature first (full parity)
2. Mark old API deprecated (why, replacement, when removed)
3. Test both APIs

### Adaptation
- Project guidelines override card plans
- Update plan when constraints discovered
- Log in Implementation Notes

### General
- Keep changes scoped to impacted modules
- Follow existing patterns
- Confirm before "fixing" suspicious-looking code (Chesterton's Fence)
- Each item = pushable state
- Update card as you go, not at end

## Validation
- Run smallest task set first (`./gradlew :module:task`)
- `./gradlew check` before completion
- For GraphQL: run `agents.d/utilities/graphql-ref-scan.sh`
- Review against playbooks

## Completion
- List files changed, validation results
- Move card to `agents.d/work_completed/`
- State residual risks
