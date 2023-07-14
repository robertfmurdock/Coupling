package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Display
import web.cssom.TextAlign
import web.cssom.WhiteSpace
import web.cssom.px

external interface PartyBrowserProps : Props {
    var party: PartyDetails
}

@ReactFunc
val PartyBrowser by nfc<PartyBrowserProps> { (party) ->
    div {
        css {
            whiteSpace = WhiteSpace.normal
            display = Display.block
            margin = 5.px
            textAlign = TextAlign.left
        }
        ConfigHeader {
            this.party = party
            +(party.name ?: "")
        }
    }
}
