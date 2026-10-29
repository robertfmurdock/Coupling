# DynamoDB schema proposal

## Decision

Replace the present table-per-record-history model with four workload-oriented
tables. Store the current state separately from immutable revision history, use
time-bucketed tables for the two unbounded party timelines, and expose
pagination or summaries for GraphQL fields that otherwise grow without bound.

This is a target-state design. It intentionally does **not** prescribe a
migration, rollout sequence, or implementation work.

The design is for Coupling's observed domain: parties normally have three to
fourteen active people. It does not pre-shard ordinary party data. It does,
however, prevent history from making current reads slower and establishes
bounded paths before contribution and pairing history become large.

## Evidence used

The primary source of demand is the production client document
[`client/src/commonMain/graphql/queries.graphql`](client/src/commonMain/graphql/queries.graphql).
It represents requests made by real application pages, unlike
`sdk/src/commonMain/graphql/test-queries.graphql`, which is useful for
contract coverage but is not treated as a production-traffic signal.

The server action layer confirms several important consequences of those
documents:

- `partyList` combines a user's authorized party IDs and player memberships,
  then loads party records. The current Dynamo implementation scans the
  `TRIBE` table even when it has a set of requested IDs.
- Party configuration, players, pins, secrets, integration, and pairing sets
  are written as timestamped revisions. Normal reads query the entire entity
  or party history, sort it in memory, and select the latest record.
- Contribution windows query all records in a party partition and apply a
  non-key filter for the requested date window. Pair reports then filter the
  resulting contribution list in application memory.
- The client Statistics and History pages request pairing history as an
  unbounded list. Global statistics currently loads every party and every
  party's pairing history for a year.
- `LIVE_CONNECTION` uses a single `entityType = USER_CONNECTION` partition
  key. Looking up a connection ID also queries that whole logical partition
  and filters afterward.

These patterns conflict with AWS guidance to model from access patterns and
use `Query` or `GetItem` in application paths rather than scans. A filter
expression reduces returned data, not the capacity read. AWS also recommends
high-cardinality partition keys and carefully sized index projections:
[query and scan guidance](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-query-scan.html),
[partition-key guidance](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-design.html),
and [GSI projection guidance](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html).

## Target tables

All keys below use UTF-8 strings. `#` separates stable key components; values
must either be IDs that cannot contain `#` or be encoded before being placed in
a key. `revisionId` is a monotonically sortable ULID generated at the write
boundary. Timestamps are UTC ISO-8601 strings in sort keys and Unix epoch
seconds only for `expiresAt` TTL attributes.

### 1. `coupling-core`

This is the current-state and relationship read model. Its primary key is
`PK` / `SK`. It holds only the latest mutable representation, so routine
configuration and relationship reads do not grow with audit retention.

| Item type | PK | SK | Purpose |
| --- | --- | --- | --- |
| User profile | `USER#<userId>` | `PROFILE` | Current user identity, connected emails, subscription references, and preferences. |
| Validated user email | `USER#<userId>` | `VALIDATED_EMAIL#<normalizedEmail>` | An authenticated user's current primary or connected identity. "Validated" applies to the user identity, not to a Player field. |
| Email alias | `EMAIL#<normalizedEmail>` | `USER#<userId>` | Direct lookup of the user owning a validated primary or connected email. It is a relationship item, not a GSI. |
| Party metadata | `PARTY#<partyId>` | `META` | Current party configuration and small page-header summary. |
| Owned party | `USER#<userId>` | `OWNED_PARTY#<partyId>` | Current owner-to-party relationship and party-list summary. |
| Email player-party lookup | `EMAIL#<eligiblePlayerEmail>` | `PLAYER_PARTY#<partyId>#<playerId>` | Derived lookup for every normalized primary or additional email on an active player. Player-email verification is not required; this preserves the existing team-access rule without a player-table scan. |
| Current player | `PARTY#<partyId>` | `PLAYER#<playerId>` | Active player profile. |
| Retired player | `PARTY#<partyId>` | `RETIRED_PLAYER#<playerId>` | Retired player profile, separated so the common player list does not filter it. |
| Pin | `PARTY#<partyId>` | `PIN#<pinId>` | Current pin configuration. |
| Party integration | `PARTY#<partyId>` | `INTEGRATION#SLACK` | Slack team and channel configuration. |
| Party secret metadata | `PARTY#<partyId>` | `SECRET#<secretId>` | Description, creation/use timestamps, and encrypted-secret reference. |
| Current pairing pointer | `PARTY#<partyId>` | `CURRENT_PAIRING` | Pointer and compact summary of the current pairing set. |
| User boost | `USER#<userId>` | `BOOST` | Current entitlement and expiry. |
| Party boost view | `PARTY#<partyId>` | `BOOST#<userId>` | Minimal current boost projection for party pages. |
| Mutation receipt | `ACTOR#<actorId>` | `MUTATION#<idempotencyKey>` | Request fingerprint, accepted status, and replayable outcome for a mutation; `expiresAt` is 24 hours after acceptance. |
| Global shard summary | `GLOBAL#STATS#<year>#SHARD#<0-15>` | `SUMMARY` | Additive non-sensitive totals for one of 16 deterministic party-ID-hash shards. |
| Global party-stat projection | `GLOBAL#STATS#<year>#SHARD#<0-15>` | `PARTY#<partyId>` | Approved non-sensitive per-party statistic fields only, held in the party's deterministic shard and ordered by party ID. |

The global shard is `SHA-256(UTF-8(partyId))[0] & 0x0f`, expressed as a decimal
integer from `0` through `15`. This function is part of the stored-key contract;
all writers and readers use it rather than choosing a language-specific hash.

