package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.PairCycleTimeBarChart
import com.zegreatrob.coupling.client.components.graphing.contribution.StoryEaseGraph
import com.zegreatrob.coupling.client.components.stats.PairFrequencyControls
import com.zegreatrob.coupling.client.components.stats.Visualization
import com.zegreatrob.coupling.client.components.stats.Visualization.AllContributionsLineOverTime
import com.zegreatrob.coupling.client.components.stats.Visualization.AllEaseLineOverTime
import com.zegreatrob.coupling.client.components.stats.Visualization.CycleTimeBoxPlot
import com.zegreatrob.coupling.client.components.stats.Visualization.MedianCycleTimeBarChart
import com.zegreatrob.coupling.client.components.stats.Visualization.PairContributionsLineOverTime
import com.zegreatrob.coupling.client.components.stats.Visualization.PairEaseHeatmap
import com.zegreatrob.coupling.client.components.stats.Visualization.PairEaseLineOverTime
import com.zegreatrob.coupling.client.components.stats.Visualization.PairFrequencyHeatmap
import com.zegreatrob.coupling.client.components.stats.Visualization.StoryContributionsOverTime
import com.zegreatrob.coupling.client.components.stats.Visualization.StoryContributionsPercentOverTime
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
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
                AllContributionsLineOverTime -> AllContributionsLineGraph(data.allContributions(), window)
                StoryContributionsOverTime -> StoryContributionGraph(data.allContributions(), window, false)
                StoryContributionsPercentOverTime -> StoryContributionGraph(data.allContributions(), window, true)
                Visualization.StoryEaseGraph -> StoryEaseGraph(data, window)
                PairContributionsLineOverTime -> PairContributionsLineGraph(data, window)
                PairFrequencyHeatmap -> PairContributionsHeatMap(data, window, spinsUntilFullRotation)
                PairEaseHeatmap -> PairEaseHeatMap(data, window, spinsUntilFullRotation)
                PairEaseLineOverTime -> PairEaseLineGraph(data, window)
                MedianCycleTimeBarChart -> PairCycleTimeBarChart(data, window)
                CycleTimeBoxPlot -> PairCycleTimeBoxPlot(data, window)
                AllEaseLineOverTime -> AllEaseLineGraph(data.allContributions(), window)
            }
        }
    }
}

private fun List<Pair<CouplingPair, ContributionReport>>.allContributions(): List<Contribution> = flatMap {
    it.second.contributions?.elements
        ?: emptyList()
}

fun setWindowSearchParamHandler(setSearchParams: SetURLSearchParams) = { updatedWindow: GqlContributionWindow? ->
    setSearchParams({ previous ->
        previous.also {
            if (updatedWindow != null) {
                previous.set("window", updatedWindow.name)
            } else {
                previous.delete("window")
            }
        }
    }, unsafeJso { })
}

private fun List<PlayerPair>.toPairContributions(): List<Pair<CouplingPair, ContributionReport>> = mapNotNull {
    val contributionReport = it.contributionReport ?: return@mapNotNull null
    it.players?.elements?.toCouplingPair()?.let { pair -> pair to contributionReport }
}
