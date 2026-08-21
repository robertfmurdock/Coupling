# Dashboard Deploy Role SSM Boundary

## Goal
Allow the restricted dashboard deployment role to configure dashboard Gradle tasks without reading unrelated test SSM parameters.

## Constraints
- Preserve SSM reads for Compose tasks that require their values.
- Keep provider resolution lazy and configuration-cache-compatible.
- **TDD required: test → fail → implement → pass → refactor per slice (see PLAYBOOK_CODE_STYLE.md)**

## Checklist
- [x] Identify the failed deployment boundary
- [x] Record the focused failing CI behavior: restricted dashboard role fails during root configuration before deployment
- [x] Make Compose SSM environment values lazy providers
- [x] Verify a dashboard task configures without SSM resolution
- [x] Record and fix the subsequent focused CI failure: artifact upload did not depend on generated application parameters
- [ ] Run scoped and broad checks
- [ ] Move to `agents.d/work_completed/`

## Validation
- Commands:
  - `./gradlew :deploy:dashboard:dashboardGatewayParameters --no-configuration-cache`
  - `./gradlew :deploy:dashboard:dashboardDeployStack -PdashboardDryRun=true --no-configuration-cache`
- Results:
  - Passed. The dashboard task configured and ran after the SSM values became lazy providers.
  - GitHub Actions run `32518181125`, application deployment job `96887300099`: configured under the restricted dashboard role and passed `dashboardDoctor` and `dashboardPackage`; Gradle then correctly rejected an undeclared producer/consumer dependency between `dashboardApplicationParameters` and `dashboardUploadArtifact` before any deployment mutation.
  - Passed. The complete application deployment graph generated parameters, passed the doctor checks, packaged the release, ran the artifact upload dry run, and validated the CloudFormation template.