`partyList` queries the user's `OWNED_PARTY#` rows and each validated user email
for `PLAYER_PARTY#` rows, unions the party IDs, then uses `BatchGetItem` for
party metadata not already projected into the list rows. A resolver pages the
relationship queries and chunks `BatchGetItem` requests internally, but returns
the full existing unbounded GraphQL list by deliberate product choice. This
preserves Coupling's current policy: a party is visible to an owner or to an
authenticated user whose primary or connected identity matches any email on an
active player. It does not introduce a replacement role or permission system.
`party(id)` is
`GetItem(PARTY#id, META)`. Party configuration pages use one `BatchGetItem`
for `META` and the handful of directly-addressed settings, plus bounded
party-partition queries for players, pins, and secrets.

An email alias is created for every normalized primary and connected user
email. The corresponding `EMAIL#... / PLAYER_PARTY#...` rows are created or
removed when an active player's primary or additional email set changes. A
player's additional email may be marked unvalidated in the Player model; that
does not prevent a validated user who controls that email from seeing the
party. Together these rows replace the user email GSI plus subsequent revision
lookup and the player email scan for user discovery and access checks. A
transaction condition prevents an email alias from silently being assigned to
an unrelated active user.

Secrets need not store secret material in a broadly readable item. Store an
encrypted ciphertext encrypted with a KMS key or, preferably for OAuth tokens
and comparable credentials, a Secrets Manager reference. The Core item holds
only the reference and non-sensitive metadata. Decryption authority belongs to
the narrow integration execution role, not the general GraphQL role.

### 2. `coupling-activity`

This table contains current timeline records and the materialized read models
needed by reports. Its primary key is `PK` / `SK`. A party's contribution and
pairing timelines are independently bucketed by calendar month.

| Item type | PK | SK | Purpose |
| --- | --- | --- | --- |
| Active contribution generation | `PARTY#<partyId>` | `ACTIVE_CONTRIBUTION_GENERATION` | Current generation number used by all contribution reads and writes. |
| Contribution locator | `PARTY#<partyId>#CONTRIB#GEN#<generation>` | `CONTRIBUTION#<contributionId>` | Canonical current location: effective timestamp, bucket, and version. |
| Contribution timeline | `PARTY#<partyId>#CONTRIB#GEN#<generation>#<YYYY-MM>` | `AT#<effectiveTimestamp>#<contributionId>` | Full current contribution payload ordered by integration time, falling back to authored time. |
| Pairing locator | `PARTY#<partyId>` | `PAIRING#<pairingSetId>` | Canonical current location and pairing date. |
| Pairing timeline | `PARTY#<partyId>#PAIRING#<YYYY-MM>` | `AT#<pairingDate>#<pairingSetId>` | Immutable pairing snapshot for history pages. |
| Pair aggregate | `PARTY#<partyId>` | `PAIR#<canonicalPlayerPair>` | Counts and recent-pairing values used by Statistics and pairing views. |
| Party-year aggregate | `PARTY#<partyId>` | `STATS#<year>` | Precisely defined additive values (such as spin count) used by pairing views and global shard totals. |
| Contribution window summary | `PARTY#<partyId>#CONTRIB#GEN#<generation>` | `SUMMARY#<YYYY-MM>` | Precisely defined additive values, such as count and cycle-time-present count, for overview queries. |

`effectiveTimestamp` is a persisted, non-null value chosen at write time:
`integrationDateTime`, else `dateTime`, else the write timestamp. This removes
the current query's `integrationDateTime OR dateTime` filter and makes the
desired chronology a key condition.

For a contribution save, first strongly read the active generation and its
locator. A transaction deletes the prior timeline item when the bucket or sort
key changes, writes the new timeline item and locator, updates defined
additive summaries, and writes the immutable history record. A delete writes a
tombstone in History and removes the current locator/timeline items; current
reads therefore never need to filter `isDeleted` rows. `clearContributions`
increments `ACTIVE_CONTRIBUTION_GENERATION` and writes its History event in one
transaction. Reads immediately select only the new generation; an idempotent
worker later removes obsolete-generation rows without altering audit history.

`saveContribution` accepts either one contribution or a list. A one-item save
uses the transaction above. A bulk save snapshots the active generation, then
processes contribution IDs in bounded, retryable chunks; each entry has a
stable item-derived idempotency key and a generation condition. The list is not
one all-or-nothing DynamoDB transaction, matching the current sequential-list
behavior: completed entries can be visible before a later entry fails. A retry
replays only unfinished entries. If a clear advances the generation, remaining
chunks fail their condition and the caller retries them in the new generation;
old-generation partial work is immediately invisible. Contribution chunks do
not update pairing locators, pair aggregates, current-pair pointers, or global
statistics, so they are orthogonal to normal coupling operations. Monthly
summary contention is limited to concurrent contribution work in the same
party/month and is retried with normal conditional-write backoff.

For a pairing save, write the immutable timeline row, locator/current pointer,
affected pair aggregates, party-year aggregate, deterministic global-year
shard aggregate, and History record in one transaction when the write set fits
DynamoDB's limits.
For unusually large fan-outs, make the source pairing row authoritative and
apply derived aggregate updates idempotently through a stream consumer; the
GraphQL response must then declare that aggregate results are eventually
consistent. Current party sizes make the transactional path the expected one.

Monthly buckets deliberately bound each query partition while retaining a
simple date range. A Week, Month, Quarter, HalfYear, or Year contribution
window fans out only to the intersecting months, queries each with
`BETWEEN`/prefix key conditions, merges descending streams, and stops once a
client limit is satisfied. `All` is read through internal cursor pagination;
the existing GraphQL field remains compatible even though its unbounded response
cannot become predictably small merely by changing storage.

