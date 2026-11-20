# Local visual evidence

Run from the repository root with the normal local E2E prerequisites: JDK and
Docker, the configured AWS credentials for local SSM secrets, and network access
for Gradle/npm, Chrome for Testing, Serverless, Auth0, and application assets.
The task builds the client/server and starts the existing local Compose stack.
Use a fresh daemon outside an agent sandbox, as described in the
[Gradle playbook](../../agents.d/context/GRADLE_PLAYBOOK.md#browser-tests-and-sandboxed-daemons).

```sh
./gradlew :e2e:captureVisualEvidence \
  -PcaptureFixture=contribution-overview -PcaptureLabel=Before \
  -PcaptureViewport=1280x900 --no-daemon
```

For narrow evidence use `-PcaptureViewport=390x844`. Repeat with
`-PcaptureLabel=After` after the implementation. Defaults are the contribution
overview fixture, label `Capture`, and viewport `1280x900`. Width and height must
be integers between 1 and 8192; unsupported browser sizes fail rather than
silently producing a larger viewport. `WDIO_CHROME_BINARY` uses the existing
plugin override when a specific installed Chrome is needed.

The task prints its manifest path. Each invocation creates a unique directory
under `e2e/build/reports/visual-evidence/`, containing `capture.png` and
`manifest.json`. Labels are manifest metadata, never file paths. Repeated labels
preserve every previous run; there is no overwrite. The task always executes
when requested and does not restore evidence from the build cache. `clean`
removes these generated artifacts, so copy evidence elsewhere before cleaning.

Inspect the PNG and require `status: complete` before using it. The manifest
records the source revision and dirty state, fixture version/conditions, label,
requested and measured viewport, PNG dimensions, device scale, theme,
browser/version, paths, and lifecycle status. Dirty-state evidence identifies
the checkout but does not encode the uncommitted diff; preserve that diff or
commit before making reproducibility claims.

`partial` means capture has not finished, `captured` means the PNG is ready but
the WDIO runner has not finished cleanup, and `complete` requires both capture
and a successful runner exit. Failures record only their stage in the manifest,
excluding exception messages that could contain auth payloads. Normal Ctrl-C
uses WDIO's worker/browser shutdown. An abruptly killed process can leave a
partial/captured manifest; neither is successful evidence. As with other browser
tools, SIGKILL or a host crash cannot guarantee cleanup; inspect and stop any
orphaned browser processes after such an interruption.

Runner HTML/JUnit output is separate, in `e2e/build/reports/visual-evidence-runner/`
and `e2e/build/visual-evidence-runner/test-results/`; diagnostics use the existing
collector. Do not upload raw runner logs as evidence without reviewing them.
Capture is opt-in: normal `check` runs harness tests but never creates product
evidence or seeds this fixture.

## Fixture and comparison conditions

`contribution-overview-v1` uses a reserved local party ID and fixed contribution
IDs, names, dates, authors, and cycle times. Preparation resets only that party;
cleanup deletes it after capture. Do not run captures concurrently against the
same local stack, or alongside the normal E2E suite, whose bootstrap clears the
test account's parties. Authentication and SDK writes reuse E2E helpers.

Readiness requires the expected contribution summary (five contributions, two
contributors, median cycle time 20m), loaded fonts and decoded images, followed
by two animation frames. Animations and third-party avatars are disabled using
existing test settings. The browser uses light color scheme, UTC, en-US and
device scale 1. Server-assigned Save Timestamp **display text only** is replaced
with `12:00:00, 2026-11-19` for the five fixture cards; actual server records are
unchanged. This normalization is disclosed in every manifest and fails if the
five expected values cannot be identified. No product styles or code change.

These are viewport screenshots, not full-page screenshots, pixel-diff baselines,
or mobile device emulation. Narrow evidence intentionally preserves any actual
overflow. Match fixture version, viewport, theme, browser version and device
scale when comparing Before/After. Remote assets and browser upgrades can still
change rendering; inspect both images.

## Explicit upload

Follow the [work-card visual evidence procedure](../../agents.d/context/WORK_CHECKLIST.md#record-visual-evidence).
Uploads remain an explicit operator action, for example:

```sh
gh issue comment <issue-number> --body 'Implementation: <commit-sha>; viewport/PNG: 390x844; scale 1' \
  --attach './e2e/build/reports/visual-evidence/<before-run>/capture.png#Before — narrow' \
  --attach './e2e/build/reports/visual-evidence/<after-run>/capture.png#After — narrow'
```

Verify the uploaded attachments. The capture task does not post issues/comments.

## Ownership and reuse

Kotlin owns capture policy, serializable manifests, failure contracts, the WDIO
adapter, fixture orchestration and Testmints tests. A small JS fragment connects
WDIO launcher hooks to exported Kotlin lifecycle functions, before a browser
worker starts and after it exits. No extra npm dependencies are required.

The existing Gradle WDIO plugin supports public `WdioTest` tasks, separately
compiled specs, config fragments, browser installation/lifecycle, reporters and
screenshots. It lacks a named evidence-fixture/manifest API and current jsmints
Kotlin browser bindings omit `setViewport`, emulation and screenshot operations.
The local adapter binds those existing WDIO methods rather than implementing a
second driver. A future upstream binding improvement can replace it directly.
Shared source inclusion reuses auth/page helpers without copying their code or
registering product tests in the capture compilation.

Validation entry points:

```sh
./gradlew :e2e:visualEvidenceTest
./gradlew :e2e:jsNodeDiagnosticsBrowserTest --no-daemon
./gradlew check --no-daemon
```
