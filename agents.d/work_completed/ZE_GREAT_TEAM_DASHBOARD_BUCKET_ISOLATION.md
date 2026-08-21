# Ze Great Team Dashboard Bucket Isolation

## Goal
Use a Coupling-owned dashboard artifact bucket rather than the dashboard project's reference bucket.

## Constraints
- Do not provision AWS resources from this change.
- Keep credentials and secrets out of source control.

## Checklist
- [x] Review the existing dashboard deployment parameters
- [x] Replace the reference bucket with a Coupling-owned bucket name
- [x] Determine whether upstream documentation covers consumer bucket and IAM provisioning
- [x] Move to agents.d/work_completed/

## Implementation Notes
- The published consumer guide requires a private bucket and GitHub OIDC role but does not provide a consumer bootstrap template or scoped IAM policy.
- The dashboard repository's `infra/bootstrap.yml` provisions only its own resources and hard-codes its reference bucket, roles, and GitHub repository trust; it is not a reusable consumer setup.

## Validation
- Commands: `./gradlew :deploy:dashboard:dashboardDoctor --no-configuration-cache --quiet`
- Results: expected terminal failure: `coupling-ze-great-dashboard-artifacts` does not yet exist. AWS identity, parameter/template compatibility, and hosted client checks pass.
