# Welcome lab baseline

Run outside the agent sandbox with a fresh daemon, like the repository's other
browser tasks:

```sh
fish -lc './gradlew :e2e:measureWelcomePerformance --no-daemon'
```

The task builds/starts the normal local app stack and uses the existing
WebdriverIO/Chrome tooling. It performs three fresh-profile navigations to
`https://localhost/welcome/`, each followed by a warm refresh. No authentication,
party creation, or saved-data changes are performed. `WDIO_CHROME_BINARY` uses the
same optional installed-browser override as other browser tasks. This opt-in task
always executes and is deliberately outside `check`; timing assertions would be
flaky release gates.

Read `e2e/build/reports/web-performance/welcome.json`. Only `status: complete`
with six samples is a complete run. Preserve this file before the next invocation,
which replaces it; interrupted runs retain `status: partial` and completed samples.
Check the command outcome and timestamp as well as status. Reports contain only
numeric page/resource observations and environment metadata, not URLs, page text,
headers, request bodies, tokens, or party identifiers.

Conditions: 1280x900 outer window, device scale factor 1, no network/CPU throttling.
Actual viewport is recorded per sample. Readiness is the visible “Come on in!”
button; host elapsed time includes WebDriver round trips/polling, while readyAtMs
is the page clock at that observation. Observers initialize before page scripts;
the capture window ends two seconds after readiness. Each session and driver is
closed even on failure. No performance report is cached or reused by Gradle.

Interpretation:

- LCP and CLS are bounded navigation observations, not final page-lifetime or
  field Web Vitals. No INP or interaction trace is measured yet.
- CLS is the largest session-window sum (gaps under 1s, window under 5s), excluding
  shifts following recent input. Unsupported observer metrics remain null; an
  empty supported CLS observation is zero. Missing LCP remains null.
- Long tasks report observed count and total duration. Truncation is disclosed
  if either bounded observer storage or the resource-timing buffer fills.
- Resource count/transfer bytes cover Resource Timing entries only, not the
  navigation document. Cross-origin entries without Timing-Allow-Origin often
  hide their sizes. zeroSizeResourceCount includes unavailable sizes and genuinely
  empty resources; reportedTransferBytes is never a total page-weight estimate.
- Fresh profiles do not reset OS DNS/TLS or remote CDN caches. Welcome content is
  randomized and uncontrolled. Record browser/OS/CPU and source revision/dirty
  state; compare like conditions and retain repeated samples and variation.

Metric aggregation is tested in `:e2e:jsNodeTest` using Testmints. Authenticated
party/pair routes, controlled throttling, full network byte capture, and interaction
traces remain separate CPL-54 work. Do not select active-resource optimizations
from these partial measurements alone.
