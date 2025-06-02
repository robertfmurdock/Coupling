package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.external.recharts.LinePoint
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ResponsiveContainer
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Scatter
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ScatterChart
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Tooltip
import com.zegreatrob.coupling.client.components.graphing.external.recharts.XAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.YAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ZAxis
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props

external interface StoryEaseGraphProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val StoryEaseGraph by nfc<StoryEaseGraphProps> { props ->
    val (pairsToReports, window) = props
    val allContributions = pairsToReports
        .flatMap { it.second.contributions?.elements ?: emptyList() }
        .distinctBy { it.id }
    val rawStoryContributions = allContributions.groupBy { it.story }
    val storyContributions = rawStoryContributions.filter { it.key?.contains(",") == false }
    val splitContributions = rawStoryContributions.filter { it.key?.contains(",") == true }

    if (pairsToReports.flatMap { it.second.contributions?.elements ?: emptyList() }.isEmpty()) {
        return@nfc
    }
    ResponsiveContainer {
        width = "100%"
        height = "100%"
        ScatterChart {
            margin = unsafeJso {
                bottom = 60
                left = 40
                right = 80
                top = 20
            }
            XAxis {
                type = "category"
                dataKey = "x"
                name = "story"
                interval = 0
            }
            YAxis {
                type = "number"
                dataKey = "y"
                name = "ease"
                domain = arrayOf(0, "dataMax")
            }
            Tooltip {
            }
            ZAxis {
                type = "number"
                dataKey = "z"
                name = "count"
                range = arrayOf(20, 1000)
                domain = arrayOf(0, 50)
            }
            Scatter {
                data = storyContributions.mapNotNull { (story, contributions) ->
                    unsafeJso<LinePoint> {
                        x = story ?: return@mapNotNull null
                        y = contributions.mapNotNull { it.ease }.average()
                        z = contributions.size
                    }
                }.toTypedArray()
            }
        }
    }
}
