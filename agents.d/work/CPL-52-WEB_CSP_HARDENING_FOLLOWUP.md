# CPL-52 — Enforce a compatible content security policy

**Work card protocol: 2**

## Goal and Constraints

Design and safely enforce a Content Security Policy for the Coupling web
client, accounting for the repository's CDN lookup/template pipeline and all
runtime integrations. Preserve Auth0, Slack/Discord OAuth, analytics, GraphQL,
WebSocket, GraphiQL, and supported browser behavior.

## Acceptance Evidence

- Capture the release-generated HTML/import map, transitive requests, and actual
  deployed headers. Cover Welcome, authenticated party and contribution views,
  Auth0 return, integration navigation, GraphiQL, and WebSockets. Distinguish
  document, navigation, and subresource origins.
- Verify report-only collection with sanitized reports. Establish header
  ownership, browser support, retention, request size and rate limits first.
  Keep tokens, identifiers, queries and raw script samples out of stored evidence.
- Demonstrate enforcement with real browser regressions across those flows,
  including Slack and Discord OAuth and GraphiQL query execution. Record any
  required compatibility exception and the reason for it.

## Safe Slice Sequence

1. Extend the source inventory below with sanitized runtime evidence. Reuse the
   release import map and request evidence in [CPL-62](CPL-62-AUTHENTICATED_PERFORMANCE_CALIBRATION.md)
   where conditions match. The resulting inventory supports either project.
2. Add report-only collection at the confirmed document-header owner. Test that
   an expected report is retained in sanitized form and sensitive fields are
   excluded. The current browsing contract continues during collection.
3. Authorize trusted scripts using nonces or stable hashes where supported.
   Verify caching, server-injected configuration, the import map, Emotion
   styles and the GraphiQL iframe. Use report-only evidence to test changes
   to script, style, connect, frame, image, font, worker, object and document
   directives before enforcement. Resolve treatment of mutable analytics,
   Font Awesome and Google Fonts.
4. Enable enforcement once representative flows pass without unexpected
   violations. The browser scenarios should expose breakage from a policy that
   blocks a required resource or allows an unauthorized test resource.

## Evidence and Resume Note

Local Caddy carries report-only headers without a report collector. Native Chrome
on November 17 showed script, style, font, connection and static.localhost
manifest notices plus a cross-origin manifest start URL warning. Production
header ownership and transitive runtime coverage remain open. Start by capturing
sanitized headers and network evidence from a representative release build.
The [contributor closeout](../work_completed/CPL-57-WEB_CONTRIBUTOR_POPOVER_FOLLOWUP.md)
records native save-focus and owner-reported VoiceOver acceptance. The broader
calibrated accessibility matrix is tracked in CPL-56.

### Source Inventory

| Owner/source | Loading behavior | CSP work to resolve |
| --- | --- | --- |
| `client/build.gradle.kts` (`lookupCdnUrls`) and `scripts/cdn-lookup/.../CdnProvider.kt` | Generates `cdn.json` in the Kotlin npm project directory, using versioned `https://esm.sh/` URLs and dependency/external query parameters. This is generated output, not a root-level checked-in file. | Capture the generated map for the release under review and its transitive module requests; package versions alone do not describe the complete request graph. |
| `client/cdn.settings.json`, `client/vite.config.mjs` | Selected dependencies are external to Rollup. Vite reads the generated map and injects imports/global assignments into HTML; serve mode substitutes development references. | Review production and serve outputs separately. Preserve React/shared dependency identity. The current local script allowlist omits `esm.sh`; enforcement cannot safely be copied from it. |
| `client/src/jsMain/resources/index.html` | Inline import map, inline module bridge, environment assignments, analytics initializer; external Font Awesome kit, Google Fonts CSS, versioned CodeMirror CSS, Google Tag Manager script. | Distinguish build-stable script hashes from response-specific config; inspect kit/font/analytics descendants. Existing SRI on CodeMirror does not authorize scripts under CSP. Mutable kit/analytics resources need an explicit strategy. |
| `server/.../express/route/IndexRoute.kt` | Fetches built HTML from `Config.clientUrl`, rewrites static asset URLs, injects a configuration script into head, and streams HTML. Config includes auth domain/client ID, basename, asset and socket hosts. | Per-response nonces, if selected, must coordinate the HTML response header and trusted script construction here. Do not blanket-authorize every script encountered during HTML rewriting. Static-host headers are not the application's document headers. |
| `client/.../graphql/GraphiQL.kt` | Sandboxed `srcDoc` iframe with scripts/same-origin allowed; React/ReactDOM 18.3.1 from unpkg, GraphiQL 3.8.3 JS/CSS from jsDelivr, inline bootstrap and style attributes. Versioned external resources have SRI. Fetcher is passed from the parent. | Test iframe policy inheritance and bootstrap explicitly. Noncing only the outer page is insufficient evidence that the embedded editor works. Retain introspection/query behavior without storing credentials or query content in reports. |
| Emotion-styled client/components | Runtime CSS generation is used throughout, including popup styling. | Inspect actual style elements and style attributes before removing `style-src 'unsafe-inline'`; script authorization does not resolve style authorization. |
| `Caddyfile`, mounted by `docker-compose.yml` | Local localhost/static.localhost blocks carry report-only CSP; no report endpoint is configured. Static host allows reads from localhost. Socket host proxies separately. | Local policy is observational, not enforced protection or proof of production header ownership. Capture headers at the deployed app and asset origins; establish production owner before editing rollout logic. |
