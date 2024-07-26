package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.Party
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PartyStats
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@Serializable
data class JsonCouplingQueryResult(
    val partyList: List<JsonParty>? = null,
    val user: GqlUser? = null,
    val party: JsonParty? = null,
    val globalStats: JsonGlobalStats? = null,
    val config: GqlConfiguration? = null,
    val pairs: List<JsonPair>? = null,
)

private fun JsonParty.toModel(): Party? {
    return Party(
        id = id?.let(::PartyId) ?: return null,
        details = details?.toModelRecord(),
        integration = integration?.toModelRecord(),
        pinList = pinList?.map(GqlPinDetails::toModel),
        playerList = playerList?.map(JsonPlayerRecord::toModel),
        retiredPlayers = retiredPlayers?.map(JsonPlayerRecord::toModel),
        secretList = secretList?.map(JsonSecretRecord::toModel),
        pairAssignmentDocumentList = pairAssignmentDocumentList?.map(JsonPairAssignmentDocumentRecord::toModel),
        currentPairAssignmentDocument = currentPairAssignmentDocument?.toModel(),
        boost = boost?.toModelRecord(),
        pairs = pairs?.map(JsonPair::toModel),
        pair = pair?.let(JsonPair::toModel),
        contributionReport = contributionReport?.toModel(),
        medianSpinDuration = medianSpinDuration,
        spinsUntilFullRotation = spinsUntilFullRotation,
    )
}

fun JsonCouplingQueryResult.toDomain(raw: JsonElement) = CouplingQueryResult(
    raw = raw.toString(),
    partyList = partyList?.mapNotNull(JsonParty::toModel),
    user = user?.toModel(),
    party = party?.toModel(),
    globalStats = globalStats?.toModel(),
    config = config?.toModel(),
)

@Serializable
data class JsonParty(
    val id: String? = null,
    val details: GqlPartyDetails? = null,
    val integration: GqlPartyIntegration? = null,
    val pinList: List<GqlPinDetails>? = null,
    val playerList: List<JsonPlayerRecord>? = null,
    val secretList: List<JsonSecretRecord>? = null,
    val retiredPlayers: List<JsonPlayerRecord>? = null,
    val pairAssignmentDocumentList: List<JsonPairAssignmentDocumentRecord>? = null,
    val currentPairAssignmentDocument: JsonPairAssignmentDocumentRecord? = null,
    val boost: GqlBoostDetails? = null,
    val pairs: List<JsonPair>? = null,
    val pair: JsonPair? = null,
    val medianSpinDuration: Duration? = null,
    val spinsUntilFullRotation: Int? = null,
    val contributionReport: JsonContributionReport? = null,
)

@Serializable
data class JsonContributionReport(
    val partyId: String? = null,
    val contributions: List<GqlContribution>? = null,
    val count: Int? = null,
    val medianCycleTime: Duration? = null,
    val withCycleTimeCount: Int? = null,
    val contributors: List<JsonContributor>? = null,
)

fun JsonContributionReport.toModel() = ContributionReport(
    partyId = partyId?.let(::PartyId),
    contributions = contributions?.map(GqlContribution::toModel),
    contributors = contributors?.map(JsonContributor::toModel),
    count = count,
    medianCycleTime = medianCycleTime,
    withCycleTimeCount = withCycleTimeCount,
)

fun ContributionReport.toJson() = JsonContributionReport(
    partyId = partyId?.value,
    contributions = contributions?.map(PartyRecord<Contribution>::toJson),
    contributors = contributors?.map { it.toJson() },
    count = count,
    medianCycleTime = medianCycleTime,
    withCycleTimeCount = withCycleTimeCount,
)

@Serializable
data class JsonPair(
    val players: List<JsonPlayerRecord>? = null,
    val count: Int? = null,
    val partyId: String? = null,
    val spinsSinceLastPaired: Int? = null,
    val recentTimesPaired: Int? = null,
    val pairAssignmentHistory: List<JsonPairAssignment>? = null,
    val contributionReport: JsonContributionReport? = null,
)

@Serializable
data class JsonContributor(
    val email: String? = null,
    val playerId: String? = null,
    val details: JsonPlayerRecord? = null,
)

fun GqlContributionWindow.toModel() = when (this) {
    GqlContributionWindow.All -> null
    GqlContributionWindow.Year -> 365.days
    GqlContributionWindow.Quarter -> (365 / 4).days
    GqlContributionWindow.Month -> 30.days
    GqlContributionWindow.Week -> 7.days
}

fun JsonPair.toModel() = PlayerPair(
    players = players?.map(JsonPlayerRecord::toModel),
    count = count,
    spinsSinceLastPaired = spinsSinceLastPaired,
    recentTimesPaired = recentTimesPaired,
    pairAssignmentHistory = pairAssignmentHistory?.map { json ->
        PairAssignment(
            documentId = json.documentId?.let(::PairAssignmentDocumentId),
            details = json.details?.toModel(),
            date = json.date,
            allPairs = json.allPairs?.map(GqlPinnedPair::toModel),
            recentTimesPaired = json.recentTimesPaired,
        )
    },
    contributions = contributionReport?.toModel(),
)

fun PartyElement<PlayerPair>.toJson() = JsonPair(
    players = element.players?.map(PartyRecord<Player>::toSerializable),
    spinsSinceLastPaired = element.spinsSinceLastPaired,
    partyId = partyId.value,
)

@Serializable
data class JsonGlobalStats(
    val parties: List<JsonPartyStats>,
    val totalParties: Int,
    val totalSpins: Int,
    val totalPlayers: Int,
    val totalAppliedPins: Int,
    val totalUniquePins: Int,
)

fun JsonGlobalStats.toModel() = GlobalStats(
    parties = parties.map(JsonPartyStats::toModel),
    totalSpins = totalSpins,
    totalPlayers = totalPlayers,
    totalAppliedPins = totalAppliedPins,
    totalUniquePins = totalUniquePins,
    totalParties = totalParties,
)

fun GlobalStats.toJson() = JsonGlobalStats(
    parties = parties.map(PartyStats::toJson),
    totalSpins = totalSpins,
    totalPlayers = totalPlayers,
    totalAppliedPins = totalAppliedPins,
    totalUniquePins = totalUniquePins,
    totalParties = totalParties,
)

@Serializable
data class JsonPartyStats(
    val name: String,
    val id: String,
    val playerCount: Int,
    val spins: Int,
    val medianSpinDurationMillis: Double?,
    val medianSpinDuration: String? = null,
    val appliedPinCount: Int,
    val uniquePinCount: Int,
)

fun JsonPartyStats.toModel() = PartyStats(
    name = name,
    id = PartyId(id),
    playerCount = playerCount,
    spins = spins,
    medianSpinDuration = medianSpinDurationMillis?.milliseconds,
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
)

fun PartyStats.toJson() = JsonPartyStats(
    name = name,
    id = id.value,
    playerCount = playerCount,
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
    spins = spins,
    medianSpinDuration = medianSpinDuration?.toString(),
    medianSpinDurationMillis = medianSpinDuration?.toDouble(DurationUnit.MILLISECONDS),
)
