# Few-Shot Code Style Examples

This file provides concrete examples from the codebase to illustrate preferred code style patterns for refactoring work.

## ✅ Excellent Patterns (Love It)

### Simple Fun Interfaces

**Example: SecretValidator.kt**
```kotlin
fun interface SecretValidator {
    suspend fun validateSubject(secret: String): Pair<SecretId, String>?
}
```

**Why this works:**
- Simple functional interface
- Single responsibility
- Composable
- Easy to test and mock

### Minimal Query with Delegation

**Example: PinsQuery.kt**
```kotlin
@ActionMint
data class PinsQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdPinRecordsSyntax {
        suspend fun perform(query: PinsQuery) = query.partyId.loadPins()
    }
}
```

**Why this works:**
- Minimal, focused query
- Delegates to syntax trait
- No unnecessary abstraction
- Clear intent

### Fluent Extension Functions for Command Flow

**Example: ServerCreateSecretCommandDispatcher.kt**
```kotlin
interface ServerCreateSecretCommandDispatcher : CreateSecretCommand.Dispatcher {
    val secretRepository: SecretSave
    val secretGenerator: PartySecretGenerator

    override suspend fun perform(command: CreateSecretCommand): Pair<Secret, String> = command.partySecret()
        .save()
        .oneTimeSecretValueGeneration()

    private suspend fun PartyElement<Secret>.oneTimeSecretValueGeneration() = Pair(
        first = element,
        second = secretGenerator.createSecret(this),
    )

    private suspend fun PartyElement<Secret>.save() = apply { secretRepository.save(this) }

    private fun CreateSecretCommand.partySecret(): PartyElement<Secret> = partyId.with(
        Secret(
            id = SecretId.new(),
            description = description,
            createdTimestamp = Clock.System.now(),
            lastUsedTimestamp = null,
        ),
    )
}
```

**Why this works:**
- Fluent, readable flow in `perform()`
- Extension functions make the pipeline clear
- Each function has single responsibility
- Easy to follow the transformation chain

### Decorator Pattern with Caching

**Example: CachedPartyRepository.kt**
```kotlin
class CachedPartyRepository(private val repository: PartyRepository) : PartyRepository by repository {

    private val mutex = Mutex()
    private val cache = mutableMapOf<PartyId, Record<PartyDetails>?>()

    override suspend fun loadParties(partyIds: Set<PartyId>): List<Record<PartyDetails>> = mutex.withLock {
        repository.loadParties(partyIds).also { cache.putAll(it.associateBy { record -> record.data.id }) }
    }

    override suspend fun getDetails(partyId: PartyId): Record<PartyDetails>? = mutex.withLock {
        cache.getOrPut(partyId) { repository.getDetails(partyId) }
    }
}
```

**Why this works:**
- One of very few acceptable uses of mutable data structures
- Clear decorator pattern with delegation
- Concurrency safety with mutex
- Single concern: caching

### Well-Structured Query with Extension Functions

**Example: GlobalStatsQuery.kt (partial)**
```kotlin
@ActionMint
data class GlobalStatsQuery(val year: Int) {

    interface Dispatcher {

        val partyRepository: PartyRepository
        val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

        suspend fun perform(query: GlobalStatsQuery): GlobalStats = partyRepository.loadParties(emptySet())
            .toStats(yearMatcher(query.year))
            .filter(::excludePartiesSpinningUnnaturallyFast)
            .toGlobalStats()

        private suspend fun List<Record<PartyDetails>>.toStats(
            matchesYear: (PartyRecord<PairingSet>) -> Boolean,
        ): List<PartyStats> = asFlow()
            .map { it to pairAssignmentDocumentRepository.loadPairAssignments(it.data.id) }
            .filter { (_, docs) -> docs.any(matchesYear) }
            .map { (party, docs) -> partyStats(party, docs, matchesYear) }
            .toList()

        private fun excludePartiesSpinningUnnaturallyFast(stats: PartyStats): Boolean {
            val medianSpinDuration = stats.medianSpinDuration
            return medianSpinDuration != null && medianSpinDuration > 1.minutes
        }

        private fun List<PartyStats>.toGlobalStats() = GlobalStats(
            parties = this,
            totalParties = size,
            totalSpins = sumOf(PartyStats::spins),
            totalPlayers = sumOf(PartyStats::playerCount),
            totalAppliedPins = sumOf(PartyStats::appliedPinCount),
            totalUniquePins = sumOf(PartyStats::uniquePinCount),
        )
    }
}
```

