package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.action.stats.StatisticsReport
import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.action.stats.heatmap.fire
import com.zegreatrob.coupling.client.ClientDispatcher
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc
import react.PropsWithValue
import react.create
import react.useEffectOnce
import react.useState

val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                pairAssignmentDocumentList()
                pairs {
                    players()
                    spinsSinceLastPaired()
                }
                medianSpinDuration()
                spinsUntilFullRotation()
            }
        },
        toNode = { _, dispatchFunc, queryResult ->
            val party = queryResult.party ?: return@CouplingQuery null
            val action = CalculateHeatMapAction(
                players = party.playerList?.elements ?: return@CouplingQuery null,
                history = party.pairAssignmentDocumentList?.elements ?: return@CouplingQuery null,
                rotationPeriod = party.spinsUntilFullRotation ?: return@CouplingQuery null,
            )
            CalculatingPartyStats.create { value = Triple(action, dispatchFunc, queryResult) }
        },
        key = partyId.value,
    )
}

val CalculatingPartyStats by
    nfc<PropsWithValue<Triple<CalculateHeatMapAction, DispatchFunc<ClientDispatcher>, CouplingQueryResult>>> { props ->
        var results by useState<List<List<Double?>>?>(null)
        val calculate = props.value.second { results = fire(props.value.first) }
        val queryResult = props.value.third
        useEffectOnce { calculate() }

        val party = queryResult.party ?: return@nfc
        val partyDetails = party.details?.data ?: return@nfc

        PartyStatistics(
            StatisticsQuery.Results(
                party = partyDetails,
                players = party.playerList?.elements ?: return@nfc,
                history = party.pairAssignmentDocumentList?.elements ?: return@nfc,
                pairs = party.pairs ?: return@nfc,
                report = StatisticsReport(
                    spinsUntilFullRotation = party.spinsUntilFullRotation ?: return@nfc,
                    medianSpinDuration = party.medianSpinDuration,
                ),
                heatmapData = results ?: return@nfc,
            ),
        )
    }
