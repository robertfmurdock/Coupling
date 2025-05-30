package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.unsafeJso
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.TextAlign
import web.cssom.em

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
                dangerouslySetInnerHTML = unsafeJso { __html = parse(loadMarkdownString("ContributionStart")) }
            }
        }
    }
}
