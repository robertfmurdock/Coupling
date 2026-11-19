# CPL-52 — Outcome

## Goal and Constraints

Design and safely enforce a Content Security Policy for the Coupling web
client, accounting for the repository's CDN lookup/template pipeline and all
runtime integrations. Preserve Auth0, Slack/Discord OAuth, analytics, GraphQL,
WebSocket, GraphiQL, and supported browser behavior.

This card is intentionally a lightweight draft. Flesh out the exact policy,
deployment ownership, browser-support target, and violation collection plan
before playing the card.

## Scope

- Inventory generated and inline scripts/styles from `client/vite.config.mjs`,
  `cdn.json`, the HTML template, server-injected configuration, and GraphiQL's
  `srcDoc`.
- Identify required `script-src`, `style-src`, `connect-src`, `frame-src`,
  `img-src`, `font-src`, `worker-src`, `base-uri`, `object-src`, and
  `frame-ancestors` sources.
- Define report-only rollout and violation collection/redaction, then promote
  to enforcement after authenticated, OAuth, integration, GraphiQL, and
  WebSocket flows are covered.
- Prefer nonces or static hashes over broad origin allowlists; document any
  unavoidable compatibility fallback.

## Planned Slices

- [ ] Reproduce and document the complete runtime resource inventory.
- [ ] Add report-only CSP observability and focused browser scenarios.
- [ ] Remove or nonce/hash inline execution and narrow CDN allowances.
- [ ] Enforce CSP with authenticated, OAuth, integration, GraphiQL, and
      WebSocket regression coverage.

## Flesh Out Before Playing

- Confirm whether Caddy, the server, or an upstream CDN owns production
  headers.
- Confirm the production browser-support policy and whether CSP Level 3
  features are acceptable.
- Choose a durable violation sink and retention/redaction rules; do not log
  tokens, authorization headers, or query-bearing URLs.
- Decide whether mutable analytics and Font Awesome/Google Fonts remain
  third-party or move behind a deliberate self-hosting/privacy decision.
- Capture a baseline of current CSP console violations on representative
  authenticated and unauthenticated routes.

## Validation Expectations

- Testmints browser scenarios name their starting resource/auth state, perform
  one navigation or integration action, and verify loaded resources plus
  absence or expected reporting of CSP violations.
- Run focused browser checks, then `fish -lc './gradlew check --no-daemon'`
  outside the sandbox before each complete implementation slice.
- Record policy changes, compatibility risks, residual violations, and commit
  IDs here.

## Initial Evidence

- Current deployment has a report-only policy in `Caddyfile` from the web best
  practices remediation card.
- The client uses generated CDN import metadata, inline HTML/template scripts,
  Auth0, analytics, GraphQL, WebSockets, and a GraphiQL `srcDoc`.
- No violation reporting endpoint is currently configured.
- Manual local Chrome review (2026-11-17) observed report-only notices for
  scripts, styles, fonts, connections, and the manifest hosted at static.localhost.
  The manifest also reports a cross-origin start_url warning. These are local
  observations, not a production-origin allowlist or evidence of enforcement;
  capture exact sanitized origins and required behavior before changing policy.


## Source Inventory Slice: 2026-11-19

Documentation-only investigation after contributor implementation `661b77836`.
No policy, resource loading, headers, UI, or deployment behavior changed. Native
browser access still fails with “Sky Computer Use native pipe startup failed”; this
is source evidence, not a complete runtime inventory. The first planned slice
remains unchecked until transitive resources and actual response headers are
captured. Contributor native save-focus and calibrated VoiceOver checks remain on
its own open card.

| Owner/source | Loading behavior | CSP work to resolve |
| --- | --- | --- |
| `client/build.gradle.kts` (`lookupCdnUrls`) and `scripts/cdn-lookup/.../CdnProvider.kt` | Generates `cdn.json` in the Kotlin npm project directory, using versioned `https://esm.sh/` URLs and dependency/external query parameters. This is generated output, not a root-level checked-in file. | Capture the generated map for the release under review and its transitive module requests; package versions alone do not describe the complete request graph. |
| `client/cdn.settings.json`, `client/vite.config.mjs` | Selected dependencies are external to Rollup. Vite reads the generated map and injects imports/global assignments into HTML; serve mode substitutes development references. | Review production and serve outputs separately. Preserve React/shared dependency identity. The current local script allowlist omits `esm.sh`; enforcement cannot safely be copied from it. |
| `client/src/jsMain/resources/index.html` | Inline import map, inline module bridge, environment assignments, analytics initializer; external Font Awesome kit, Google Fonts CSS, versioned CodeMirror CSS, Google Tag Manager script. | Distinguish build-stable script hashes from response-specific config; inspect kit/font/analytics descendants. Existing SRI on CodeMirror does not authorize scripts under CSP. Mutable kit/analytics resources need an explicit strategy. |
| `server/.../express/route/IndexRoute.kt` | Fetches built HTML from `Config.clientUrl`, rewrites static asset URLs, injects a configuration script into head, and streams HTML. Config includes auth domain/client ID, basename, asset and socket hosts. | Per-response nonces, if selected, must coordinate the HTML response header and trusted script construction here. Do not blanket-authorize every script encountered during HTML rewriting. Static-host headers are not the application's document headers. |
| `client/.../graphql/GraphiQL.kt` | Sandboxed `srcDoc` iframe with scripts/same-origin allowed; React/ReactDOM 18.3.1 from unpkg, GraphiQL 3.8.3 JS/CSS from jsDelivr, inline bootstrap and style attributes. Versioned external resources have SRI. Fetcher is passed from the parent. | Test iframe policy inheritance and bootstrap explicitly. Noncing only the outer page is insufficient evidence that the embedded editor works. Retain introspection/query behavior without storing credentials or query content in reports. |
| Emotion-styled client/components | Runtime CSS generation is used throughout, including popup styling. | Inspect actual style elements and style attributes before removing `style-src 'unsafe-inline'`; script authorization does not resolve style authorization. |
| `Caddyfile`, mounted by `docker-compose.yml` | Local localhost/static.localhost blocks carry report-only CSP; no report endpoint is configured. Static host allows reads from localhost. Socket host proxies separately. | Local policy is observational, not enforced protection or proof of production header ownership. Capture headers at the deployed app and asset origins; establish production owner before editing rollout logic. |

### Bounded Next Slices

1. Capture sanitized release-generated HTML/import map plus native network and
   violation evidence for Welcome, authenticated party/contributions, GraphiQL,
   Auth0 return, WebSocket, and integration navigation. Record only origin/path
   categories; strip tokens, queries, participant data and identifiers. Separate
   navigation destinations from subresource/connect requirements.
2. Select the production header owner and report collector. Define retention,
   redaction, request size/rate limits and endpoint tests before receiving reports.
   Script samples can contain sensitive content: decide whether to omit them or
   redact them before persistence; do not blindly log raw CSP bodies.
3. Design nonce/hash handling against the actual generated module/import-map and
   server-injection paths, including caching and GraphiQL. Test report-only first;
   measure legitimate violations before choosing enforcement. No unrelated COOP,
   Trusted Types, or dependency-hosting migration is bundled into this inventory.

Reviewed Modern Web Guidance `security`, especially “Enumerate inline scripts and
styles” and “Do not blindly apply strict policies to an existing application.”
Validation: source paths and generation flow inspected; documentation diff checked.
No tests invented or suite rerun for this documentation-only slice. Runtime
inventory, production ownership, reporting and enforcement remain unfinished.
