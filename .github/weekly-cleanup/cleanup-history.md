# Weekly Cleanup History

Entries are appended by the cleanup agent after each run. Do not hand-edit.

## 2026-05-16 — libraries/model
- NotEmptyFlatMap.kt: verified-in-use — provides map extension consumed by CouplingPair.kt (corrected: tcr-delete.sh uses argument basename; prior entry recorded wrong filename)

## 2026-05-17 — libraries/model
- NotEmptyFlatMap.kt: verified-in-use — full build failed after deletion
- PredictableWordPicker.kt: verified-in-use — unused extension function
- CalculateTimeSinceLastPartnership.kt: verified-in-use — candidate for deletion
- NotEmptyExtensions.kt: verified-in-use — candidate for deletion

## 2026-05-17 (run-25998194140-1) — libraries/model
- CalculateTimeSinceLastPartnership.kt: verified-in-use — candidate for deletion
- NotEmptyExtensions.kt: verified-in-use — extension functions on NotEmptyList
- AreEqualPairs.kt: verified-in-use — pair equality functions

## 2026-05-17 (run-automated-cleanup-2) — libraries/model
- (no safe targets found) — zero-import candidate scan found no deletion candidates

## 2026-05-17 (run-claude-session-cleanup) — libraries/model

## 2026-05-18 (run-claude-session-cleanup) — libraries/model
- (no safe targets found) — prior runs exhaustively tested candidate functions; cross-references verified by grep
- (no safe targets found) — all model library utilities verified as multi-module consumers; prior runs thoroughly evaluated candidates

## 2026-05-18 (run-26009271530-boundary-scan) — libraries/model
- (no safe targets found) — boundary check: model exports only multi-module reusable contracts; no app-specific policy detected
- (no safe targets found) — libraries/action contains shared algorithms (Game, Round, Wheel) correctly placed; SDK dispatchers clean (no silent fallback patterns); test placement verified appropriate across boundary levels

## 2026-05-19 (run-26095510557-1) — libraries/model
- (no safe targets found) — prior runs exhaustively tested deletion candidates; scope sweep across sdk, server, client, coupling-plugins confirms no boundary violations; Gradle build logic clean

## 2026-05-25 (run-auto-cleanup) — libraries/repository/core
- (no safe targets found) — focus module contains only interface definitions and extension syntax; no test files (tested via integration in dynamo/compound modules); all public APIs verified in-use

## 2026-06-08 (run-27156507882-1) — client/components
- Dsl.kt: verified-in-use — marked DSL wrapper with zero imports
- ColorContext.kt: verified-in-use — ColorContext with zero imports
- External.kt: verified-in-use — d3 color external wrapper

## 2026-06-15 (run-27569521241-1) — sdk
- SetupFormatter.kt: verified-in-use — zero import references
- Main.kt: verified-in-use — zero import references
- PinDetailsMapper.kt: verified-in-use — zero import references

## 2026-06-29 (run-28391060946-1) — libraries/model
- PinTarget.kt: queued — zero imports, single-value enum
- Badge.kt: queued — zero imports, simple enum
- PairingRule.kt: queued — zero imports, conversion enum
- (no safe targets found) — all tested zero-import candidates verified-in-use; prior runs exhaustively tested; no boundary violations detected
- PinTarget.kt: verified-in-use — full build failed after deletion
- PairingRule.kt: verified-in-use — full build failed after deletion

## 2026-07-06 (run-28811332303-1) — libraries/repository/core
- (no safe targets found) — focus area contains only interface definitions; no test files present; queued items from prior run (libraries/model) do not exist in current state

## 2026-07-13 — e2e
- strategy-proposed: none — e2e module exhaustively tested in prior runs (dead-code heuristic finds no candidates); boundary rules do not apply (test-only code); test/page naming shows minor convention drift but carries no structural risk; no actionable recurring pattern identified warranting new cleanup strategy

## 2026-07-20 (run-29759367416-1) — client/components
- FormatDistance.kt: verified-in-use — suspected dead code: zero external imports
- AnimationsDisabledContext.kt: verified-in-use — zero external imports
- Exterrnal.kt: verified-in-use — zero external imports