Exact non-additive values are not represented by an unspecified scalar summary.
For a requested party/year or contribution window, medians, distinct
players/pins, and contributor-derived values are calculated from the relevant
month-bucket source timeline rows. This preserves exact current behavior while
keeping a date-bounded request bounded; an existing `All` request deliberately
retains its unbounded compatibility behavior. Additive summaries remain
available for queries that need only counts or recent values.

### 3. `coupling-history`

This is the immutable revision and audit table. Its primary key is `PK` / `SK`.
It is not consulted to answer routine GraphQL current-state fields.

| Item type | PK | SK | Contents |
| --- | --- | --- | --- |
| Entity revision | `ENTITY#<kind>#<scope>#<id>` | `REV#<revisionId>` | Before/after or canonical revision payload, actor, action, timestamp, and optimistic version. |
| Party audit event | `PARTY#<partyId>#AUDIT#<YYYY-MM>` | `AT#<timestamp>#<revisionId>` | Compact event projection for a future party audit page. |

Every mutable Core row and every Activity locator write emits exactly one
entity-revision item. Pairing and contribution timeline rows are already
immutable domain events; their audit item records mutation metadata and any
later correction/deletion rather than duplicating a large snapshot needlessly.
The transaction uses `attribute_not_exists(PK)` on the revision key, an
expected version condition on the current item, and a conditional mutation
receipt write. Every mutation request carries an `Idempotency-Key` HTTP header;
the client/SDK creates one stable key per intended mutation, and a bulk save
derives each item key from its parent key plus contribution ID. The receipt
stores the request fingerprint and replayable outcome for 24 hours; a duplicate
key with the same fingerprint returns that outcome, while a different
fingerprint is rejected. DynamoDB's
`ClientRequestToken` is used only for transport retries within its service
window and is not the domain idempotency guarantee. DynamoDB transactions are
atomic within an account and Region and can touch up to 100 distinct items with
a 4 MB aggregate limit; writes beyond that boundary require an idempotent
derived-model workflow.
[AWS transaction reference](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/transaction-apis.html).

### 4. `coupling-live-connections`

This table isolates ephemeral WebSocket state from durable application data.

| Attribute | Value |
| --- | --- |
| Primary key | `PK = PARTY#<partyId>`, `SK = CONNECTION#<connectionId>` |
| Sparse GSI `byConnection` | `GSI1PK = CONNECTION#<connectionId>`, `GSI1SK = PARTY#<partyId>` |
| TTL | `expiresAt`, numeric Unix epoch seconds |
| Payload | Minimal player snapshot plus connection metadata required to publish or disconnect |

Listing a party's connections is a partition query. A disconnect callback
looks up its connection by GSI and then performs a direct delete. Refresh the
TTL heartbeat on successful use. The application must still delete immediately
when it receives an API Gateway disconnect: TTL is a backstop and DynamoDB may
remove an expired item days later, not at the expiry instant.
[AWS TTL behavior](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/TTL.html).

## Index policy

No local secondary indexes are proposed. They are unnecessary for the access
patterns above and would introduce the 10 GB item-collection constraint. The
only proposed GSI is the sparse `byConnection` GSI, whose values exist only on
live-connection rows. Its projection is `KEYS_ONLY` plus any minimal dispatch
identifier that avoids a base-table read.

The Core table uses relationship items instead of general-purpose email or
party indexes. This is intentional: a direct relationship row is smaller,
supports strong consistency on the base table, makes ownership explicit, and
does not replicate every future revision into an index. If a new cross-party
lookup cannot be expressed through one of these primary keys, introduce a
specific sparse GSI only after documenting its reader, write amplification,
projection, and expected key distribution.

## Production GraphQL access-pattern matrix

The rows below cover every production query operation in
`queries.graphql`. Fields combined by an operation are intentionally listed
together: GraphQL executes their resolvers independently today, but the target
resolver may use `BatchGetItem` or a bounded parallel query to avoid a
round-trip per field.

