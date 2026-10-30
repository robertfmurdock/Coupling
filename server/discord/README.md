# Local Discord simulation

`DISCORD_MODE` selects `live` (the default when absent) or `simulated`. Unknown
values fail configuration loading. The standard local Serverless configuration
selects `simulated`; set `DISCORD_MODE=live` explicitly to override it. Deployed
stage configurations without a mode continue to use live Discord.

Simulation replaces only Discord HTTP responses. The application still uses
`DiscordClient`, `ClientDiscordRepository`, GraphQL authorization and commands,
and the configured integration storage. It never sends requests to Discord and
does not require real Discord credentials.

Use a synthetic OAuth code `simulated:<destination>`, where the destination is a
nonempty sequence of ASCII letters, digits, underscores, or hyphens. For example,
`simulated:local-channel` returns webhook `simulated-webhook-local-channel`, channel
`local-channel`, guild `simulated-guild-local-channel`, and deterministic synthetic
credentials. Other codes return `invalid_grant`. Invoke the existing
`grantDiscordAccess` mutation through the SDK with that code and your local party
ID; the simulator does not provide a browser OAuth consent page.

Message sends return IDs `simulated-message-<destination>-<hash>`, where the hash
is derived deterministically from the webhook and request body. Endpoint tests can
observe the announcement destination through the existing pairing-set query.
Updates retain the supplied message ID; deletes return success. These operations
are stateless responses, not a simulated Discord message store. Do not use this
mode to verify Discord's OAuth, credential expiry, code reuse, or delivery behavior.
