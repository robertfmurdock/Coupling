package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.gql.StatisticsPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(StatisticsPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, _, queryResult ->
        PartyStatistics(
            party = queryResult.party?.details?.partyDetailsFragment?.toModel() ?: return@CouplingQuery,
            players = queryResult.party.playerList?.map { it.playerDetailsFragment.toModel() } ?: return@CouplingQuery,
            pairs = queryResult.party.pairs?.map { it.toModel() } ?: return@CouplingQuery,
            spinsUntilFullRotation = queryResult.party.spinsUntilFullRotation ?: return@CouplingQuery,
            medianSpinDuration = queryResult.party.medianSpinDuration,
            chartComponent = CouplingResponsiveLine,
        )
    }
}

private fun StatisticsPageQuery.Pair.toModel(): PlayerPair = PlayerPair(
    players = players?.map {
        partyRecord(
            partyId = it.partyPlayerDetailsFragment.partyId,
            modifyingUserEmail = it.partyPlayerDetailsFragment.modifyingUserEmail!!,
            isDeleted = it.partyPlayerDetailsFragment.isDeleted,
            timestamp = it.partyPlayerDetailsFragment.timestamp,
            data = it.partyPlayerDetailsFragment.playerDetailsFragment.toModel(),
        )
    },
    spinsSinceLastPaired = spinsSinceLastPaired,
    recentTimesPaired = recentTimesPaired,
    pairAssignmentHistory = pairAssignmentHistory?.map { it.toModel() },
)

private fun StatisticsPageQuery.PairAssignmentHistory.toModel(): PairAssignment = PairAssignment(
    date = date,
    recentTimesPaired = recentTimesPaired,
)
