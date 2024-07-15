package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.stats.PairFrequencyControls
import com.zegreatrob.coupling.client.components.stats.Visualization
import com.zegreatrob.coupling.client.components.stats.create
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.Props
import react.router.dom.SetURLSearchParams
import react.router.dom.useSearchParams

external interface IncubatingPartyStatisticsLoadingFrameProps : Props {
    var commander: Commander
    var party: PartyDetails
}

@ReactFunc
val IncubatingPartyStatisticsLoadingFrame by nfc<IncubatingPartyStatisticsLoadingFrameProps> { props ->
    val (commander, party) = props
    val (searchParams, setSearchParams) = useSearchParams()
    val window: JsonContributionWindow = searchParams["window"]?.let { window ->
        JsonContributionWindow.entries.find { it.name == window }
    } ?: JsonContributionWindow.Quarter
    val setWindow = setWindowSearchParamHandler(setSearchParams)
    CouplingQuery(
        commander = commander,
        query = graphQuery {
            party(party.id) {
                pairs {
                    players()
                    contributions(window = window)
                }
            }
        },
        toNode = { reload, _, queryResult ->
            PairFrequencyControls.create(
                pairsContributions = queryResult.party?.pairs?.toPairContributions() ?: return@CouplingQuery null,
                view = { (visualization, data) ->
                    when (visualization) {
                        Visualization.LineOverTime -> PairFrequencyLineGraph.create(data, window)
                        Visualization.Heatmap -> PairFrequencyHeatMap.create(data, window)
                    }
                },
                window = window,
                setWindow = {
                    setWindow(it)
                    reload()
                },
            )
        },
    )
}

private fun setWindowSearchParamHandler(setSearchParams: SetURLSearchParams) =
    { updatedWindow: JsonContributionWindow? ->
        setSearchParams({ previous ->
            previous.also {
                if (updatedWindow != null) {
                    previous["window"] = updatedWindow.name
                } else {
                    previous.delete("window")
                }
            }
        }, jso { })
    }

private fun List<PlayerPair>.toPairContributions(): List<Pair<CouplingPair, List<Contribution>>> = mapNotNull {
    it.players?.elements?.toCouplingPair()
        ?.let { pair -> pair to (it.contributions?.elements ?: emptyList()) }
}
