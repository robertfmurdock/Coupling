package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.dd
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.dl
import react.dom.html.ReactHTML.dt
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.section
import web.cssom.BoxSizing
import web.cssom.Color
import web.cssom.Display
import web.cssom.Flex
import web.cssom.FlexWrap
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.TextAlign
import web.cssom.em
import web.cssom.number
import web.cssom.px
import web.cssom.vw
import kotlin.time.Duration

external interface ContributionQuickStatsPanelProps : Props {
    var contributionCount: Int

    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var medianCycleTime: Duration?
    var cycleTimeCount: Int
    var contributorCount: Int
}

@ReactFunc
val ContributionQuickStatsPanel by nfc<ContributionQuickStatsPanelProps> { props ->
    section {
        css {
            boxSizing = BoxSizing.borderBox
            width = 70.vw
            maxWidth = 38.em
            marginBlock = 12.px
            padding = 12.px
            borderWidth = 2.px
            borderStyle = LineStyle.solid
            borderColor = Color("#c9a754")
            borderRadius = 10.px
            backgroundColor = Color("#fff7df")
            color = Color("#392809")
            textAlign = TextAlign.start
        }
        h2 {
            css { margin = 0.px }
            +"Contribution summary"
        }
        dl {
            css {
                display = Display.flex
                flexWrap = FlexWrap.wrap
                gap = 12.px
                margin = 0.px
                marginBlockStart = 8.px
            }
            summaryMetric("Recent contributions", props.contributionCount.toString())
            summaryMetric("With cycle time", props.cycleTimeCount.toString())
            summaryMetric("Median cycle time", props.medianCycleTime?.toString() ?: "Unavailable")
            summaryMetric("Contributors", props.contributorCount.toString())
        }
    }
}

private fun ChildrenBuilder.summaryMetric(label: String, value: String) {
    div {
        css { flex = Flex(number(1.0), number(1.0), 8.em) }
        dt { +label }
        dd {
            css {
                marginInlineStart = 0.px
                fontWeight = FontWeight.bold
            }
            +value
        }
    }
}
