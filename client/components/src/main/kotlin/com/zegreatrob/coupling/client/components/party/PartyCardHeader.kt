package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.CardHeader
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import csstype.BackgroundRepeat
import csstype.Color
import csstype.FontWeight
import csstype.Globals
import csstype.None
import csstype.Position
import csstype.TransitionProperty
import csstype.TransitionTimingFunction
import csstype.integer
import csstype.s
import csstype.url
import emotion.react.css

data class PartyCardHeader(val party: Party, val size: Int) : DataPropsBind<PartyCardHeader>(partyCardHeader)

val partyCardHeader by ntmFC<PartyCardHeader> { (party, size) ->
    CardHeader {
        this.size = size
        css {
            backgroundColor = Color("#EEF2F7D8")
            backgroundRepeat = BackgroundRepeat.repeatX
            backgroundImage = url(pngPath("overlay"))
            fontWeight = FontWeight.bold
            zIndex = integer(100)
            position = Position.relative
            transitionDuration = 0.4.s
            transitionProperty = "background".unsafeCast<TransitionProperty>()
            transitionTimingFunction = TransitionTimingFunction.easeOut

            "a" {
                color = Globals.inherit
                textDecoration = None.none
            }
        }
        this.headerContent = party.name ?: ""
    }
}
