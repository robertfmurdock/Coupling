package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.Party
import com.zegreatrob.coupling.model.PartyRecord
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

@Serializable
data class JsonCouplingQueryResult(
    val partyList: List<JsonParty>? = null,
    val user: GqlUser? = null,
    val party: JsonParty? = null,
    val globalStats: GqlGlobalStats? = null,
    val config: GqlConfiguration? = null,
    val pairs: List<JsonPair>? = null,
)

private fun JsonParty.toModel(): Party? {
    return Party(
        id = id?.let(::PartyId) ?: return null,
        details = details?.toModelRecord(),
        integration = integration?.toModelRecord(),
        pinList = pinList?.map(GqlPinDetails::toModel),
        playerList = playerList?.map(GqlPlayerDetails::toModel),
        retiredPlayers = retiredPlayers?.map(GqlPlayerDetails::toModel),
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
    val playerList: List<GqlPlayerDetails>? = null,
    val secretList: List<JsonSecretRecord>? = null,
    val retiredPlayers: List<GqlPlayerDetails>? = null,
    val pairAssignmentDocumentList: List<JsonPairAssignmentDocumentRecord>? = null,
    val currentPairAssignmentDocument: JsonPairAssignmentDocumentRecord? = null,
    val boost: GqlBoostDetails? = null,
    val pairs: List<JsonPair>? = null,
    val pair: JsonPair? = null,
    val medianSpinDuration: Duration? = null,
    val spinsUntilFullRotation: Int? = null,
    val contributionReport: JsonContributionReport? = null,
)

typealias JsonContributionReport = GqlContributionReport

fun JsonContributionReport.toModel() = ContributionReport(
    contributions = contributions?.map(GqlContribution::toModel),
    count = count,
    medianCycleTime = medianCycleTime,
    withCycleTimeCount = withCycleTimeCount,
    contributors = contributors?.map(GqlContributor::toModel),
    partyId = partyId?.let(::PartyId),
)

fun ContributionReport.toJson() = JsonContributionReport(
    contributions = contributions?.map(PartyRecord<Contribution>::toJson),
    contributors = contributors?.map { it.toJson() } ?: emptyList(),
    count = count,
    medianCycleTime = medianCycleTime,
    withCycleTimeCount = withCycleTimeCount,
    partyId = partyId?.value,
)

@Serializable
data class JsonPair(
    val players: List<GqlPlayerDetails>? = null,
    val count: Int? = null,
    val partyId: String? = null,
    val spinsSinceLastPaired: Int? = null,
    val recentTimesPaired: Int? = null,
    val pairAssignmentHistory: List<JsonPairAssignment>? = null,
    val contributionReport: JsonContributionReport? = null,
)

fun JsonPair.toModel() = PlayerPair(
    players = players?.map(GqlPlayerDetails::toModel),
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
