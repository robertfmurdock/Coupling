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
- [ ] Run scoped and broad checks
- [ ] Move to `agents.d/work_completed/`

## Validation
- Commands:
  - `./gradlew :deploy:dashboard:dashboardGatewayParameters --no-configuration-cache`
- Results:
  - Passed. The dashboard task configured and ran after the SSM values became lazy providers.
