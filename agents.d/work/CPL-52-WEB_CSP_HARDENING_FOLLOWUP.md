# CPL-52 — Enforce a compatible content security policy

**Work card protocol: 2**

## Goal and Constraints

Design and safely enforce a Content Security Policy for the Coupling web
client, accounting for the repository's CDN lookup/template pipeline and all
runtime integrations. Preserve Auth0, Slack/Discord OAuth, analytics, GraphQL,
WebSocket, GraphiQL, and supported browser behavior.

Apply the [environment convention](../context/ARCHITECTURE_CANONICAL.md#build-and-deployment):
local, sandbox, prerelease and production exercise the same CSP implementation.
Use normal configured origins and shared deployment wiring; avoid environment-name
branches and minimize flags. CSP is baseline application behavior, independent of
the existing prerelease feature flag. That flag changes the features the policy
must support, not whether CSP runs. Local development must remain a trustworthy
proxy for the released app, with necessary concessions explicit and minimal.

## Strategy

Use controlled local browser evidence to develop and verify one CSP implementation
for the released app. No additional AWS resources, deployed collection service,
new gateway methods, roles or log groups are authorized. Do not make field-report
collection a prerequisite for policy improvement. The withdrawn collector
implementation and its opt-in flag are removed. Do not reintroduce a report receiver as preparation for local discovery.

Start with the source inventory and release import map already captured. Observe
candidate policies through real browser violation events and request evidence
while exercising the normal local app with release-generated assets. Any one-off
event observation must start before application scripts; keep sanitized directive, disposition,
resource classification and counts, never raw URLs, queries, identifiers, samples
or console dumps. Preserve enough fixed dependency classification to make the
findings actionable. Disclose bounded capture windows, truncation and uncovered
flows; an empty capture does not prove the policy is compatible.

Use HTTP response headers for report-only trials: a CSP meta tag cannot substitute
for a report-only header. Keep maintained tests at existing application boundaries:
server response tests for policy construction and trusted markup, and normal
application browser tests for required behavior and unauthorized-script blocking.
Do not build a separate measurement subsystem or fixture-only CSP test category
as a prerequisite. One-off browser discovery can be documented without retaining
its source as maintained tooling. No browser report endpoint is needed.

Implement policy construction and script authorization at the document renderer
(`IndexRoute`). Derive application, asset and socket origins from existing
configuration. Prefer per-response nonces for explicitly trusted generated markup
and stable hashes for immutable inline blocks. Never stamp a nonce onto every
script encountered in already-rendered HTML. Evaluate loader trust, the import
map, GraphiQL's inherited iframe policy and Emotion styles against browser
results before selecting the final directives.

Cover meaningful configuration variations, including both states of the existing
prerelease flag, rather than duplicating the suite for environment names. Keep
normal local behavior faithful to release; declare necessary simulated-service or
browser concessions. Real integration checks and deployed header verification
close those specific gaps through existing infrastructure. They are not an
environment promotion sequence or a new telemetry project.

## Acceptance Evidence

- Capture release-generated HTML/import map and transitive resource requirements.
  Reuse existing evidence where release and configuration match. Distinguish
  document navigation, subresource origins and application connections.
- Verify policy delivery and behavior through existing server response and normal
  application browser tests. During discovery, sanitize any retained evidence and
  disclose bounded observation, truncation and pending requests.
- Exercise Welcome, authenticated party and contribution views, Auth0 return,
  Slack/Discord authorization navigation and return, GraphiQL query execution,
  WebSockets and incubating feature paths. Record concessions and unverified
  boundaries rather than claiming synthetic fixtures cover real integrations.
- Prove trusted scripts still execute and unauthorized test scripts are blocked
  through the actual document-rendering path using release assets. Verify nonce
  freshness, caching, configuration injection, import map and iframe behavior.
- Keep policy behavior common across local and deployed configurations. Verify
  delivered headers and real integration behavior after an authorized release,
  without provisioning resources or adding collection infrastructure.

## Safe Slice Sequence

1. **Use the existing evidence.** Review the source inventory and sanitized
   captures below. Document remaining dependency and integration gaps without
   adding a dedicated observer tool or tests of that tool.
2. **Develop policy through application tests.** Use the existing server response
   tests and normal application browser suite with release assets. Cover actual
   policy delivery, required application behavior and unauthorized resources.
   Keep configuration and capture scope explicit; reuse applicable evidence from
   [CPL-62](CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md).
3. **Authorize trusted scripts and tighten directives.** Deliver small useful
   renderer/policy slices with focused tests, using nonces or hashes and minimal
   ordinary configuration. Browser scenarios must catch missing authorization
   and unintended authorization. Address caching, import-map dependencies,
   server-injected configuration, GraphiQL bootstrap/inheritance, Emotion and
   mutable third-party loaders. Evaluate script, style, connect, frame, image,
   font, worker, object and document directives from observed requirements.
4. **Enforce the shared policy.** Once representative flows pass, enable the
   common enforcing header through the same application path. Verify required
   behavior and deliberately unauthorized resources in local browser regressions.
   Close real-service and supported-browser gaps; verify deployed header delivery
   after an authorized release. Document remaining compatibility exceptions and
   their reasons.

Each implementation slice follows the repository red–green–refactor and full
`./gradlew check` commit gate. Pushing, releasing and deployment require the
owner's authorization. No additional AWS resources are authorized. If credentials
prevent normal local-stack validation, retain the blocker and continue independent
fixture/unit work without bypassing the configured AWS identity.

## Evidence and Resume Note

**Resume · 2026-11-23:** Outgoing-history review removed the superseded report
collector, standalone server export, flag and local wiring. Server source,
Caddy and Compose configuration match the production baseline. The
outgoing content is documentation and sanitized research only. There are no
CSP source, build or test changes. Research remains evidence of bounded
discovery, not enforcement or full compatibility.

The CSP observer, its tool-only Node/Chrome tests, fixture extensions and build
wiring were also removed after the owner challenged maintaining a separate
measurement subsystem. They used existing runners, but tested the instrument
rather than application behavior. The one-off Welcome recorder and Gradle task
were removed rather than retained as unmaintained source. The experiment is
documented below. Next: design a
small, maintained document-renderer authorization/policy slice, with local
release-browser evidence and tests. Do not restore the discarded collector or
manual production-edit workflow as a prerequisite.

Before the application-owned collector, local Caddy supplied a report-only
policy without a receiver. Native Chrome on November 17 showed script, style,
font, connection and static.localhost manifest notices plus a cross-origin manifest start URL warning. Subsequent
captures below identified `IndexRoute` as the document-header owner and recorded
release and runtime evidence. Transitive coverage is still incomplete. Resume
with local browser observation and release-asset validation; reuse applicable
inventory rather than restarting baseline captures.
The [contributor closeout](../work_completed/CPL-57-WEB_CONTRIBUTOR_POPOVER_FOLLOWUP.md)
records native save-focus and owner-reported VoiceOver acceptance. The broader
calibrated accessibility matrix is tracked in CPL-56.

### Production baseline · 2026-11-20

- `./gradlew :client:jsBrowserProductionVite` succeeded at `60c09477d`. The
  22-entry import map in the generated HTML matched both the generated
  `build/js/packages/Coupling-client/cdn.json` and the live document's map,
  including version and dependency parameters. All entries point to
  `https://esm.sh`. This comparison establishes map parity for this capture.
- The public document at `https://coupling.zegreatrob.com/` returned HTTP 200
  with API Gateway and Express response markers and without CSP, CSP
  report-only, or reporting endpoint headers. The rewritten entry module at
  `https://assets.zegreatrob.com` returned HTTP 200 through CloudFront from S3
  without CSP headers. In repository code, Express `IndexRoute` serves the
  final document at the root and fallback routes, and the deployment passes a
  release-versioned asset URL to the API Lambda. Put document policy ownership
  at that response boundary so the header and rendered configuration can be
  tested together. Verify header forwarding through API Gateway in each deployment.
- A fresh headless Chrome 158 visit to the public route observed a single
  document navigation to the app origin. Script responses were from
  `esm.sh` (323), `assets.zegreatrob.com` (21),
  `kit.fontawesome.com` (1), and `www.googletagmanager.com` (1).
  Stylesheets came from the asset host, `cdnjs.cloudflare.com`, and
  `fonts.googleapis.com`. A font loaded from `fonts.gstatic.com`.
  Font Awesome fetched from `ka-f.fontawesome.com`, and analytics sent
  requests to `www.google-analytics.com`. The asset host also supplied
  images and another request classified as “other” by the browser.
  These are origin and resource-type observations from one session. A separate
  fresh session confirmed that the Welcome “Come on in!” control rendered
  and observed the same script counts.

### Public entry points · 2026-11-20

- In a fresh Chrome session, opening the production GraphiQL URL returned
  HTTP 200 without an enforcing CSP header. The editor iframe did not render
  within eight seconds in the signed-out session. No editor CDN requests were
  observed in that window. Signed-in editor loading and query execution remain
  required checks.
- From Welcome, opening the login chooser and selecting Login produced an
  Auth0 top-level navigation (HTTP 302 then 200). This records the outgoing
  navigation only. The authenticated return, Auth0 fetches and any silent
  authentication iframe need their own evidence.

### Prerelease gateway baseline · 2026-11-20

- The custom domain maps its root to the prerelease REST API stage. The deployed
  REST API exposes only `ANY /` and `ANY /{proxy+}`. Stage method settings are
  empty, access logging is unconfigured, and tracing is disabled. A fresh GET
  returned HTTP 200 without CSP, CSP report-only, or Reporting-Endpoints headers.
- The application's Lambda log group had no configured retention period. This
  was relevant to the withdrawn collection proposal; no report storage is planned.

These observations cover prerelease only; inspect the other deployment packages
and effective controls without assuming the same live state.

### Withdrawn collection approach · reviewed 2026-11-23

Earlier local commits added an opt-in report-only header, a same-origin report
route, bounded/sanitized report aggregation and a standalone Lambda export with
HTTP-boundary tests. A dedicated gateway route, role and log group were proposed
but never deployed. This expanded a CSP task into a reporting service without
providing the dependency evidence needed for policy design.

The owner rejected additional AWS resources and environment promotion gates.
The agreed direction is one application policy across ordinary configurations,
local browser observation, and tested script authorization/enforcement. The
outgoing history was rewritten to omit the collector implementation and its
receiver-only tests entirely. No dormant collector or collection feature flag
is retained. The original work is preserved only on the local backup branch
`backup/cpl52-before-outgoing-review-20261123`.

Modern Web Guidance `security` supports discovery before enforcement and warns
against blanket nonce stamping. Its generic deployed-reporting and script-sample
recommendations do not override the owner's local-first, no-new-resource and
sanitized-evidence constraints. Trusted Types and isolation policies are not
automatic additions to this card's scope.

### Discarded local observation instrument · 2026-11-21/22

The earlier implementation added a browser-side observer, covered by the
existing Node and Chrome diagnostic-fixture compilations. It installs before
navigation using WebdriverIO's preload API and aggregates genuine CSP events by
fixed directive, disposition and dependency resource label. Unknown strings
become `other`; no raw URLs, samples or policy text reach the snapshot. The
1,000-event cap exposes dropped-event counts. No report endpoint or AWS resources
are used and no application policy changed.

The two Node scenarios first failed the intended observed-count assertions
against a non-recording observer. They subsequently verified fixed known-origin matching,
same-origin classification, lookalike-host rejection, sensitive-field exclusion
and the event cap. Real Chrome fixtures prove that a nonce-authorized script
executes in both modes, while an unauthorized inline script executes under
report-only and is blocked under enforcement; each produces the expected real
violation disposition. This is instrument validation only, not release-app or
integration acceptance. This source and its tests are no longer retained.

The initial downloaded Chrome 158.0.8127.36 crashed during session creation.
Following the playbook, the cached browser was preserved and downloaded afresh;
both CSP browser scenarios then passed. The complete independent harness check
passed: 38 Node tests, five Chrome fixture tests and Kotlin lint. Test review
confirmed that a working page alone cannot pass the violation assertions; harness
code review confirmed fixed labels and bounded state, with no raw-event storage
or application/environment branches.

The first full checks found binding naming/annotation-spacing lint issues, which
were fixed. A later cached full check reported expired SSO, but identity and the
exact SSM read succeeded in the normal fish shell (only the parameter count was
printed). `fish -lc './gradlew check --no-daemon --no-configuration-cache'`
passed on November 22 in 1m 21s (697 tasks: 206 executed, one from cache,
490 up-to-date), including the application E2E suite. Fresh configuration avoided
the stale credential failure; no profile, permanent cache setting or AWS resource
changes were made. This historical validation does not justify keeping the
measurement subsystem. Supported-browser and real-integration coverage remain open.

### Normal-origin Welcome discovery · 2026-11-22

A one-off recorder observed the actual local release app at
`https://localhost/welcome/`. It installed the validated observer before
navigation, checked the exact delivered candidate header, required the visible
Welcome control, and captured a bounded two-second period afterward. No login,
party creation or saved-data mutation occurred. The recorder and Gradle task
were discarded during review because they depended on manually editing
production header code; they are not maintained repository tooling.

Two proposed delivery mechanisms were rejected during this slice. Chrome's BiDi
header overrides/response fulfillment acknowledged the candidate but a real
fixture still evaluated the original policy (a distinct `base-uri` probe failed).
A loopback proxy passed HTTP/fixture tests but changed the app origin, conflicting
with the static host's `https://localhost` CORS contract and preventing Welcome
readiness. That is a concrete example of why local identity/parity matters; no
CORS exceptions or browser-security bypasses were introduced. The proxy and its
tests were removed, not retained as another supported path. The sanitized
[rejected proxy attempt](../research/WEB_CSP_WELCOME_PROXY_REJECTED.json) remains
`partial` and is not application compatibility evidence.

For the accepted experiment, the existing `setCspReportOnlyHeaders` policy text
was temporarily changed to `default-src 'self'; base-uri 'none'; object-src
'none'; frame-ancestors 'self'`, using the then-existing local report-only opt-in
(now removed).
The candidate has no report-uri/report-to or script samples. The normal renderer
and local release stack delivered it at their real origin. After capture the
temporary production edit was restored; no production source diff remains.

The [accepted capture](../research/WEB_CSP_WELCOME_LOCAL_DISCOVERY.json) records
Chrome 158.0.8127.36, a measured 1280×700 viewport, local prerelease features
enabled and 22 import-map entries. The matching-header check passed. It captured
792 report-only events with zero drops, including three inline-script events,
646 esm.sh script events, 57 local-static script events, five inline-style
events, and known fonts, icon-kit and analytics requirements. These are violation
events, not counts of distinct resources: the network observer recorded 363
completed responses, including 323 esm.sh responses, with zero HTTP errors,
failed requests or pending requests at the snapshot. One image violation remains
`other` and needs investigation before choosing an image directive. Do not infer
the origin from that category or widen the policy speculatively.

This is a signed-out, single-session dependency discovery pass, not an enforcing
policy or compatibility proof. It independently supports authorizing trusted
script bootstraps and their module graph, while handling Emotion styles, font
loaders and analytics deliberately. Authenticated views, the existing prerelease
flag's off state, GraphiQL query execution, OAuth return and supported-browser
coverage remain open. Next: implement small, tested renderer authorization
patterns using this evidence. No additional AWS resources, feature flags,
deployment or field reporting are authorized/required.

The restored full check attempted on November 23 initially failed before tests
with an expired SSO token, and Docker was unavailable. During outgoing-history
review later that day, the normal stack started healthy and the focused Node,
Chrome fixture and full Gradle check passed (697 tasks, 1m 47s). That check
validated the inherited tree before removing the withdrawn code. Validation of
the final rewritten slice is recorded below; the experiment remains discovery
rather than application compatibility evidence.

### Outgoing-history review · 2026-11-23

The first review retained the observer because its tests proved bounded,
sanitized aggregation and real fixture report-only/enforcement behavior. The
owner then challenged the maintenance and test scope. Although those scenarios
used existing runners, they protected a new tool rather than the application's
CSP contract. The observer, bindings, fixture changes, tests and build input were
removed. Future implementation should add tests to the existing server and
application browser suites alongside the behavior they protect.

The initial reduced tree passed `fish -lc './gradlew check --no-daemon'` outside
the sandbox in 1m 50s (697 tasks). After removing the measurement subsystem,
`fish -lc './gradlew check --no-daemon'` passed again outside the sandbox in
1m 19s (697 tasks: 62 executed, nine from cache, 626 up-to-date), including the
normal application E2E suite. `git diff --check` passed. All source, test and
build files match `origin/master`.
The outgoing commit contains only this card, shared environment guidance and two
sanitized historical captures. The unrelated untracked IDE schema mapping
remains outside this slice.

### Policy design to verify

Keep package versions, dependency parameters, and shared React identity in the
generated import map. Evaluate deriving hashes for build-stable inline blocks
from the emitted release bytes and using a per-response nonce on the
server-generated config script. Verify that the delivered script bytes still
match the release hashes after rewriting and that the response path does not
share-cache nonce-bearing HTML. Release CDN assets can retain their own
caching behavior. Evaluate explicit script and connection origins against
report-only evidence before granting any third-party loader transitive trust.

### Signed-in sandbox capture · 2026-11-20

A fresh Chrome session covered interactive Auth0 sign-in and return, a party's
pair assignment and contribution views, and opening GraphiQL. The party had
Slack and Discord configured per its owner. The capture retained origins,
resource types, HTTP status codes and counts. Observations were aggregated at
session level, so a request cannot always be attributed to an individual view.

| Boundary | Observed evidence |
| --- | --- |
| Top-level navigation | Sandbox app and `zegreatrob.us.auth0.com`. Auth0-hosted sign-in loaded script and CSS from `cdn.auth0.com`. |
| Application connections | Four HTTP 200 responses from same-origin GraphQL, one successful fetch to `zegreatrob.us.auth0.com`, and a received WebSocket frame from `wss://socket.sandbox.coupling.zegreatrob.com`. |
| GraphiQL | Two script responses from `unpkg.com` and JS and CSS responses from `cdn.jsdelivr.net`. This confirms the editor's CDN requests were made. Query execution was not verified. |
| Images | `assets.zegreatrob.com`, `api.dicebear.com`, `robohash.org`, and `www.gravatar.com`, with image redirects reaching `i0.wp.com`, `i1.wp.com`, and `i2.wp.com`. |
| Fonts and telemetry | Fonts from `fonts.gstatic.com` and `ka-f.fontawesome.com`. Fetches reached `ka-f.fontawesome.com` and `www.google-analytics.com`. |

Before enforcement, verify Slack and Discord authorization navigation and return
with a disposable fixture, execute a GraphiQL query, and test policy reporting
and behavior across supported browsers. Auth0's hosted sign-in document has its
own subresources. Keep that navigation evidence separate from grants for the
Coupling document. Treat observed avatar hosts as compatibility evidence for
the sampled party because other parties can have different avatar sources.

### Source Inventory

| Owner/source | Loading behavior | CSP work to resolve |
| --- | --- | --- |
| `client/build.gradle.kts` (`lookupCdnUrls`) and `scripts/cdn-lookup/.../CdnProvider.kt` | Generates `cdn.json` in the Kotlin npm project directory, using versioned `https://esm.sh/` URLs and dependency/external query parameters. This is generated output, not a root-level checked-in file. | Capture the generated map for the release under review and its transitive module requests; package versions alone do not describe the complete request graph. |
| `client/cdn.settings.json`, `client/vite.config.mjs` | Selected dependencies are external to Rollup. Vite reads the generated map and injects imports/global assignments into HTML; serve mode substitutes development references. | Review production and serve outputs separately. Preserve React/shared dependency identity. The current local script allowlist omits `esm.sh`; enforcement cannot safely be copied from it. |
| `client/src/jsMain/resources/index.html` | Inline import map, inline module bridge, environment assignments, analytics initializer; external Font Awesome kit, Google Fonts CSS, versioned CodeMirror CSS, Google Tag Manager script. | Distinguish build-stable script hashes from response-specific config; inspect kit/font/analytics descendants. Existing SRI on CodeMirror does not authorize scripts under CSP. Mutable kit/analytics resources need an explicit strategy. |
| `server/.../express/route/IndexRoute.kt` | Fetches built HTML from `Config.clientUrl`, rewrites static asset URLs, injects a configuration script into head, and streams HTML. Config includes auth domain/client ID, basename, asset and socket hosts. | Per-response nonces, if selected, must coordinate the HTML response header and trusted script construction here. Do not blanket-authorize every script encountered during HTML rewriting. Static-host headers are not the application's document headers. |
| `client/.../graphql/GraphiQL.kt` | Sandboxed `srcDoc` iframe with scripts/same-origin allowed; React/ReactDOM 18.3.1 from unpkg, GraphiQL 3.8.3 JS/CSS from jsDelivr, inline bootstrap and style attributes. Versioned external resources have SRI. Fetcher is passed from the parent. | Test iframe policy inheritance and bootstrap explicitly. Noncing only the outer page is insufficient evidence that the embedded editor works. Retain introspection/query behavior without storing credentials or query content in reports. |
| Emotion-styled client/components | Runtime CSS generation is used throughout, including popup styling. | Inspect actual style elements and style attributes before removing `style-src 'unsafe-inline'`; script authorization does not resolve style authorization. |
| `Caddyfile`, mounted by `docker-compose.yml` | Caddy supplies the original local diagnostic report-only header. The static.localhost block also has a diagnostic header. Static host allows reads from localhost. Socket host proxies separately. | Local policy is observational, not enforced protection or proof of production header ownership. Capture headers at the deployed app and asset origins; establish production owner before editing rollout logic. |
