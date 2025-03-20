package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.chromatic.schemeCategory10
import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.scaleOrdinal
import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.scaleTime
import com.zegreatrob.coupling.client.components.graphing.external.d3.timeformat.timeFormat
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Area
import com.zegreatrob.coupling.client.components.graphing.external.recharts.AreaChart
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Legend
import com.zegreatrob.coupling.client.components.graphing.external.recharts.LinePoint
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ResponsiveContainer
import com.zegreatrob.coupling.client.components.graphing.external.recharts.XAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.YAxis
import com.zegreatrob.coupling.client.components.graphing.scaledTimeFormat
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.Object
import js.objects.Record
import js.objects.jso
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import react.Props
import web.cssom.WhiteSpace

external interface StoryContributionGraphProps : Props {
    var data: List<Contribution>
    var window: GqlContributionWindow
}

@ReactFunc
val StoryContributionGraph by nfc<StoryContributionGraphProps> { props ->
    val (data, window) = props
    val points = data.groupBy(contributionsByDate)
        .mapNotNull { group ->
            val date = group.key ?: return@mapNotNull null
            dateContributionCountByStory(date, group.value)
        }.toTypedArray()

    val stories = data.mapNotNull { contribution -> contribution.story }.distinct()

    val xMinMillis = points.minOf { it.x }
    val xMaxMillis = points.maxOf { it.x }
    val timeScale = scaleTime().domain(arrayOf(xMinMillis, xMaxMillis)).nice()
    val timeFormatter = timeFormat(scaledTimeFormat(xMinMillis, xMaxMillis))
    val myColor = scaleOrdinal().domain(stories.toTypedArray()).range(schemeCategory10)
    if (points.isNotEmpty()) {
        ResponsiveContainer {
            width = "100%"
            height = "100%"
            AreaChart {
                this.data = points
                XAxis {
                    dataKey = "x"
                    domain = timeScale.domain().map(js.date.Date::valueOf).toTypedArray()
                    scale = timeScale
                    type = "number"
                    ticks = timeScale.ticks(5).map(js.date.Date::valueOf).toTypedArray()
                    tickFormatter = timeFormatter
                    fontSize = 12
                }
                YAxis {
                    type = "number"
                }
                Legend {
                    width = "90%"
                    height = "7%"
                    wrapperStyle = jso { whiteSpace = WhiteSpace.preWrap }
                }
                stories.forEach { story ->
                    Area {
                        type = "monotone"
                        dataKey = story
                        stackId = "1"
                        stroke = myColor(story)
                        fill = myColor(story)
                    }
                }
            }
        }
    }
}

private fun dateContributionCountByStory(
    date: LocalDate,
    dateContributions: List<Contribution>,
): LinePoint = Object.assign(
    jso<LinePoint> {
        x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toInt()
    },
    dateContributions.groupBy(Contribution::story)
        .toList()
        .fold(Record<String, Int>()) { record, (story, storyContributions) ->
            if (story == null) {
                record
            } else {
                record.apply { set(story, storyContributions.size) }
            }
        },
)
