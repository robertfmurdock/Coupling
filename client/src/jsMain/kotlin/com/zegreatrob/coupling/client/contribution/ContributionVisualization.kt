package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.stats.PairFrequencyControls
import com.zegreatrob.coupling.client.components.stats.Visualization
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
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

external interface ContributionVisualizationProps : Props {
    var commander: Commander
    var party: PartyDetails
    var spinsUntilFullRotation: Int
}

@ReactFunc
val ContributionVisualization by nfc<ContributionVisualizationProps> { props ->
    val (commander, party, spinsUntilFullRotation) = props
    val (window, setWindow) = useWindow(GqlContributionWindow.Quarter)
    CouplingQuery(
        commander = commander,
        query = graphQuery {
            party(party.id) {
                pairs {
                    players()
                    contributionReport(window = window) {
                        contributions()
                        medianCycleTime()
                        withCycleTimeCount()
                    }
                }
            }
        },
    ) { reload, _, queryResult ->
        PairFrequencyControls(
            pairsContributions = queryResult.party?.pairs?.toPairContributions() ?: return@CouplingQuery,
            window = window,
            setWindow = {
                setWindow(it)
                reload()
            },
        ) { (visualization, data) ->
            when (visualization) {
                Visualization.LineOverTime -> PairFrequencyLineGraph(data, window)
                Visualization.Heatmap -> PairFrequencyHeatMap(data, window, spinsUntilFullRotation)
                Visualization.MedianCycleTimeBarChart -> PairCycleTimeBarChart(data, window)
                Visualization.CycleTimeBoxPlot -> PairCycleTimeBoxPlot(data, window)
            }
        }
    }
}

fun setWindowSearchParamHandler(setSearchParams: SetURLSearchParams) = { updatedWindow: GqlContributionWindow? ->
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

private fun List<PlayerPair>.toPairContributions(): List<Pair<CouplingPair, ContributionReport>> = mapNotNull {
    val contributionReport = it.contributionReport ?: return@mapNotNull null
    it.players?.elements?.toCouplingPair()
        ?.let { pair -> pair to contributionReport }
}
