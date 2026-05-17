### Investigation Protocol
- Track findings as a ledger: record each candidate and its verdict (`deleted` / `verified-in-use` / `skipped`). Do not re-investigate a candidate once a verdict is reached.
- **Candidate list:** A pre-computed candidate list is available at `__CANDIDATES_FILE__`. Read this file first and use it as your starting point — do not grep for candidates from scratch if the list is non-empty.
- **Preferred investigation method for dead code candidates:** use the TCR delete script:
  ```
  agents.d/utilities/tcr-delete.sh <path/to/File.kt> ["optional reason"]
  ```
  The script deletes the file, runs `./gradlew check`, and automatically: reverts the file if the build fails, appends the verdict (`deleted` or `verified-in-use`) to cleanup-history.md, and writes the run header if not already present. You do not need to manually revert or write history for deletions.
- Maximum candidates evaluated per run: **3**.
- If no safe target exists after evaluating candidates, stop without leaving any uncommitted deletions.
