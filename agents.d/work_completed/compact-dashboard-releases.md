# Compact dashboard releases

## Goal
Show the four latest package versions in half their previous board grid area.

## Constraints
- Scope: deploy/dashboard/board.yaml; no application or deployment behavior changes.
- Preserve endpoints, JSON paths, links, and the 15-minute refresh interval.
- Use grouped HTTP facts supported by the existing 0.27.1 dependency.

## Checklist
- [x] Review repository guidance and upstream grouped-fact documentation and rendering styles.
- [x] Replace four tiles with one compact, labeled two-column release panel.
- [x] Match the schema annotation to the installed dashboard version.
- [x] Review the diff and run scoped and repository checks.

## Implementation Notes
Configuration-only change: existing packaging validation is the meaningful contract check;
no implementation-mirroring tests were added. Grid allocation falls from 24 to 12 cells.
The browser layout has not been visually verified. No deployment was performed.

## Validation
- `./gradlew :deploy:dashboard:check` — passed, including packaging the new board.
- `./gradlew check` — passed (unchanged tests were largely up-to-date).
- `git diff --check` — passed.
