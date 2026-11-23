# CPL-41 — Dashboard Deploy Role SSM Boundary

## Disposition — closed 2026-11-23

Closed by owner decision. The SSM boundary and subsequent deployment fixes are
implemented and committed, including `eede0a79d`, `16c955f0c`, and `1fe6f9ef2`.
[GitHub Actions run 44834636953](https://github.com/robertfmurdock/Coupling/actions/runs/44834636953)
at `5528547316203b6a541e1a4197829f39eb878d2b` passed both Build and Deploy
Dashboard, including the Deploy and verify dashboard step. That deployment path
uses the restricted role and verifies the application through the public gateway,
covering the card's intended boundary and superseding the earlier rollout failure
below. Run and job results were inspected at closeout; no fresh tests or deployment
were run.

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
- [x] Record and fix the gateway deployment failure: HTTP API does not expose `ExecutionArn`
- [x] Run scoped and broad checks — subsequent successful build and deployment linked above
- [x] Move to `agents.d/work_completed/`

## Validation
- Commands:
  - `./gradlew :deploy:dashboard:dashboardGatewayParameters --no-configuration-cache`
  - `./gradlew :deploy:dashboard:dashboardDeployStack -PdashboardDryRun=true --no-configuration-cache`
- Results:
  - Passed. The dashboard task configured and ran after the SSM values became lazy providers.
  - GitHub Actions run `32518181125`, application deployment job `96887300099`: configured under the restricted dashboard role and passed `dashboardDoctor` and `dashboardPackage`; Gradle then correctly rejected an undeclared producer/consumer dependency between `dashboardApplicationParameters` and `dashboardUploadArtifact` before any deployment mutation.
  - Passed. The complete application deployment graph generated parameters, passed the doctor checks, packaged the release, ran the artifact upload dry run, and validated the CloudFormation template.
  - GitHub Actions run `32520403587`: application deployment succeeded. Gateway role assumption succeeded with the immutable OIDC subject, then CloudFormation rolled back before gateway creation because `AWS::ApiGatewayV2::Api` has no `ExecutionArn` attribute.
