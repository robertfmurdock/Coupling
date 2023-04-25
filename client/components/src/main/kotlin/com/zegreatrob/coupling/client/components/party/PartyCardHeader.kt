package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.CardHeader
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import web.cssom.Globals
import web.cssom.None

data class PartyCardHeader(val party: Party, val size: Int) : DataPropsBind<PartyCardHeader>(partyCardHeader)

val partyCardHeader by ntmFC<PartyCardHeader> { (party, size) ->
    CardHeader {
        this.size = size
        css {
            "a" {
                color = Globals.inherit
                textDecoration = None.none
            }
        }
        this.headerContent = party.name ?: ""
    }
}