| Client operation and page | Current server path | Target read model and DynamoDB operation |
| --- | --- | --- |
| `partyListQuery`, `slackConnectPageQuery`, `incubatingQuery`; party list, Slack, incubating pages | `PartyListQuery` gathers owner IDs and player membership, then `DynamoPartyRepository.loadParties` scans `TRIBE`. | Query `USER#id` owned-party rows and every validated user email's `PLAYER_PARTY#` rows; union party IDs, page those reads, and chunk batch reads of PartyDetails. No party-table or player-email scan; the public list remains intentionally unbounded. |
| `partyQuery`; party configuration | `PartyQuery` reads the newest party revision. | `GetItem(PARTY#id, META)`. |
| `playerPageQuery`, `retiredPlayersPageQuery`; player pages | `PlayersQuery` and `RetiredPlayersQuery` load and reduce party-wide player revision history. | Query `PARTY#id` with `begins_with(SK, 'PLAYER#')` or `RETIRED_PLAYER#`; no deleted-row filter. |
| `pinPageQuery`, `pinListPageQuery`; pin pages | `PinsQuery` loads party pin history. | Query `PARTY#id`, `begins_with(SK, 'PIN#')`. |
| `prepareSpinPageQuery`, `newPairsPageQuery`, `currentPairsPageQuery`; spin and pair pages | Player/pin reads plus `CurrentPairAssignmentDocumentQuery`; current pairing currently searches full pairing history. | Batch get party meta/current-pair pointer, bounded player/pin query, then direct get pairing timeline row; party boost is direct `GetItem`. |
| `StatisticsPageQuery`; statistics | `PairListQuery`, pairing history, and statistics functions read players, retired players, contributions, and pairing history. | Core player queries plus Pair Aggregate and Party-Year additive items for counters/recent values. Exact medians and distinct player/pin values read the requested party/year's bounded month buckets; no all-party scan is used. |
| `partySecretsPageQuery`; secrets | `SecretListQuery` loads party secret history; boost is a separate lookup. | Query `SECRET#` Core metadata items; direct party boost item. Read secret material only in the authorized integration path. |
| `userQuery`, `userPageQuery`, `prereleaseUserPageQuery`; user pages | `UserQuery` returns current user; party list repeats the scan path. | `GetItem(USER#id, PROFILE)`, validated-email and owned-party queries for the party list, and direct user boost item. |
| `contributionVisualizationPageQuery`; visualization shell | Party detail and `spinsUntilFullRotation`, which derives from player data. | Get party meta and bounded active-player query; optionally persist a current rotation estimate if profiling shows repeated calculation matters. |
| `contributionVisualizationDataQuery`; visualization | Pair list plus a contribution query/filter per pair and window. | Query requested monthly contribution buckets once, then form pair views from a server-side bounded report. Add a pair-window aggregate only when measured traffic makes per-pair detail too expensive. |
| `contributionListPageQuery`, `contributionOverviewPageQuery`; contribution pages | Player lists plus `PartyContributionQuery`, whose window is a filter over party contribution records. | Read the active contribution generation, then use internal cursors to traverse its bounded month buckets and assemble the existing list response. Use additive monthly summary rows when only counts are requested, and source buckets for exact medians/contributor-derived values. |
| `integrationPageQuery`; integrations | Party detail plus `PartyIntegrationQuery`. | Batch get `META` and `INTEGRATION#SLACK`; configuration remains a direct configuration-provider read. |
| `historyPageQuery`; pairing history | `PairAssignmentDocumentListQuery` returns the entire party pairing history. | Traverse pairing month buckets with internal cursors and assemble the existing full list response. The response remains intentionally unbounded; a future additive connection may expose the cursors. |
| `globalStats`; administration/client test operation | `GlobalStatsQuery` scans all parties and loads every party's pairing history for the selected year. | Read and sum 16 `GLOBAL#STATS#year#SHARD#n / SUMMARY` items, then query and merge shard-local `PARTY#` projections in party-ID order. Each requested exact non-additive party field reads that party/year's source buckets. This eliminates scans and the hot single summary key, but the unchanged full-list API remains linear in its returned parties. |

The remaining GraphQL query fields in the schema (`Party.integration`,
`Party.boost`, `Party.currentPairingSet`, `Party.secretList`, `Party.pinList`,
`Party.playerList`, `Party.retiredPlayers`, `Party.pairList`,
`Party.contributionReport`, and `User.players`) are covered by the relevant
rows above. They must share request-scoped loaders so a single GraphQL request
does not repeat the same direct read for multiple selected fields.

### Mutations and write paths

| Mutation family | Target write set |
| --- | --- |
| `saveParty`, `deleteParty`, `saveSlackIntegration` | Core current item(s), current owner-party relationship changes where applicable, and one History revision per changed entity. |
| `savePlayer`, `deletePlayer` | Active or retired player item, normalized primary and additional-player-email lookup changes, and History revision. Player-email verification is not part of the lookup rule. |
| `savePin`, `deletePin`, `createSecret`, `deleteSecret` | Core current item or removal plus History; secret tokens/ciphertext use the restricted secret boundary. |
| `spin`, `savePairAssignments`, `deletePairAssignments` | Activity pairing rows, current pointer, pair and yearly additive summaries, the party's deterministic global-year shard aggregate, and History event. Use a transaction or an idempotent projection worker when fan-out exceeds its limit. |
| `saveContribution` (one or bulk) | After party authorization, save each contribution under the snapshotted active generation using an item-derived idempotency key, active-generation condition, locator/timeline rows, defined additive summaries, and History revision. Bulk chunks are bounded and retryable, not all-or-nothing; do not scan to find existing contribution IDs. |
| `clearContributions` | Atomically advance the active contribution generation and write a History event. Clean up obsolete-generation locators, timelines, and summaries asynchronously and idempotently. |
| `connectUser`, `disconnectUser`, `saveBoost`, `deleteBoost` | Core profile, validated-email and email-alias rows, affected email-to-player-party projections, boost views, and History revisions. |
| Slack/Discord grant mutations | Restricted integration/secret references, party integration metadata, and History revisions. |

## GraphQL compatibility and optional future evolution

This target data model requires no GraphQL schema change. Existing fields,
arguments, mutations, response shapes, and user-visible behavior remain
compatible; resolvers use the new keys, internal cursors, and exact source
windows behind the current contract. Mutation transport adds the required
`Idempotency-Key` HTTP header, which the client/SDK supplies without adding a
GraphQL input. In particular, `pairingSetList`,
`contributionReport.contributions`, nested `Pair.pairingSetList`, `pairList`,
and `globalStats(year)` do not need renamed fields or new required inputs.

The storage design does not make existing unbounded responses bounded. If
product later needs predictable response size for unusually long histories, it
may add—not replace—cursor-based connections for those lists, an aggregate-only
contribution report, or a paginated global party-stat connection. Such fields
must be additive and optional; existing list fields are neither deprecated nor
removed by this proposal. `globalStats` remains broadly available and
aggregate-only, regardless of whether a future connection is added.

## Correctness rules

1. A current row has an integer `version`; an update requires
   `version = :expectedVersion` and writes `version + 1`.
2. Every accepted domain mutation writes a unique History revision in the same
   transaction as its source-of-truth current row whenever possible.
3. Authorization preserves the current owner-or-authenticated-user-email
   policy. A validated user identity may match any primary or additional email
   on an active player; player-email verification is not required. The
   canonical User and Player rows are authoritative; owned-party and
   email-to-player-party rows are rebuildable lookup projections, not an
   independent permission model.