**Why this works:**
- Clear, fluent pipeline in `perform()`
- Well-named extension functions for transformation steps
- Each function has single, clear purpose
- Good use of method references (`::excludePartiesSpinningUnnaturallyFast`)
- Flow-based processing for async operations

### Simple, Direct Resolvers

**Example: AddToSlackUrlResolve.kt**
```kotlin
val addToSlackUrlResolve: (Json, Json, Request, Json) -> Promise<String> = { _, _, _, _ ->
    slackInstallProvider.generateInstallUrl(
        InstallUrlOptions(
            scopes = arrayOf(
                "chat:write",
                "chat:write.customize",
                "channels:history",
                "groups:history",
                "commands",
            ),
            redirectUri = slackRedirectUri(),
        ),
    )
}
```

**Why this works:**
- Direct, no unnecessary ceremony
- Clear what it does
- Appropriate for JS interop context (a bit "JS-y" but correct for the use case)

## ✓ Good Patterns (Like It)

### Simple Command Dispatcher

**Example: ServerSaveSlackIntegrationCommandDispatcher.kt**
```kotlin
interface ServerSaveSlackIntegrationCommandDispatcher : SaveSlackIntegrationCommand.Dispatcher {

    val partyRepository: PartyIntegrationSave

    override suspend fun perform(command: SaveSlackIntegrationCommand) = with(command) {
        partyRepository.save(partyId.with(PartyIntegration(team, channel)))
        VoidResult.Accepted
    }
}
```

**Why this works:**
- Simple, straightforward command
- Could be more fluent (see excellent examples above)

### Interface Composition with Sealed Results

**Example: DiscordRepository.kt**
```kotlin
interface DiscordRepository :
    DiscordSendSpin,
    DiscordDeleteSpin {
    suspend fun exchangeForWebhook(code: String): ExchangeResult

    sealed interface ExchangeResult {
        data class Success(val discordTeamAccess: DiscordTeamAccess) : ExchangeResult
        data class Error(val error: String, val description: String?) : ExchangeResult
    }
}

fun interface DiscordSendSpin {
    suspend fun sendSpinMessage(webhook: DiscordWebhook, newPairs: PairingSet): String?
}

fun interface DiscordDeleteSpin {
    suspend fun deleteMessage(webhook: DiscordWebhook, deadPairs: PairingSet)
}
```

**Why this works:**
- Good interface composition
- Sealed interface for type-safe results
- **Improvement:** Each interface should be in its own file

### Structured Query with Parallel Loading

**Example: PartyListQuery.kt**
```kotlin
@ActionMint
object PartyListQuery {

    interface Dispatcher :
        CurrentConnectedUsersProvider,
        UserPlayersSyntax,
        CurrentUserProvider,
        PartyRecordSyntax {

        suspend fun perform(query: PartyListQuery): PartyListResult = fetchAuthorizedPartyIds()
            .loadParties()

        private suspend fun fetchAuthorizedPartyIds() = coroutineScope {
            Pair(async { userAuthorizedPartyIds() }, async { playerAuthorizedPartyIds() })
        }.let { Pair(it.first.await(), it.second.await()) }

        private suspend fun Pair<Set<PartyId>, Set<PartyId>>.loadParties() = coroutineScope {
            val parties = getPartyRecords(first + second)
            PartyListResult(
                ownedParties = parties.filter { it.data.id in first },
                playerParties = parties.filter { it.data.id in second && it.data.id !in first },
            )
        }

        private suspend fun playerAuthorizedPartyIds(): Set<PartyId> = currentUser.loadPlayers()
            .map { it.data.partyId }
            .toSet()

        private suspend fun userAuthorizedPartyIds(): Set<PartyId> = loadCurrentConnectedUsers()
            .flatMap { it.authorizedPartyIds }
            .toSet()
    }
}

data class PartyListResult(val ownedParties: List<Record<PartyDetails>>, val playerParties: List<Record<PartyDetails>>)
```

**Why this works:**
- Excellent structure with clear separation
- Good use of parallel loading
- Extension functions for transformation
- **Could improve:** Some clarity in naming and flow

