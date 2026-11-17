# Outcome

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