4. Current reads do not interpret tombstones. Deleted state lives in History;
   current rows are removed or explicitly moved to the retired collection.
5. Every mutation carries an `Idempotency-Key` HTTP header and request
   fingerprint. Its durable receipt expires after 24 hours; within that
   interval a duplicate request returns the original outcome rather than adding
   revisions or aggregates twice, while mismatched reuse is rejected.
6. Timeline cursors include bucket and exclusive-start key. They must not
   expose DynamoDB key format as a public contract; encode/sign them at the
   GraphQL boundary.
7. Materialized aggregate fields are additive or have a stated exact update
   rule. Exact medians, distinct values, and contributor-derived values are
   calculated from explicit bounded source windows, never inferred from an
   unspecified scalar summary.

## Runtime authorization integration

The DynamoDB keys optimize Coupling's existing access calculation; they do not
replace runtime authorization. After authentication resolves the current user
and connected identities, the server derives that user's validated-email set
and permits a party only when the user owns it or an active current player's
primary or additional email matches one of those identities. The Player email
does not need independent verification.

Create an `AuthorizedPartyContext` for each `(request, partyId)` after this
check succeeds. Cache contexts in a map keyed by `partyId`, never in one
request-wide slot. Every party child resolver, party-scoped mutation, and
WebSocket connection uses the context whose party ID exactly matches the
requested resource. This prevents authorization established for party A from
being reused to load or mutate party B in a multi-party GraphQL request.

The GraphQL `party` root remains the normal read barrier. Child resolvers may
avoid repeating the lookup only because they receive an already authorized
context, not a raw party ID plus the unrestricted command dispatcher. Each
party-scoped mutation performs the same check both at its GraphQL boundary and
in its command path, protecting future non-GraphQL callers. Global statistics
is intentionally excluded from this party gate and returns only its approved
aggregate projection.

## Capacity and growth behavior

At the expected party size, Core party partitions remain small: active players,
pins, secrets, and current configuration are bounded by application use rather
than retained history. Activity partition size is bounded by month, so an old
party with many years of history has no slower current-player or current-pair
read than a new party.

Do **not** add write sharding now. Reconsider bucket granularity or sharding
only after measurement shows one of the following for a party/month key:

- sustained traffic approaching a material portion of a single-partition
  ceiling (AWS documents 3,000 read units/s and 1,000 write units/s per
  partition for items within the relevant size limits);
- repeated `KeyRangeThroughputExceeded`/hot-key throttles after normal retry
  handling;
- a month bucket approaching an operational size chosen well below 10 GB; or
- a realistic party produces enough same-month timeline items that pagination
  or query latency violates its service objective.

At that point first shorten the bucket (month to week) for new data. Add a
deterministic shard suffix only if write rate, rather than retained size,
remains the constraint; queries then fan out across a documented fixed shard
count. This follows AWS's high-cardinality-key guidance and avoids making
ordinary reads more complex before the domain needs it. Global statistics are
the deliberate exception: they begin with 16 deterministic party-ID-hash
shards because every pairing write would otherwise contend on one yearly
summary key.

`partyList` remains intentionally unbounded at the GraphQL boundary. Its
resolver must page relationship queries and chunk batch reads to respect Dynamo
request limits, but this proposal does not introduce a party-list connection or
a maximum accessible-party count. That is an accepted product/API boundary, not
a claim that the response will remain small for every user.

Item payloads must remain below the 400 KB DynamoDB item maximum. Large audit
snapshots, exported reports, or attachments belong in S3 with immutable object
keys and a small DynamoDB reference. Keep attribute names compact enough to
avoid crossing 1 KB write and 4 KB read billing boundaries unintentionally.

## Operational safeguards

- Use on-demand capacity initially; revisit provisioned capacity only after
  workload telemetry is stable enough to forecast. Turn on PITR for all four
  tables and test restore/export procedures independently of application
  deploys.
- Scope IAM by table and operation. The GraphQL role receives only the Core and
  Activity actions it needs; live-connection handlers receive only the live
  table; secret decrypt/read authority is isolated. Replace the current broad
  `AmazonDynamoDBFullAccess` deployment policy.
- Enable DynamoDB Streams on Core and Activity only if derived aggregates are
  made asynchronous. Consumers must be idempotent because transaction records
  can arrive interleaved and GSI/stream propagation is not a read-your-write
  guarantee.
- Alarm on consumed capacity, throttled requests by operation, successful
  request latency, system errors, GSI write throttles, iterator age for any
  stream projection, and TTL deletion backlog. Enable Contributor Insights to
  identify hot partition keys before customer-visible throttling.
- Review GSI projection size before adding an index. `KEYS_ONLY` is the default
  for a lookup index; project only fields that remove a frequent base-table
  read. [AWS index guidance](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-indexes-general.html)
  explains the storage, throughput, and fetch trade-off.
- Use exponential-backoff retries for transient throttling, bounded by a
  request deadline. Retries must use the stable mutation receipt key and
  fingerprint; DynamoDB's `ClientRequestToken` alone is insufficient outside
  its service retry window.
- Alarm on per-shard global-summary capacity and throttles in addition to the
  existing hot-key signals. A global-stat query also records merge latency and
  shard-page skew so one shard cannot silently dominate its response time.
- Periodically reconcile rebuildable Core projections. Recompute
  `PLAYER_PARTY#` and party-list lookup rows from the current canonical User
  and Player items, compare them to stored projections, alert on divergence,
  and repair only the projections. History remains append-only evidence: a
  remediation is a new event, never an edit to an old revision.

## Frequently asked questions

### Does this conflict with AWS single-table guidance?

