# Ze Great Team Dashboard Deployment

## Goal
Deploy the published Ze Great Team dashboard from successful Coupling `master` builds.

## Constraints
- Use the Gradle wrapper and Gradle-owned deployment automation.
- Pin `@continuous-excellence/ze-great-dashboard-aws` to `0.1.27`.
- Use the existing `CouplingDeploy` OIDC role in `us-east-1`; no credentials in source control.
- **TDD required: test → fail → implement → pass → refactor per slice (see PLAYBOOK_CODE_STYLE.md)**

## Checklist
- [x] Review card for template compliance
- [x] Identify deployment and workflow patterns
- [x] Package configuration
  - [x] Write failing configuration slice for missing board input
  - [x] Add valid board and verify package
  - [x] Write failing configuration slice for missing parameters
- [x] Add doctor, artifact upload, CloudFormation deployment, and endpoint health tasks
- [x] Add checked-in CloudFormation parameters
- [x] Invoke deployment from successful `master` workflow builds
- [x] Final refactor pass
- [x] Review against applicable playbooks
- [x] Move to agents.d/work_completed/

## Implementation Notes
- The upstream AWS package owns Lambda packaging and generated-template compatibility.
- The published radiator names `GITHUB_TOKEN`; this consumer omits it to use the selected unauthenticated requests.
- The existing private `ze-great-dashboard-reference-artifacts` bucket is the available dashboard artifact bucket in `us-east-1`.

## Validation
- Commands:
  - `./gradlew :deploy:dashboard:dashboardPackage --no-configuration-cache`
  - `./gradlew :coupling-plugins:check :deploy:dashboard:dashboardDoctor :deploy:dashboard:dashboardDeployStack -PdashboardDryRun=true --no-configuration-cache`
  - `./gradlew :sdk:jsNodeTest --no-configuration-cache --quiet`
- Results:
  - Package, doctor, generated-template validation, and build-logic checks passed.
  - `./gradlew check` was started; `:sdk:jsNodeTest` had one timeout in an unrelated SDK test, then passed on a focused rerun. The broad run was stopped after that failure while its e2e work continued.
