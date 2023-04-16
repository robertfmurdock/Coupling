package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import csstype.Display
import csstype.TextAlign
import csstype.WhiteSpace
import csstype.px
import emotion.react.css
import react.dom.html.ReactHTML.div

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
