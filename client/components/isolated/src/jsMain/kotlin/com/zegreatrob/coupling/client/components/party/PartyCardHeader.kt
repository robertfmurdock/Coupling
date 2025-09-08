package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.CardHeader
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import web.cssom.Globals
import web.cssom.None

external interface PartyCardHeaderProps : Props {
    var party: PartyDetails
    var size: Int
}

@ReactFunc
val PartyCardHeader by nfc<PartyCardHeaderProps> { (party, size) ->
    CardHeader {
        this.size = size
        css {
            "a" {
                color = Globals.Companion.inherit
                textDecoration = None.Companion.none
            }
        }
        this.headerContent = party.name ?: ""
    }
}
