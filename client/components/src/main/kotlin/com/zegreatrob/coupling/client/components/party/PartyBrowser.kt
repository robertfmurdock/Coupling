package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.dom.html.ReactHTML.div
import web.cssom.Display
import web.cssom.TextAlign
import web.cssom.WhiteSpace
import web.cssom.px

data class PartyBrowser(val party: Party) : DataPropsBind<PartyBrowser>(partyBrowser)

val partyBrowser by ntmFC<PartyBrowser> { (party) ->
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
