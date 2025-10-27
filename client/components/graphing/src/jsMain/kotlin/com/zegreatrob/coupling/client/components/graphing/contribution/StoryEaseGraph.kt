package com.zegreatrob.coupling.client.components.graphing.contribution

import com.zegreatrob.coupling.client.components.graphing.external.recharts.LinePoint
import com.zegreatrob.coupling.client.components.graphing.external.recharts.RechartsMargin
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ResponsiveContainer
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Scatter
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ScatterChart
import com.zegreatrob.coupling.client.components.graphing.external.recharts.TickProps
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Tooltip
import com.zegreatrob.coupling.client.components.graphing.external.recharts.XAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.YAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ZAxis
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.core.toPrecision
import js.lazy.Lazy
import react.FC
import react.Props
import react.ReactNode
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.text
import react.dom.svg.TextAnchor

external interface StoryEaseGraphProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

val StoryEaseTick = FC<TickProps> { props ->
    g {
        transform = "translate(${props.x},${props.y})"
        text {
            x = 0.0
            y = 0.0
            dy = 4.0
            fill = "#666"
            transform = "rotate(-70)"
            textAnchor = TextAnchor.end
            +props.payload.value?.toString()
        }
    }
}

@ReactFunc
@Lazy
val StoryEaseGraph by nfc<StoryEaseGraphProps> { props ->
    val (pairsToReports, window) = props
    var storyContributions = pairsToReports.contributionsByStory()

    if (pairsToReports.flatMap { it.second.contributions?.elements ?: emptyList() }.isEmpty()) {
        return@nfc
    }
    ResponsiveContainer {
        width = "100%"
        height = "100%"
        ScatterChart {
            margin = RechartsMargin(bottom = 60, left = 40, right = 80, top = 20)
            XAxis {
                type = "category"
                dataKey = "x"
                name = "story"
                interval = 0
                tick = StoryEaseTick
            }
            YAxis {
                type = "number"
                dataKey = "y"
                ticks = arrayOf(1, 2, 3, 4, 5)
                name = "ease"
                domain = arrayOf(0, "dataMax")
            }
            Tooltip {
                formatter = { value, name ->
                    ReactNode(
                        if (name == "ease") {
                            value.unsafeCast<Double>().toPrecision(3)
                        } else {
                            value.toString()
                        },
                    )
                }
            }
            ZAxis {
                type = "number"
                dataKey = "z"
                name = "count"
                range = arrayOf(20, 1000)
                domain = arrayOf(0, 50)
            }
            Scatter {
                data = storyContributions.map { (story, contributions) ->
                    LinePoint(
                        x = story,
                        y = contributions.mapNotNull { it.ease }.average(),
                        z = contributions.size,
                    )
                }.toTypedArray()
            }
        }
    }
}

fun List<Pair<CouplingPair, ContributionReport>>.contributionsByStory(): Map<String, List<Contribution>> {
    val allContributions = flatMap { it.second.contributions?.elements ?: emptyList() }
        .distinctBy { it.id }
    val rawStoryContributions: Map<String, List<Contribution>> =
        allContributions.groupBy { it.story }
            .mapNotNull { (key, value) -> if (key != null) key to value else null }
            .toMap()

    val splitContributions = rawStoryContributions.flatMap {
        if (it.key.contains(",")) {
            it.key.split(",").map { story -> story to it.value }
        } else {
            emptyList()
        }
    }.toMap()

    val storyContributions = rawStoryContributions.filter { !it.key.contains(",") }.toMutableMap()

    splitContributions.forEach { (story, contributions) ->
        storyContributions[story] = contributions + storyContributions[story].orEmpty()
    }
    return storyContributions.filter { it.value.isNotEmpty() }
}