No. AWS's core guidance is to begin with access patterns, keep the table count
low, and colocate heterogeneous items that are routinely read together. The
Core table is a single-table design for current party/user data. Activity,
History, and Live Connections are separate because their unbounded growth,
retention, stream, permission, and TTL behavior differ materially from current
configuration reads. This is a small number of workload boundaries, not a
relational-style table-per-entity model. See AWS's
[data-modeling foundations](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/data-modeling-foundations.html)
and [single-table versus multi-table discussion](https://aws.amazon.com/blogs/database/single-table-vs-multi-table-design-in-amazon-dynamodb/).

### Can the model later show change history for every entity?

Yes. Each mutable entity has a canonical immutable revision stream under
`ENTITY#<kind>#<scope>#<id>`, ordered by `REV#<revisionId>`. A GraphQL
connection such as `player.changeHistory(first, after)` queries one partition
and paginates. The compact party-month audit projection supports a party-wide
change feed without reading every entity's history. If product needs later add
user-wide or actor-wide audit feeds, add lightweight monthly projection rows;
do not duplicate the full revision payload.

All history reads use internal cursor pagination and may be date-bounded. The
model supports indefinite retention, but it cannot make an existing unbounded
GraphQL list safe or predictably fast without a future additive API choice.

### Does denormalization create multiple sources of truth?

It creates multiple copies, but not multiple authorities. Each domain fact has
one canonical current item: Core User/Party/Player/Pin rows for configuration,
Activity timeline rows for contributions and pairing sets, and Live Connection
base rows for sockets. Locators, list rows, summaries, global statistics, and
audit-feed rows are labeled projections. Immutable History records are the
authoritative audit evidence for an accepted mutation, not the current-state
read model.

Critical source and projection writes are transactional with version and
idempotency conditions. Non-critical aggregates may be eventually consistent
only when maintained by an idempotent stream consumer and declared as such to
the API. Reconciliation rebuilds projections from canonical current data;
it must never infer a new current state or permission from a stale projection.

### How do testing and repair protect against an unanticipated data bug?

The implementation should have historical tests that replay user, player,
party, contribution, and pairing mutations and assert the expected current
state and audit history after every step. Property-style tests should permute
retries and concurrent updates, proving that versions prevent lost updates and
idempotency prevents duplicate revisions or aggregate increments. Regression
fixtures preserve every previously discovered data-integrity failure.

The repair path distinguishes authority from derived data. It may automatically
rebuild relationship lookups and summaries from canonical Core/Activity rows,
then verify the result against immutable History. It must not automatically
restore a deleted entity, recreate an owner or active-player relationship, or
rewrite audit history from an ambiguous projection. Those cases are quarantined
for a reviewed repair that appends a remediation event and carries a
correlation ID.

### How is Coupling's existing owner-or-authenticated-user-email access rule kept fast and safe?

The proposal retains the rule exactly: an owner sees a party through their
current owned-party relationship; a team participant sees it because one of
their validated user identities matches a primary or additional email on an
active current player. It does not add roles or replace email-based team
access.

For the normal path, fetch the user's small validated-email set, query each
corresponding `EMAIL#... / PLAYER_PARTY#...` partition, union the resulting
party IDs with `OWNED_PARTY#` rows, and batch read party metadata. For a direct
party request, check the same bounded lookups. If a lookup projection is
missing or suspect, query that party's current active players and compare every
primary or additional player email to the user's validated-email set. At
Coupling's expected three-to-fourteen-person party size, this fallback is
bounded and keeps the authorization result faithful to the canonical User and
Player data.

Historical tests cover email linking/unlinking, player email edits,
retirement/deletion, and ownership changes. Reconciliation can safely rebuild
the email-to-player-party lookup; it cannot grant visibility solely because a
stale projection claims a relationship.

### Why does user authentication, rather than player-email verification, control team access?

The access decision answers whether the authenticated user controls an email
that a current active player record names. "Validated" therefore describes the
user's primary or connected identity. A Player's primary and additional emails
are team-managed membership data; an additional email may be displayed as
unvalidated without preventing the authenticated owner of that email from
seeing the party. The `PLAYER_PARTY` projection contains every normalized
primary and additional active-player email so its fast path preserves that
rule exactly.

### Why are exact medians and distinct values read from source windows?

Counts and other additive values have small, precise update rules. Exact
medians and distinct sets do not: a scalar cannot be safely merged after an
edit or delete without losing information. For those fields, the resolver reads
the requested party/year or contribution-window month buckets and calculates
the answer from the authoritative timeline events. This is exact and bounded by
the caller's explicit window; it avoids presenting an approximation as the
current product statistic.

### Why are global statistics split into 16 fixed shards?

Every pairing can affect global yearly totals. A single yearly summary would
make all those writes contend on one partition key. The specified
`SHA-256(UTF-8(partyId))[0] & 0x0f` function selects one of 16 shards,
distributing writes without a mutable routing table. Global totals are the sum
of the 16 shard summaries. Party projections stay in the same shard and are
merged by party ID for a stable, opaque-cursor page.

### Why is a durable mutation receipt needed when DynamoDB has `ClientRequestToken`?

The DynamoDB token protects a transaction retry only for its service-defined
window. The client/SDK sends one stable `Idempotency-Key` HTTP header per
intended mutation; bulk contribution entries derive stable child keys from it.
Coupling stores a receipt with the actor, key, request fingerprint, outcome,
and a 24-hour TTL in the same transaction as the domain write. Matching retries
replay the stored outcome; a reused key with different contents fails. This
makes the application's 24-hour retry contract explicit and prevents duplicate
revisions or aggregate changes.

### How can clearing contributions be atomic when there may be many rows?

`clearContributions` does not delete every row synchronously. It atomically
advances the party's active contribution generation and records the audit
event. All current reads select that new generation immediately, so users never
observe a partial clear. An idempotent worker later removes old-generation
timeline, locator, and summary rows; the immutable History event remains.

### How do single and bulk contribution saves coexist with normal coupling work?

Contribution ingestion is a separate Activity workload. A single contribution
does one conditional source-of-truth transaction: it reads the active
generation, moves or writes that contribution's locator/timeline row, updates
its defined summary state, writes History, and records its idempotency result.
It does not touch pairing locators, pair aggregates, the current-pair pointer,
or global statistics.

The existing `saveContribution` list input remains supported for integrations
that update in bulk. It is deliberately a sequence of bounded, independently
idempotent contribution chunks rather than one giant transaction. This matches
the current sequential-list behavior and avoids DynamoDB's transaction limits.
The trade-off is explicit: a completed chunk may be visible while a later chunk
is retried, but a retry never duplicates completed entries. Contention from a
bulk import is limited to contribution rows and any same-party/month summary;
it does not block normal spin, pairing, player, pin, or configuration work.

A concurrent clear is the meaningful boundary. The bulk operation conditions
each chunk on its snapshotted active generation. If clear advances it, remaining
chunks stop instead of mixing old and new data; retrying them writes only the
new generation, while partial old-generation results are already invisible.
If product later requires a bulk import to appear all at once, it needs a
separate generation-style publish operation; that is not a property of the
current list mutation and is intentionally out of scope here.

### How does the cost surface of this proposal compare to the current cost?

The proposal trades more predictable write and storage cost for current-state
reads that do not grow with unrelated revision history. The following is an
illustrative on-demand estimate, not a forecast. Assume `us-east-1`, Standard
table class, item/index writes no larger than 1 KB, eventually consistent reads
no larger than 4 KB, and current on-demand rates of $1.25 per million write
request units (WRUs), $0.25 per million read request units (RRUs), and $0.25
per GB-month of storage. A transactional 1-KB write consumes two WRUs. See
[AWS on-demand capacity units](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/read-write-operations.html)
and [AWS pricing](https://aws.amazon.com/dynamodb/pricing/on-demand/); another
Region, larger item, GSI, backup, stream, or transfer setting changes the math.

| Illustrative operation | Current request cost | Target request cost | Why it changes |
| --- | --- | --- | --- |
| Load current party after 500 one-KB revisions | 250 RRUs, about $0.0000625 | 1 strong `GetItem` RRU, about $0.00000025 | The target reads `META`, not 500 revisions. |
| Read 40 recent contributions from a party holding 1,000 records | 500 RRUs, about $0.000125, if the filter examines all records | 20 RRUs, about $0.000005 | The target reads one month bucket instead of filtering the party partition. |
| Save a player with a primary and one additional email | About 2 WRUs / $0.0000025 for a one-KB base write plus its current email GSI | 10 WRUs / $0.0000125 for five one-KB transactional writes: player, two lookup rows, History, receipt | The extra writes buy direct authorization and auditability. |
| Save a pairing affecting four pair aggregates | About 1 WRU / $0.00000125 for a one-KB append | About 22 WRUs / $0.0000275 for 11 one-KB transactional writes: timeline, locator, pointer, four aggregates, party-year, one global shard, History, receipt | The write precomputes current-pair and counter paths while preserving one audit decision. |

The target's stored bytes also increase: it retains current items and only the
projections needed by readers alongside immutable History, mutation receipts
for 24 hours, and additive summaries. It may therefore cost more per mutation
and per retained GB, especially with PITR and Streams enabled. The relevant
comparison is not a single append but the total cost of a user-visible operation
after years of history.

Exact non-additive statistics remain an explicit read-cost trade-off. A median,
distinct set, or contributor result reads its selected source window; a
count-only request uses small additive summaries. `globalStats` is the important
exception: its 16-shard summary is cheap, but the unchanged API asks for every
party. If it requests exact per-party fields for 500 parties with 100 one-KB
pairing records each, both designs still read roughly 50,000 source records
(about 25,000 eventually consistent RRUs, or $0.00625) to calculate them. The
target removes scans and the hot single summary key, but does not turn that
unbounded exact full-list response into a cheap operation without a future,
additive bounded API.

Before rollout, replace these assumptions with production measurements:
item-size percentiles, requests by matrix operation, returned and scanned
bytes, retained-history distribution, GSI/stream/PITR storage, transaction
conflicts, and p50/p95 latency. Compare total cost per successful user-visible
operation at the same retained-history size, not the cost of one isolated
write.

### Which actions are expected to be slower, and why is that acceptable?

The proposal deliberately moves work to the write boundary when it prevents
unbounded or ambiguous reads later. The following paths may have higher latency
or more DynamoDB request units than their current simple append-only writes:

- `saveParty`, `savePlayer`, `deletePlayer`, and identity-link changes update
  the canonical row, relationship projections, History, and mutation receipt.
  This makes party discovery and authorization direct lookups rather than
  scans or revision reduction.
- `spin`, `savePairAssignments`, and `deletePairAssignments` are the most
  write-amplified interactive mutations: they conditionally update the pairing
  source, current pointer, pair/year summaries, one global shard, audit, and
  receipt. The transaction and optimistic-version check are acceptable because
  they prevent lost updates and let subsequent current-pair/statistic reads
  avoid replaying all pairing history.
- A contribution edit that changes its effective time can cost more than a new
  contribution because it removes the old timeline location before writing the
  new one and updating its summaries. `clearContributions` returns after one
  generation-switch transaction; its old-row cleanup is slower background work
  and never delays the user-visible clear.
- Exact median, distinct, and contributor report fields—and `globalStats`—can
  be slower than a small-history query because they intentionally read their
  authoritative source window or fixed 16-shard fan-out. A selected party/date
  range has a known bound; the unchanged full-list `globalStats` remains linear
  in every party it returns. That cost buys exact results rather than a hidden
  scan of every retained party record.

Transactions can also surface an optimistic-concurrency conflict rather than
silently accepting overlapping edits. A brief retry is acceptable because the
durable mutation receipt makes it safe and because preserving one correct audit
history is more valuable than returning a fast but lost update. These costs
should be measured separately from ordinary current-state page reads; they are
not evidence that the read-model design has failed.

### Where should this design improve performance most, and how do we measure success?

The biggest wins should be felt by established Coupling parties, not brand-new
ones. Opening a party, editing its players or pins, viewing the current pairs,
and reconnecting a live session should stay quick even after years of spins,
edits, and contribution imports. Today, that older history can be reread just
to answer a question about the party as it is now.

Recent contribution views should also stay responsive as a party ages: a team
looking at this week, month, quarter, half-year, or year should pay for that
chosen period, not for every contribution it has ever recorded. A bulk
contribution import should not make someone else's spin, pairing, or party
setup wait.

Measure this with a small set of realistic party stories before and after the
change: open a new party and a long-lived party, start a spin and view its
current pairs, load a recent contribution report, run a bulk contribution
update while another person uses pairing, and reconnect a live client. Success
means the long-lived party feels much like the new one for ordinary work; the
bulk import does not disrupt pairing; and every story returns the same party,
pairing, and contribution results as before.

The exception is intentionally visible: asking for all history, a very large
exact report, or the current full global-stat list can still take longer because
the product asked for a large exact answer. Those are compatibility choices,
not the everyday Coupling flows this design is meant to protect. Internally,
the service should still watch latency, errors, and work per request so a
regression is caught before users feel it.

### Does this design require GraphQL API changes, or suggest future changes?

No GraphQL schema change is required to adopt this design. The current schema,
operations, inputs, response shapes, and user-visible behavior can remain as
they are while resolvers move to the new read models. Clients and raw
integrations must send an `Idempotency-Key` HTTP header for mutations; the
SDK/client creates it automatically, so it is request metadata rather than a
GraphQL input. Internal DynamoDB cursors, generation selection, aggregate
reads, and shard merges are implementation details, not GraphQL contract
changes.

The design does suggest optional, additive future connections or report fields
if product later wants bounded response sizes for long pairing or contribution
histories, or for global party statistics. They are not prerequisites, planned
deprecations, or a change to existing callers. Keeping current unbounded lists
is an explicit compatibility choice with the capacity trade-off described
above.

### How is runtime authorization kept aligned with these lookups?

The authorization result is computed once per requested party after user and
connected-identity resolution, then carried in an `AuthorizedPartyContext`.
Contexts are keyed by party ID for the lifetime of a request. A resolver for
party B cannot reuse a context that was authorized for party A, including when
one GraphQL operation uses aliases to request multiple parties. Party child
resolvers receive this context rather than an unrestricted dispatcher.

Every party-scoped mutation and WebSocket connection repeats the same policy
check before it reads or writes party data; command handlers repeat it as
defense in depth. The allowed policy remains unchanged: ownership or a match
between a resolved validated user email and any primary or additional email on
an active player. `globalStats` is an intentional exception because it is
broadly available, but its GraphQL and stored projections are restricted to
approved aggregate fields and cannot traverse into party content.

## Verification criteria for a later implementation

1. Every production client query in the matrix is implemented with `GetItem`,
   `Query`, `BatchGetItem`, a bounded month-bucket fan-out, or a materialized
   aggregate. No request-path `Scan` and no filter-based identifier lookup
   remains.
2. Integration tests cover internal-cursor continuity, reverse chronology,
   bucket boundaries, empty ranges, direct current-pair lookup, player retirement,
   email alias uniqueness, connection cleanup, contribution-generation clear
   visibility, single and bulk contribution retry/clear interleavings, and
   owner-or-authenticated-user-email access decisions for both primary and
   additional player emails.
3. Concurrent-update tests prove that a losing writer cannot overwrite the
   current version or create a duplicate audit revision.
4. Load tests model small parties with long-lived histories, then deliberately
   exercise the stated rebucketing/sharding thresholds and all 16 global-stat
   shards.
5. Restore tests prove that Core, Activity, History, and Live Connections have
   their intended retention and recovery behavior. Live connection state is
   explicitly treated as rebuildable.
6. Historical and property-style integrity tests replay authorization-relevant
   identity/player changes, concurrent writes, and retries. They prove
   matching `Idempotency-Key` retries replay one result, mismatched reuse fails,
   bulk child keys remain stable, and only derived rows are auto-repaired.
7. Authorization tests cover a multi-party GraphQL alias request, every
   party-scoped mutation, and WebSocket connection setup. They prove that a
   context authorized for one party cannot expose or mutate another party.
8. Contract tests assert that broadly available `globalStats` exposes only its
   approved aggregate fields, merges shard pages in stable party-ID order, and
   cannot gain a path to party content. Cross-runtime tests prove the specified
   SHA-256 shard function selects the same shard for every party ID.
9. Exact-stat tests cover edits and deletes, proving median, distinct, and
   contributor-derived values come from the selected bounded source window
   rather than an approximate scalar summary.

## Non-goals

- No migration strategy, dual-read/write period, backfill plan, or code change
  is included here.
- No claim is made about actual request frequency: client documents show
  supported demand shapes, not production telemetry.
- `partyList` remains an unbounded public response. The resolver respects
  DynamoDB request limits internally, but this proposal does not add a
  connection or impose a maximum accessible-party count.
- This proposal does not mandate DynamoDB single-table design. It selects a
  small number of tables by scaling boundary and access correlation, which is
  more appropriate than either one table per entity or one table for every
  workload.
