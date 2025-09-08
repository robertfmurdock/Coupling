package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.PartyId
import react.Props

@Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
external interface PartyButtonProps : Props {
    var partyId: PartyId
}
