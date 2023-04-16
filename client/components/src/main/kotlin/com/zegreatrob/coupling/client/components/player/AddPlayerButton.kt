package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import react.Props
import react.router.dom.Link

external interface AddPlayerButtonProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
}

val addPlayerButton by nfc<AddPlayerButtonProps> { props ->
    Link {
        to = "/${props.partyId.value}/player/new/"
        tabIndex = -1
        draggable = false
        add(CouplingButton(large, orange)) {
            +"Add Player!"
        }
    }
}
