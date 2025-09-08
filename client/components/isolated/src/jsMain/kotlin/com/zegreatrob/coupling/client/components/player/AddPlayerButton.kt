package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PartyButtonProps
import com.zegreatrob.coupling.client.components.Paths.newPlayerConfigPath
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.minreact.nfc
import react.router.dom.Link

val AddPlayerButton by nfc<PartyButtonProps> { props ->
    Link {
        to = props.partyId.newPlayerConfigPath()
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = orange
            +"Add Player!"
        }
    }
}
