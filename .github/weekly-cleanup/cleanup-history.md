# Weekly Cleanup History

Entries are appended by the cleanup agent after each run. Do not hand-edit.

## 2026-05-17 — libraries/model
- NotEmptyFlatMap.kt: deleted — specialized flatMap overload superseded by Iterable-based version in NotEmptyExtensions.kt

## 2026-05-16 — libraries/model
- NotEmptyExtensions.kt: verified-in-use — provides map extension consumed by CouplingPair.kt
