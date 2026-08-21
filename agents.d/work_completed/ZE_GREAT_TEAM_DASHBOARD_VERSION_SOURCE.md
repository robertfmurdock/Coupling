# Ze Great Team Dashboard Version Source

## Goal
Use the managed JavaScript dependency declaration as the only dashboard package version source.

## Constraints
- Keep the dashboard package version pinned exactly in `libraries/js-dependencies/package.json`.
- Do not duplicate that version in Gradle deployment logic.

## Checklist
- [x] Remove the duplicate Gradle version declaration
- [x] Verify package output uses the managed dependency version
- [x] Move to agents.d/work_completed/

## Implementation Notes
- The dashboard CLI defaults its release version to its installed package manifest version.

## Validation
- Commands: `./gradlew :deploy:dashboard:dashboardPackage --no-configuration-cache`
- Results: package output reports `dashboardVersion` `0.1.27`; `deploy/dashboard/build.gradle.kts` has no version literal.