## ❌ Anti-Patterns (Dislike It)

### Large Methods That Are Hard to Understand

**Example: PairListQuery.kt - perform() method**
```kotlin
suspend fun perform(query: PairListQuery): List<PartyElement<PlayerPair>> {
    val (contributions, playerListData, retiredPlayerListData) = query.loadData()

    val allPlayerData = playerListData + retiredPlayerListData
    val naturalPairCombinations = allPlayerData
        .pairCombinations()

    val allContributionPairs: Set<Set<Record<PartyElement<Player>>>> =
        contributions.mapNotNull { contribution ->
            contribution.participantEmails
                .map { email ->
                    allPlayerData.find { it.data.player.matches(email) }
                        ?: placeholderPlayer(query, email)
                }
                .toSet().ifEmpty { null }
        }.toSet()

    val naturalPlayerSets: Set<Set<Record<PartyElement<Player>>>> =
        naturalPairCombinations.map { it.players.toSet() }.toSet()
    val extraPairs = allContributionPairs - naturalPlayerSets

    val filter: (PlayerPair) -> Boolean = if (query.includeRetired == true) {
        ({ true })
    } else {
        ({ pair -> !pair.anyPlayersAreRetired(retiredPlayerListData) })
    }
    return query.partyId.with(
        (
            naturalPairCombinations + extraPairs.map {
                PlayerPair(players = it.toList(), pairAssignmentHistory = emptyList())
            }
            ).filter(filter),
    )
}
```

**Why this doesn't work:**
- Big method that's hard to understand quickly
- Multiple concerns mixed together
- Should be broken into smaller, well-named extension functions
- See excellent examples above for how to structure this better

### Overly Complex Command Dispatchers

**Example: ServerSavePairAssignmentDocumentCommandDispatcher.kt**

The `perform()` method chains multiple side effects:
```kotlin
override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
    partyId.with(pairAssignments)
        .apply { save() }
        .apply { cannon.fire(broadcastAction()) }
        .let { VoidResult.Accepted }
        .also { partyId.loadIntegration()?.updateMessage(pairAssignments, partyId) }
}
```

**Why this doesn't work:**
- Multiple side effects chained together
- Mixing different concerns (save, broadcast, integration update)
- Hard to reason about the flow
- Better to break into explicit steps with clear names

### Methods That Mix Concerns and Are Hard to Parse

**Example: ServerSavePairAssignmentDocumentCommandDispatcher.kt - updateMessage() method**
```kotlin
private suspend fun PartyIntegration.updateMessage(pairs: PairingSet, partyId: PartyId) {
    val team = slackTeam ?: return
    val channel = slackChannel ?: return
    val accessRecord = slackAccessRepository.get(team) ?: return
    val token = accessRecord.data.accessToken

    coroutineScope {
        launch {
            if (pairs.slackMessageId != null) {
                slackRepository.sendSpinMessage(channel, token, pairs, partyId)
            }
        }

        launch {
            if (pairs.discordMessageId != null) {
                discordAccessRepository.get(partyId)?.data?.element?.let { discordTeamAccess ->
                    discordRepository.sendSpinMessage(discordTeamAccess.webhook, pairs)
                }
            }
        }
    }
}
```

**Why this is messy and hard to parse:**
- Multiple early returns for Slack validation at the top
- Then handles both Slack AND Discord in the same method (mixed concerns)
- Slack validation at the top doesn't relate to Discord handling below
- Nested nullable chaining in Discord part (`?.data?.element?.let`) is hard to follow
- Method name (`updateMessage`) doesn't convey it handles two separate integrations
- Better to split into `updateSlackMessage()` and `updateDiscordMessage()` with clear, separate flows

## Key Principles Extracted

1. **Prefer simple fun interfaces** for single-method contracts
2. **Use fluent extension functions** to create clear transformation pipelines
3. **Keep functions small** with single, clear responsibilities
4. **Extract well-named private functions** rather than inline complex logic
5. **Use delegation** (interface inheritance) to compose behavior
6. **Prefer immutability** except in rare, justified cases (like caching decorators)
7. **Use method references** (`::<function>`) when appropriate for readability
8. **Break large methods into smaller functions** with descriptive names
9. **Put one interface per file** for clarity
10. **Avoid mixing multiple concerns** in a single function
