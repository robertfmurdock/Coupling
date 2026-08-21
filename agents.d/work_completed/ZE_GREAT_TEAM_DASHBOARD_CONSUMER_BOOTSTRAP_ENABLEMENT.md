# Enable the Ze Great Team Dashboard

## Goal
Deploy the private dashboard Lambda and its Coupling-owned public HTTP gateway from successful `master` builds.

## Constraints
- Use the Gradle wrapper and Gradle-owned deployment automation.
- `deploy/dashboard/bootstrap.json` is the non-secret source of truth for the consumer names, region, GitHub identity, and protected environment.
- An AWS administrator must review and explicitly execute the upstream `core-v1.yml` and `github-oidc-v2.yml` change sets. CI must never bootstrap AWS resources.
- Restrict GitHub Environment `dashboard-production` to `master`; store only `DASHBOARD_DEPLOY_ROLE_ARN` and `DASHBOARD_CLOUDFORMATION_EXECUTION_ROLE_ARN` as its variables.
- Keep the package-owned private Lambda and Coupling-owned public gateway separate.
- **TDD required: test → fail → implement → pass → refactor per slice (see PLAYBOOK_CODE_STYLE.md)**

## Checklist
- [x] Review card for template compliance
- [x] Identify deployment and workflow patterns
- [x] Consumer bootstrap boundary
  - [x] Add non-secret consumer bootstrap manifest
  - [x] Document administrator-reviewed bootstrap boundary
- [x] Private application deployment
  - [x] Upgrade the dashboard package to `0.1.32`
  - [x] Generate application parameters from the bootstrap manifest
  - [x] Pass the bootstrap execution role to application deployment
- [x] Public gateway
  - [x] Add the Coupling-owned HTTP API template and API-scoped Lambda permission
  - [x] Generate gateway parameters from the bootstrap manifest
  - [x] Check `/health` and `/` through the gateway
- [x] CI activation
  - [x] Add protected application deployment after build
  - [x] Add dependent gateway deployment using `CouplingDeploy`
- [x] Validate scoped tasks and workflow structure
- [x] Run `./gradlew check`
- [x] Final refactor pass and playbook review
- [x] Move to `agents.d/work_completed/`

## Implementation Notes
- Immutable GitHub IDs were verified against the public repository API: owner `6215634`, repository `18269766`.
- The administrator should generate and review upstream bootstrap parameters/change sets with `ze-great-dashboard-aws bootstrap parameters` and `bootstrap change-set`; execution is intentionally outside this repository CI.
- The artifact bucket is private. The API Gateway default `execute-api` endpoint is the only public entry point.
- There is no plugin test source set for these deployment task types. The focused build-level assertions are generated parameter output and CloudFormation template validation.
- First protected deployment remains externally gated: run the bootstrap change sets, configure the reviewed Environment variables, then confirm `/health` and `/` return 2xx through the gateway. The private Lambda has no Function URL in the `0.1.32` template.

## Validation
- Commands:
  - `./gradlew kotlinUpgradeYarnLock --no-configuration-cache`
  - `./gradlew :deploy:dashboard:dashboardApplicationParameters :deploy:dashboard:dashboardGatewayParameters :deploy:dashboard:dashboardPackage --no-configuration-cache`
  - `./gradlew :deploy:dashboard:dashboardGatewayDeployStack -PdashboardDryRun=true --no-configuration-cache`
  - `./gradlew :deploy:dashboard:dashboardDoctor --no-configuration-cache --quiet`
  - `git diff --check`
  - `ruby -e 'require "yaml"; YAML.load_file(".github/workflows/main.yml")'`
  - `./gradlew :coupling-plugins:check --no-configuration-cache`
  - `./gradlew check --no-configuration-cache --quiet`
- Results:
  - Managed Yarn lock resolved `@continuous-excellence/ze-great-dashboard-aws` exactly to `0.1.32`.
  - Parameter generation produced `Name=ze-great-team-dashboard`, private artifact bucket `coupling-ze-great-dashboard-artifacts`, and gateway function name from the bootstrap manifest; package output reported dashboard version `0.1.32`.
  - Gateway CloudFormation validation passed.
  - Doctor passed toolchain, AWS identity, generated parameter/template compatibility, and hosted client checks; it correctly failed only because the administrator-owned artifact bucket has not been bootstrapped (`NoSuchBucket`).
  - Workflow YAML parsing, `git diff --check`, scoped build-logic checks, and repository-wide `check` passed.
