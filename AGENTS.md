# Agent Guidance

Coupling is a multi-module Gradle project with Kotlin/JVM and Kotlin/JS: a web
client, backend services, and shared libraries.

## Contribution Protocol

Deliver meaningful features as small, independently releasable test-plus-
implementation commits. Each commit must pass the full test suite and be safe to
push and release; it must remain useful and safe if subsequent work stops.

Before tests, call the shot: state the behavior, affected boundary, proposed
contract/design, scenarios, and expected failure. After confirming a meaningful
failure and before production edits, state the smallest implementation, ownership,
and data flow. Use concise prose or a diagram when useful. These are visible
updates, not approval gates; continue within agreed scope and resolve material
ambiguity or scope changes with the user.

Run focused checks during red–green–refactor, then `./gradlew check` and review the
complete slice before committing. Commit passing tests and implementation together
before starting the next slice. Preserve existing behavior unless intentionally
changed, and include required cross-layer changes in the same commit. Do not invent
failing tests for documentation, mechanical edits, or behavior-preserving refactors.
Report validation results and gaps; do not call an unverified slice release-safe.
Pushing and releasing require the user's authorization.

Use a [work card](agents.d/context/WORK_CHECKLIST.md) for multi-commit features or
handoffs; a single slice can declare its design in the conversation. Default to
one contributor for dependent TDD steps. Delegate focused investigations or reviews
when a separate context has a clear purpose and session permissions allow it.
Hand off a compact brief, relevant paths, constraints, and expected results;
implementers still read the relevant tests. Review test clarity and production
design separately, and review the overall outcome at completion.

## Commands and Constraints

- Use `./gradlew` for build and validation tasks; express repository automation as
  Gradle tasks rather than new ad hoc shell scripts. Ordinary file/search/git tools
  remain appropriate for investigation and editing.
- Focused validation: `./gradlew :module:task`. Full suite and completion gate:
  `./gradlew check`. Use `./gradlew test` or `./gradlew build` when the task needs them.
- Reuse established capabilities; keep changes scoped and consistent with local
  patterns. Investigate intentional-looking overrides before changing them.
- For GraphQL changes, keep schema, server, SDK, and tests synchronized. Deprecated
  APIs delegate through canonical command/mutation paths. Use
  `agents.d/utilities/graphql-ref-scan.sh <pattern>` for discovery, not verification.
- Do not modify or bypass configured AWS profiles. For AWS/Gradle failures, follow
  the credential and daemon diagnosis in the Gradle playbook.

## Task-Based Guidance

Load the documents relevant to the work, not the entire context directory:

- [Architecture](agents.d/context/ARCHITECTURE_CANONICAL.md): ownership and cross-module design.
- [Testing](agents.d/context/TESTING.md): behavior changes and test edits.
- [Production code style](agents.d/context/PLAYBOOK_CODE_STYLE.md): production source edits.
- [GraphQL](agents.d/context/PLAYBOOK_GRAPHQL.md): schema, operations, and mutation routing.
- [Gradle](agents.d/context/GRADLE_PLAYBOOK.md): build logic, dependencies, automation, or build failures.
- [GitHub Actions](agents.d/context/GITHUB_ACTIONS_PLAYBOOK.md): workflow changes.
- [Context index](agents.d/context/README.md): further task-specific references.

These committed documents are ready to read on a fresh checkout; no bootstrap or
context generation is required. Edit each convention in its authoritative document.
