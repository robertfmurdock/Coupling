package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.chromatic.schemePaired
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
import emotion.react.css
import js.core.toPrecision
import js.date.Date
import js.lazy.Lazy
import js.objects.Object
import js.objects.Record
import js.objects.unsafeJso
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.WhiteSpace
import web.cssom.pct
import kotlin.math.max
import kotlin.time.toJSDate

external interface StoryContributionGraphProps : Props {
    var data: List<Contribution>
    var window: GqlContributionWindow
    var byPercent: Boolean
}

@ReactFunc
@Lazy
val StoryContributionGraph by nfc<StoryContributionGraphProps> { props ->
    val (data, window, byPercent) = props
    val contributionsByDate = data.groupBy(contributionsByDate)
    val points = contributionsByDate.mapNotNull { group ->
        val date = group.key ?: return@mapNotNull null
        dateContributionCountByStory(date, group.value, byPercent)
    }.sortedBy { it.x.unsafeCast<Double>() }.toTypedArray()

    val entries = contributionsByDate.entries
    val mapNotNull = entries.mapNotNull { Pair(it.key ?: return@mapNotNull null, it.value) }
    val sortedContributionsByDate =
        mapNotNull
            .sortedWith { l, r -> l.first.compareTo(r.first) }

    val stories = (points.flatMap { Object.Companion.keys(it).toList() }.distinct() - "x")
        .sortedWith { a, b -> preferLargerRangeWithEarlierTimes(sortedContributionsByDate, a, b) }
    val xMinMillis = points.minOfOrNull { it.x.unsafeCast<Double>() }
    val xMaxMillis = points.maxOfOrNull { it.x.unsafeCast<Double>() }
    div {
        css {
            width = 100.pct
            height = 100.pct
        }
        asDynamic()["data-testid"] = "contribution-graph"
        if (points.isEmpty() || xMinMillis == null || xMaxMillis == null) {
            return@div
        } else {
            val timeScale = scaleTime().domain(arrayOf(xMinMillis, xMaxMillis)).nice()
            val timeFormatter = timeFormat(scaledTimeFormat(xMinMillis, xMaxMillis))
            val myColor = scaleOrdinal().domain(stories.toTypedArray()).range(schemePaired)
            ResponsiveContainer {
                width = "100%"
                height = "100%"
                ComposedChart {
                    this.data = points
                    XAxis {
                        dataKey = "x"
                        domain = timeScale.domain().map(Date::valueOf).toTypedArray()
                        ticks = timeScale.ticks(5).map(Date::valueOf).toTypedArray()
                        tickFormatter = timeFormatter
                        fontSize = 12
                    }
                    YAxis {
                        type = "number"
                    }
                    Tooltip {
                        labelFormatter = { ReactNode(timeFormatter(it)) }
                        formatter = { value, _ -> ReactNode(value.unsafeCast<Double>().toPrecision(3)) }
                        content = { props ->
                            Tooltip.create {
                                +props
                                content = null
                                payload = props.payload?.filterIndexed { index, _ -> index % 2 == 0 }?.toTypedArray()
                            }
                        }
                    }
                    Legend {
                        width = "90%"
                        height = "7%"
                        wrapperStyle = unsafeJso { whiteSpace = WhiteSpace.Companion.preWrap }
                        content = { props ->
                            Legend.create {
                                +props
                                content = null
                                payload = props.payload?.filterIndexed { index, _ -> index % 2 == 0 }?.toTypedArray()
                            }
                        }
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
                            if (!byPercent) {
                                barSize = 2
                            }
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
}

private fun preferLargerRangeWithEarlierTimes(
    sortedContributionsByDate: List<Pair<LocalDate, List<Contribution>>>,
    a: JsString,
    b: JsString,
): Int {
    val (minA, maxA) = contiguousRange(sortedContributionsByDate, a)
    val (minB, maxB) = contiguousRange(sortedContributionsByDate, b)
    return if (minA.compareTo(minB) != 0) {
        minA.compareTo(minB)
    } else if (maxA.compareTo(maxB) != 0) {
        maxB.compareTo(maxA)
    } else {
        (maxA - minA) - (maxB - minB)
    }
}

private fun contiguousRange(
    sortedContributionsByDate: List<Pair<LocalDate, List<Contribution>>>,
    story: JsString,
): Pair<Int, Int> {
    val minIndex =
        sortedContributionsByDate.indexOfFirst { it.second.any { it.story == story || it.story?.contains(story) == true } }
    val maxIndex = sortedContributionsByDate.subList(minIndex + 1, sortedContributionsByDate.size)
        .indexOfFirst { it.second.all { it.story != story && it.story?.contains(story) == false } }
    return Pair(minIndex, max(minIndex, if (maxIndex < 0) sortedContributionsByDate.size else maxIndex + minIndex))
}

private fun dateContributionCountByStory(
    date: LocalDate,
    dateContributions: List<Contribution>,
    byPercent: Boolean,
): LinePoint = Object.Companion.assign(
    unsafeJso<LinePoint> {
        x = date.atTime(0, 0).toInstant(TimeZone.Companion.currentSystemDefault()).toJSDate().getTime()
    },
    dateContributions.groupBy(Contribution::story).toList()
        .fold(Record<String, Double>()) { record, (story, storyContributions) ->
            if (story == null) {
                record
            } else {
                val stories = story.allDistinctStories()
                stories.forEach { storyName ->
                    val previousEntries = record[storyName].unsafeCast<Double?>() ?: 0.0
                    val total = previousEntries + (storyContributions.size.toDouble() / stories.size)
                    record[storyName] = total
                }
                record
            }
        }.also { record ->
            if (byPercent) {
                val stories = Object.Companion.keys(record)
                val total = stories.sumOf { key: String -> record[key].unsafeCast<Double>() }
                stories.forEach { key -> record[key] = record[key].unsafeCast<Double>() / total }
            }
        },
)

private fun String.allDistinctStories(): List<String> = split(",").map { it.trim() }
