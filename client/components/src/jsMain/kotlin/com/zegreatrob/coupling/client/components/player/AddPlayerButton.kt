package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.PartyButtonProps
import com.zegreatrob.coupling.client.components.Paths.newPlayerConfigPath
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.minreact.nfc

val AddPlayerButton by nfc<PartyButtonProps> { props ->
    CouplingLink {
        to = props.partyId.newPlayerConfigPath()
        sizeRuleSet = large
        colorRuleSet = orange
        +"Add Player!"
    }
}
