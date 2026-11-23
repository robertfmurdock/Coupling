# CPL-52 — Enforce a compatible content security policy

**Work card protocol: 2**

## Goal and Ownership

Implement and safely enforce a Content Security Policy for the Coupling web
client. This card owns CSP policy construction, trusted-script authorization,
compatibility checks and enforcement. Preserve Auth0, Slack/Discord OAuth,
analytics, GraphQL, WebSockets, GraphiQL and supported browser behavior.

Use the same application path across local, sandbox, prerelease and production,
with ordinary configured origins. CSP is baseline behavior, independent of the
existing prerelease flag; test the features enabled by both flag states. Follow
[environment conventions](../context/ARCHITECTURE_CANONICAL.md#build-and-deployment).

## Related Card Disposition

- This is the sole active CSP owner. The completed
  [CPL-51 review](../work_completed/CPL-51-WEB_BEST_PRACTICES_REVIEW_REMEDIATION.md)
  delivered the original local report-only starting point and transferred CSP
  hardening here; its completed work is not reopened.
- [CPL-62](CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md) remains a separate
  performance/CDN follow-up. Policy can cover the current dependency graph without
  deferring CDN loading. Neither card is a prerequisite for the other; reuse
  applicable inventories and coordinate actual shared-source changes.
- [CPL-54](../work_completed/CPL-54-WEB_PERFORMANCE_MEASUREMENT_FOLLOWUP.md) is
  completed performance evidence. [CPL-63](../work_completed/CPL-63-ROUTE_SCOPED_CDN_LOADING.md)
  remains completed as a redirect to CPL-62. Neither owns unfinished CSP work.
- Collector and observation-subsystem proposals are will-not-do work recorded
  below, not additional cards or future deliverables.

## Design and Constraints

- Construct document policy at `IndexRoute`, which serves the final HTML and
  injects runtime configuration. Derive application, asset and socket origins
  from existing configuration. Static-host headers do not establish document policy.
- Authorize explicitly trusted generated scripts using per-response nonces or
  hashes of immutable emitted bytes. Never stamp nonces onto every script found
  in rendered HTML. Verify import-map, configuration injection and cache behavior.
- Resolve third-party loader trust, GraphiQL iframe inheritance/bootstrap and
  Emotion styles from actual requirements. Preserve package versions, dependency
  parameters and shared React identity. Do not widen policy to hide unknowns.
- Use response headers for report-only discovery; a meta tag cannot supply a
  report-only policy. Discovery evidence is not enforcement or compatibility proof.
- Use existing server response tests and the normal application browser suite.
  Add tests alongside application behavior, not a separate CSP observation tool,
  synthetic instrument suite or reporting subsystem. Follow
  [tooling purpose and lifetime](../context/GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime).
- No new report receiver, deployed collection service, AWS resources, gateway
  methods, roles, log groups or feature flags. Field telemetry is not a prerequisite.
  Do not restore the discarded manual-production-edit experiment workflow.
- Retain only sanitized findings: fixed dependency/directive categories and
  counts, never raw URLs, identifiers, queries, samples or console dumps. Record
  capture conditions, truncation, pending requests and uncovered flows.

## Acceptance Evidence

- Release HTML/import map and required transitive resources are understood;
  distinguish document navigation, subresources and application connections.
- Existing server tests verify delivered policy, trusted markup, nonce freshness
  or hash matching, configuration variations and HTML caching behavior.
- Normal application browser tests prove required scripts execute and deliberately
  unauthorized scripts are blocked through the actual document-rendering path
  with release assets. Report-only discovery must preserve required behavior.
- Exercise Welcome, authenticated party/contribution views, Auth0 return,
  Slack/Discord authorization navigation and return, GraphiQL query execution,
  WebSockets and incubating features. Record real-service/browser gaps explicitly.
- After an authorized release, verify delivered headers and real integration
  behavior through existing infrastructure. No environment promotion sequence.

## Safe Slice Sequence

1. **Authorize trusted application scripts.** Read the existing renderer tests and
   source inventory; reuse the evidence below. Declare a small useful renderer
   contract and failing scenarios before edits. Implement nonce/hash authorization
   with its cross-layer changes and tests in existing suites. Resolve import-map,
   configuration and caching requirements for that slice.
2. **Tighten directives in useful increments.** Use actual application behavior
   and bounded browser evidence to select script, style, connect, frame, image,
   font, worker, object and document directives. Test required integrations and
   unauthorized resources; do not make building measurement tools a delivery step.
3. **Enforce the shared policy.** Once representative flows pass, enable the
   enforcing header through the same document path. Close real-service and
   supported-browser gaps, explain necessary compatibility exceptions, and verify
   deployed delivery after an authorized release.

Each implementation slice follows [AGENTS.md](../../AGENTS.md), including focused
red–green–refactor and the full `./gradlew check` commit gate. No artificial red
is required for documentation. Pushing, release and deployment need authorization.

## Existing Evidence and Limits

### Production and entry points · 2026-11-20

- Release generation at `60c09477d` produced a 22-entry import map matching the
  generated `cdn.json` and live document, including dependency/version parameters.
  Every entry used `esm.sh`; transitive requirements still need contextual review.
- The public document returned HTTP 200 through API Gateway/Express without CSP,
  report-only or reporting endpoint headers. Asset responses came through
  CloudFront/S3. Code inspection identified `IndexRoute` as document-header owner.
- One fresh Chrome visit observed 323 esm.sh script responses, 21 asset-host
  scripts, Font Awesome and Tag Manager loaders; CSS/fonts came from the asset
  host, cdnjs, Google Fonts and Font Awesome. Analytics reached Google Analytics.
  These are session observations, not a complete dependency allowlist.
- Login navigated to Auth0. Signed-out GraphiQL did not render within eight
  seconds. Neither observation proves authenticated-return or query compatibility.
- Prerelease exposed only the existing root/proxy API routes and no CSP headers.
  This historical check does not establish other deployments' current state.

### Signed-in sandbox · 2026-11-20

The session covered Auth0 sign-in/return, pair assignments, contributions and
opening GraphiQL. Aggregation was session-wide, so requests cannot always be
attributed to a view. Same-origin GraphQL returned four HTTP 200 responses, an
Auth0 fetch succeeded and a socket frame was received. GraphiQL requested unpkg
and jsDelivr scripts/styles, but query execution was not verified.

Images included the asset host, DiceBear, Robohash, Gravatar and WordPress image
redirect hosts. Fonts/telemetry included Google Fonts, Font Awesome and Google
Analytics. Other parties can use other avatar sources. Auth0's hosted document
has its own subresources; those navigations do not justify Coupling policy grants.
Slack/Discord OAuth return and the supported-browser matrix remain unverified.

### Welcome dependency discovery · 2026-11-22

The [normal-origin capture](../research/WEB_CSP_WELCOME_LOCAL_DISCOVERY.json)
used release assets at `https://localhost/welcome/`, Chrome 158.0.8127.36, measured
1280×700 viewport, prerelease features enabled and 22 import-map entries. It
checked the exact response-header candidate `default-src 'self'; base-uri 'none';
object-src 'none'; frame-ancestors 'self'`, without reporting endpoints or samples.
The window ended two seconds after the Welcome control appeared.

It recorded 792 report-only events with zero drops: three inline-script events,
646 esm.sh script events, 57 local-static script events, five inline-style events
and known font/icon/analytics requirements. These are events, not distinct
resources. There were 363 completed network responses (323 esm.sh), with no
recorded HTTP errors, failures or pending requests. One image category was
`other`; investigate before selecting an image grant.

This signed-out discovery pass does not establish enforcement, authenticated
compatibility, the prerelease-off configuration or GraphiQL/OAuth coverage. Its
one-off recorder and experimental header edits are not maintained source.
The [rejected proxy attempt](../research/WEB_CSP_WELCOME_PROXY_REJECTED.json) is
partial evidence only: changing the origin broke the static host's CORS contract.
BiDi response overriding also failed to prove browser policy replacement. No
CORS exceptions or browser-security bypasses were introduced.

## Disposition of Discarded Work

Earlier local work explored a collector, standalone server export and opt-in
header, then a browser observation subsystem with tool-only tests. All were
removed from outgoing source, tests, fixtures and build wiring. No collector was
deployed. The owner rejected the infrastructure expansion and ongoing maintenance
of exploratory tooling. This record preserves conclusions, not recipes to rebuild
those systems. The original local history is on
`backup/cpl52-before-outgoing-review-20261123`.

The prior documentation-only slice `bf54e769d` passed the full check after removal
of CSP tooling (697 tasks, 1m 19s), including normal application E2E. No CSP
implementation is currently delivered by that slice. This follow-up consolidates
the plan and shared tooling guidance. `fish -lc './gradlew check --no-daemon'`
passed outside the sandbox (697 tasks, 24s: 56 executed, 641 up-to-date).
Local document links and `git diff --check` also passed. This follow-up changes
only documentation; no source, tests, build logic or runtime behavior changed.

## Source Inventory

| Owner/source | Loading behavior | CSP work to resolve |
| --- | --- | --- |
| `client/build.gradle.kts` (`lookupCdnUrls`) and `scripts/cdn-lookup/.../CdnProvider.kt` | Generates `cdn.json` in the Kotlin npm project directory, using versioned `https://esm.sh/` URLs and dependency/external query parameters. This is generated output, not a root-level checked-in file. | Capture the generated map for the release under review and its transitive module requests; package versions alone do not describe the complete request graph. |
| `client/cdn.settings.json`, `client/vite.config.mjs` | Selected dependencies are external to Rollup. Vite reads the generated map and injects imports/global assignments into HTML; serve mode substitutes development references. | Review production and serve outputs separately. Preserve React/shared dependency identity. The current local script allowlist omits `esm.sh`; enforcement cannot safely be copied from it. |
| `client/src/jsMain/resources/index.html` | Inline import map, inline module bridge, environment assignments, analytics initializer; external Font Awesome kit, Google Fonts CSS, versioned CodeMirror CSS, Google Tag Manager script. | Distinguish build-stable script hashes from response-specific config; inspect kit/font/analytics descendants. Existing SRI on CodeMirror does not authorize scripts under CSP. Mutable kit/analytics resources need an explicit strategy. |
| `server/.../express/route/IndexRoute.kt` | Fetches built HTML from `Config.clientUrl`, rewrites static asset URLs, injects a configuration script into head, and streams HTML. Config includes auth domain/client ID, basename, asset and socket hosts. | Per-response nonces, if selected, must coordinate the HTML response header and trusted script construction here. Do not blanket-authorize every script encountered during HTML rewriting. Static-host headers are not the application's document headers. |
| `client/.../graphql/GraphiQL.kt` | Sandboxed `srcDoc` iframe with scripts/same-origin allowed; React/ReactDOM 18.3.1 from unpkg, GraphiQL 3.8.3 JS/CSS from jsDelivr, inline bootstrap and style attributes. Versioned external resources have SRI. Fetcher is passed from the parent. | Test iframe policy inheritance and bootstrap explicitly. Noncing only the outer page is insufficient evidence that the embedded editor works. Retain introspection/query behavior without storing credentials or query content in reports. |
| Emotion-styled client/components | Runtime CSS generation is used throughout, including popup styling. | Inspect actual style elements and style attributes before removing `style-src 'unsafe-inline'`; script authorization does not resolve style authorization. |
| `Caddyfile`, mounted by `docker-compose.yml` | Caddy supplies the original local diagnostic report-only header. The static.localhost block also has a diagnostic header. Static host allows reads from localhost. Socket host proxies separately. | Local policy is observational, not enforced protection or proof of production header ownership. Capture headers at the deployed app and asset origins; establish production owner before editing rollout logic. |
