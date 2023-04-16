package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

data class TeamStatistics(
    val spinsUntilFullRotation: Int,
    val activePlayerCount: Int,
    val medianSpinDuration: String,
) : DataPropsBind<TeamStatistics>(teamStatistics)

val teamStatistics by ntmFC<TeamStatistics> { (spinsUntilFullRotation, activePlayerCount, medianSpinDuration) ->
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
