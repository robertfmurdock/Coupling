# GitHub Actions Playbook

Use this playbook when adding or changing GitHub Actions workflows.

## Core Principle
- Keep workflow YAML thin and declarative.
- Put durable logic in repository-owned automation (`./gradlew` tasks) so it can run locally and in CI.
- Treat GitHub Actions as an orchestrator, not the primary implementation surface.

## Design Rules
- Prefer `run: ./gradlew <task>` over multi-line shell scripts in workflow steps.
- Keep GitHub-specific concerns in YAML only:
  - triggers
  - permissions
  - checkout/setup/auth
  - artifact and PR plumbing
- Keep business rules and gating logic in Gradle tasks under version control.
- Avoid embedding non-trivial parsing/branching in workflow shell blocks.

## Decoupling Goals
- A maintainer should be able to execute core automation locally with equivalent behavior.
- Workflow behavior should be testable by running the same Gradle task graph outside GitHub.
- Repository policy should not depend on GitHub event payload shape unless unavoidable.

## Validation Expectations
- Validate workflow syntax and execution path with `workflow_dispatch`.
- Validate automation logic via Gradle tasks locally first.
- Keep module-scoped validation commands explicit and wrapper-based (`./gradlew ...`).

## Change Reporting
- For workflow changes, report:
  - what moved out of YAML and where
  - which Gradle tasks now own decision logic
  - local command sequence for dry-run verification
