package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.VerticalAlign
import web.cssom.px

external interface TeamStatisticsProps : Props {
    var spinsUntilFullRotation: Int
    var activePlayerCount: Int
    var medianSpinDuration: String
}

@ReactFunc
val TeamStatistics by nfc<TeamStatisticsProps> { (spinsUntilFullRotation, activePlayerCount, medianSpinDuration) ->
    div {
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
        div {
            StatLabel { +"Spins Until Full Rotation:" }
            span { +"$spinsUntilFullRotation" }
        }
        div {
            StatLabel { +"Number of Active Players:" }
            span { +"$activePlayerCount" }
        }
        div {
            StatLabel { +"Median Spin Duration:" }
            span { +medianSpinDuration }
        }
    }
}
