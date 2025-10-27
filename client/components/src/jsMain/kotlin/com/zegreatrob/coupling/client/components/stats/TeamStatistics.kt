package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.VerticalAlign
import web.cssom.px
import kotlin.time.Duration

external interface TeamStatisticsProps : Props {
    var spinsUntilFullRotation: Int
    var activePlayerCount: Int

    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var medianSpinDuration: Duration?
}

@ReactFunc
val TeamStatistics by nfc<TeamStatisticsProps> { (spinsUntilFullRotation, activePlayerCount, medianSpinDuration) ->
    ReactHTML.div {
        css {
            display = Display.inlineBlock
            verticalAlign = VerticalAlign.top
            margin = 8.px
            padding = 10.px
            borderWidth = 2.px
            borderStyle = LineStyle.solid
            borderColor = Color("#8e8e8e")
            borderRadius = 5.px
            backgroundColor = Color("#ffffff")
        }
        StatsHeader { +"Team Stats" }
        ReactHTML.div {
            StatLabel { +"Spins Until Full Rotation:" }
            ReactHTML.span { +"$spinsUntilFullRotation" }
        }
        ReactHTML.div {
            StatLabel { +"Number of Active Players:" }
            ReactHTML.span { +"$activePlayerCount" }
        }
        ReactHTML.div {
            StatLabel { +"Median Spin Duration:" }
            ReactHTML.span { +medianSpinDuration?.format() }
        }
    }
}

private fun Duration.format() = formatDistance(inWholeMilliseconds.toInt(), 0)
