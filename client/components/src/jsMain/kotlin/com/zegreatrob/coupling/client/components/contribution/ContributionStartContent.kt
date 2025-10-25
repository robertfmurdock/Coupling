package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.MarkdownContent
import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.DangerouslySetInnerHTML
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.TextAlign
import web.cssom.em
import web.html.HtmlSource

external interface ContributionStartContentProps : Props {
    var party: PartyDetails
}

@ReactFunc
val ContributionStartContent by nfc<ContributionStartContentProps> {
    div {
        div {
            css {
                display = Display.inlineFlex
                flexDirection = FlexDirection.column
                alignItems = AlignItems.center
                width = 40.em
                textAlign = TextAlign.left
            }
            div {
                dangerouslySetInnerHTML = DangerouslySetInnerHTML(
                    __html = HtmlSource(parse(MarkdownContent.content.contributionStartMd)),
                )
            }
        }
    }
}
