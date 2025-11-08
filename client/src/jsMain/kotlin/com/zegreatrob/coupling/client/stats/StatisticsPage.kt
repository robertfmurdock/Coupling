package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.gql.StatisticsPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy

@Lazy
val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(StatisticsPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, _, queryResult ->
        PartyStatistics(
            party = queryResult.party?.partyDetails?.toDomain() ?: return@CouplingQuery,
            players = queryResult.party.playerList.map { it.playerDetails.toDomain() },
            pairs = queryResult.party.pairList.map { it.toModel() },
            spinsUntilFullRotation = queryResult.party.spinsUntilFullRotation ?: return@CouplingQuery,
            medianSpinDuration = queryResult.party.medianSpinDuration,
            chartComponent = CouplingResponsiveLine,
        )
    }
}

private fun StatisticsPageQuery.PairList.toModel(): PlayerPair = PlayerPair(
    players = players.map {
        partyRecord(
            partyId = it.partyPlayerDetails.partyId,
            modifyingUserEmail = it.partyPlayerDetails.modifyingUserEmail!!,
            isDeleted = it.partyPlayerDetails.isDeleted,
            timestamp = it.partyPlayerDetails.timestamp,
            data = it.partyPlayerDetails.playerDetails.toDomain(),
        )
    },
    spinsSinceLastPaired = spinsSinceLastPaired,
    recentTimesPaired = recentTimesPaired,
    pairAssignmentHistory = pairingSetList.map { it.toModel() },
)

private fun StatisticsPageQuery.PairingSetList.toModel(): PairAssignment = PairAssignment(
    date = date,
    recentTimesPaired = recentTimesPaired,
    documentId = id,
    playerIds = emptyList(),
)
