# Visual evidence

From the repository root, with Docker running and the configured AWS credentials
available for local SSM secrets:

```sh
./gradlew :e2e:captureVisualEvidence \
  -PcaptureFixture=contribution-overview -PcaptureLabel=Before \
  -PcaptureViewport=1280x900 --no-daemon
```

The task builds and starts the local stack and requires the normal E2E network
access. Agents must run it outside the sandbox; see the
[Gradle playbook](../../agents.d/context/GRADLE_PLAYBOOK.md#browser-tests-and-sandboxed-daemons).
Use `-PcaptureViewport=390x844` for narrow evidence and `-PcaptureLabel=After` for
the matching later capture. Defaults are `contribution-overview`, `Capture`, and
`1280x900`. Viewport dimensions must be integers from 1 to 8192; unsupported sizes
fail. `WDIO_CHROME_BINARY` selects an installed Chrome when needed.

## Inspect the result

The printed manifest path identifies a unique run directory under
`e2e/build/reports/visual-evidence/`, containing `capture.png` and `manifest.json`.
Inspect the image and require `status: complete`. `partial`, `captured`, and
`failed` are not completed evidence; `captured` still awaits runner cleanup.

The manifest records source revision/dirty state, fixture conditions, label,
browser/version, theme, scale, requested/actual viewport and PNG dimensions.
Preserve the diff for dirty checkouts. Captures always execute; repeated labels
preserve all runs. Copy evidence elsewhere before `clean` removes build output.
Runner reports are in `e2e/build/reports/visual-evidence-runner/`.

## Compare captures

The fixture uses fixed contribution data, light theme, UTC, en-US, and scale 1,
with animations and external avatars disabled. Save Timestamp display text is
normalized to `12:00:00, 2026-11-19`; server records are unchanged. These conditions
are recorded in the manifest. Capture waits for the summary, fonts and images.

Match fixture version, viewport, theme, browser version and scale for Before/After.
These are viewport screenshots: narrow overflow is preserved, and narrow size
alone is not mobile device emulation. Remote assets/browser upgrades can affect
rendering. Do not run captures concurrently or alongside E2E tests against the
same stack: they share a test account, and normal E2E setup clears its parties.
After a forced process kill or host crash, inspect for orphaned browser processes.

## Upload explicitly

Follow the [work-card evidence procedure](../../agents.d/context/WORK_CHECKLIST.md#record-visual-evidence):

```sh
gh issue comment <issue-number> --body 'Implementation: <commit-sha>; viewport/PNG: 390x844; scale 1' \
  --attach './e2e/build/reports/visual-evidence/<before-run>/capture.png#Before — narrow' \
  --attach './e2e/build/reports/visual-evidence/<after-run>/capture.png#After — narrow'
```

Verify the attachments. Uploads are operator actions; review raw runner logs for
credentials before sharing them.
