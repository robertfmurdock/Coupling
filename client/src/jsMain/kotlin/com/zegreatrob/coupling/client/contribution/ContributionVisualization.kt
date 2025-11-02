package com.zegreatrob.coupling.client.contribution

import com.apollographql.apollo.api.Optional
import com.zegreatrob.coupling.client.components.PairCycleTimeBarChart
import com.zegreatrob.coupling.client.components.contribution.AllContributionsLineGraph
import com.zegreatrob.coupling.client.components.contribution.AllEaseLineGraph
import com.zegreatrob.coupling.client.components.contribution.PairContributionsHeatMap
import com.zegreatrob.coupling.client.components.contribution.PairContributionsLineGraph
import com.zegreatrob.coupling.client.components.contribution.PairEaseHeatMap
import com.zegreatrob.coupling.client.components.contribution.PairEaseLineGraph
import com.zegreatrob.coupling.client.components.contribution.StoryContributionGraph
import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
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
import com.zegreatrob.coupling.client.gql.ContributionVisualizationDataQuery
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.type.PairsInput
import com.zegreatrob.coupling.sdk.toModel
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy
import react.Props

external interface ContributionVisualizationProps : Props {
    var commander: Commander
    var party: PartyDetails
    var spinsUntilFullRotation: Int
}

@ReactFunc
@Lazy
val ContributionVisualization by nfc<ContributionVisualizationProps> { props ->
    val (commander, party, spinsUntilFullRotation) = props
    val (window, setWindow) = useWindow(ContributionWindow.Quarter)
    CouplingQuery(
        commander = commander,
        query = GqlQuery(ContributionVisualizationDataQuery(party.id, window.toGql(), PairsInput(Optional.Present(true)))),
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

private fun List<ContributionVisualizationDataQuery.Pair>.toPairContributions(): List<Pair<CouplingPair, ContributionReport>> = mapNotNull {
    val contributionReport = it.contributionReport?.toModel() ?: return@mapNotNull null
    val players = it.players?.map { player -> player.playerDetails.toModel() }
    players?.toCouplingPair()?.let { pair -> pair to contributionReport }
}

fun ContributionVisualizationDataQuery.ContributionReport.toModel() = ContributionReport(
    contributions = contributions?.map { contribution ->
        contribution.partyContributionFragment.toModel()
    } ?: emptyList(),
    medianCycleTime = medianCycleTime,
    withCycleTimeCount = withCycleTimeCount,
    partyId = partyId,
    count = count,
)
