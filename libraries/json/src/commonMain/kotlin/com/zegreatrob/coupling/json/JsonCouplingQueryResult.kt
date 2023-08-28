package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingConfig
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@Serializable
data class JsonCouplingQueryResult(
    val partyList: List<JsonParty>? = null,
    val user: JsonUser? = null,
    val party: JsonParty? = null,
    val globalStats: JsonGlobalStats? = null,
    val config: JsonConfig? = null,
    val pairs: List<JsonPair>? = null,
)

@Serializable
data class PartyInput(
    val partyId: String,
)

private fun JsonParty.toModel(): Party? {
    return Party(
        id = id?.let(::PartyId) ?: return null,
        details = details?.toModelRecord(),
        integration = integration?.toModelRecord(),
        pinList = pinList?.map(JsonPinRecord::toModel),
        playerList = playerList?.map(JsonPlayerRecord::toModel),
        retiredPlayers = retiredPlayers?.map(JsonPlayerRecord::toModel),
        secretList = secretList?.map(JsonSecretRecord::toModel),
        pairAssignmentDocumentList = pairAssignmentDocumentList?.map(JsonPairAssignmentDocumentRecord::toModel),
        currentPairAssignmentDocument = currentPairAssignmentDocument?.toModel(),
        boost = boost?.toModelRecord(),
        pairs = pairs?.map(JsonPair::toModel),
        medianSpinDuration = medianSpinDuration,
        spinsUntilFullRotation = spinsUntilFullRotation,
        contributions = contributions?.map(JsonContributionRecord::toModel),
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
    val details: JsonPartyDetailsRecord? = null,
    val integration: JsonIntegrationRecord? = null,
    val pinList: List<JsonPinRecord>? = null,
    val playerList: List<JsonPlayerRecord>? = null,
    val secretList: List<JsonSecretRecord>? = null,
    val retiredPlayers: List<JsonPlayerRecord>? = null,
    val pairAssignmentDocumentList: List<JsonPairAssignmentDocumentRecord>? = null,
    val currentPairAssignmentDocument: JsonPairAssignmentDocumentRecord? = null,
    val boost: JsonBoostRecord? = null,
    val pairs: List<JsonPair>? = null,
    val medianSpinDuration: Duration? = null,
    val spinsUntilFullRotation: Int? = null,
    val contributions: List<JsonContributionRecord>? = null,
)

@Serializable
data class JsonPair(
    val players: List<JsonPlayerRecord>? = null,
    val count: Int? = null,
    val partyId: String? = null,
    val spinsSinceLastPaired: Int? = null,
    val recentTimesPaired: Int? = null,
    val pairAssignmentHistory: List<JsonPairAssignment>? = null,
)

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
            allPairs = json.allPairs?.map(JsonPinnedCouplingPair::toModel),
            recentTimesPaired = json.recentTimesPaired,
        )
    },
)

fun PartyElement<PlayerPair>.toJson() = JsonPair(
    players = element.players?.map(PartyRecord<Player>::toSerializable),
    spinsSinceLastPaired = element.spinsSinceLastPaired,
    partyId = partyId.value,
)

@Serializable
data class JsonConfig(
    val discordClientId: String? = null,
    val addToSlackUrl: String? = null,
    val stripeAdminCode: String? = null,
    val stripePurchaseCode: String? = null,
)

private fun JsonConfig.toModel() = CouplingConfig(
    discordClientId = discordClientId,
    addToSlackUrl = addToSlackUrl,
    stripeAdminCode = stripeAdminCode,
    stripePurchaseCode = stripePurchaseCode,
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
data class JsonGlobalStatsInput(
    val year: Int,
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
