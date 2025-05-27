package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.chromatic.schemeCategory10
import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.scaleOrdinal
import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.scaleTime
import com.zegreatrob.coupling.client.components.graphing.external.d3.timeformat.timeFormat
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Area
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Bar
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ComposedChart
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Legend
import com.zegreatrob.coupling.client.components.graphing.external.recharts.LinePoint
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ResponsiveContainer
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Tooltip
import com.zegreatrob.coupling.client.components.graphing.external.recharts.XAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.YAxis
import com.zegreatrob.coupling.client.components.graphing.scaledTimeFormat
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.Object
import js.objects.Record
import js.objects.unsafeJso
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate
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
        }
        .sortedBy { it.x }
        .toTypedArray()

    val stories = (points.flatMap { Object.keys(it).toList() }.distinct() - "x")
    val xMinMillis = points.minOf { it.x }
    val xMaxMillis = points.maxOf { it.x }
    val timeScale = scaleTime().domain(arrayOf(xMinMillis, xMaxMillis)).nice()
    val timeFormatter = timeFormat(scaledTimeFormat(xMinMillis, xMaxMillis))
    val myColor = scaleOrdinal().domain(stories.toTypedArray()).range(schemeCategory10)
    if (points.isNotEmpty()) {
        ResponsiveContainer {
            width = "100%"
            height = "100%"
            ComposedChart {
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
                Tooltip {}
                Legend {
                    width = "90%"
                    height = "7%"
                    wrapperStyle = unsafeJso { whiteSpace = WhiteSpace.preWrap }
                }
                stories.forEach { story ->
                    val color = myColor(story)
                    Area {
                        key = "$story-area"
                        type = "monotone"
                        dataKey = story
                        stackId = "2"
                        stroke = color
                        fill = color
                    }
                    Bar {
                        key = "$story-bar"
                        type = "monotone"
                        barSize = 2
                        dataKey = story
                        stackId = "1"
                        stroke = color
                        fill = color
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
    unsafeJso<LinePoint> {
        x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate().getTime()
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
