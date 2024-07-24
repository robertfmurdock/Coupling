package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PartyButtonProps
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.minreact.nfc
import react.router.dom.Link

val AddPlayerButton by nfc<PartyButtonProps> { props ->
    Link {
        to = "/${props.partyId.value}/player/new/"
        tabIndex = -1
        draggable = false
        CouplingButton(large, orange) {
            +"Add Player!"
        }
    }
}
