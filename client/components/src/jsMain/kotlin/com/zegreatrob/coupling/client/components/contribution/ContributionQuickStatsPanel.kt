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
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.section
import web.cssom.BoxSizing
import web.cssom.Display
import web.cssom.Flex
import web.cssom.FlexWrap
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.TextAlign
import web.cssom.dvw
import web.cssom.em
import web.cssom.number
import web.cssom.pct
import web.cssom.px
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
    div {
        css {
            width = 100.pct
            maxWidth = 75.dvw
            textAlign = TextAlign.center
        }
        section {
            css {
                boxSizing = BoxSizing.borderBox
                display = Display.inlineBlock
                width = 46.em
                maxWidth = 100.pct
                marginBlock = 12.px
                padding = 12.px
                textAlign = TextAlign.center
            }
            h2 {
                css {
                    margin = 0.px
                    fontWeight = FontWeight.bold
                }
                +"Contribution summary"
            }
            p {
                css {
                    marginBlock = 8.px
                    fontSize = 0.85.em
                }
                +"Based on the recent contributions shown here."
            }
            dl {
                css {
                    display = Display.flex
                    flexWrap = FlexWrap.wrap
                    justifyContent = JustifyContent.center
                    gap = 16.px
                    margin = 0.px
                    marginBlockStart = 16.px
                }
                summaryMetric("Recent contributions", props.contributionCount.toString())
                summaryMetric("With cycle time", props.cycleTimeCount.toString())
                summaryMetric("Median cycle time", props.medianCycleTime?.toString() ?: "Unavailable")
                summaryMetric("Contributors", props.contributorCount.toString())
            }
        }
    }
}

private fun ChildrenBuilder.summaryMetric(label: String, value: String) {
    div {
        css {
            flex = Flex(number(1.0), number(1.0), 8.em)
            minWidth = 0.px
        }
        dt {
            css {
                fontWeight = FontWeight.bold
                fontSize = 0.9.em
            }
            +label
        }
        dd {
            css {
                marginInlineStart = 0.px
                marginBlockStart = 8.px
                fontSize = 1.5.em
            }
            +value
        }
    }
}
