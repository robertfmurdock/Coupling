package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.pairassignments.viewHistoryButton
import com.zegreatrob.coupling.client.components.pin.pinListButton
import com.zegreatrob.coupling.client.components.player.AddPlayerButton
import com.zegreatrob.coupling.client.components.player.viewRetireesButton
import com.zegreatrob.coupling.client.components.stats.statisticsButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props

external interface PartyNavigationProps : Props {
    var party: PartyDetails
}

@ReactFunc
val PartyNavigation by nfc<PartyNavigationProps> { (party) ->
    NavigationPanel {
        settingsButton(party)
        AddPlayerButton { this.partyId = party.id }
        viewHistoryButton(party)
        pinListButton(party)
        statisticsButton(party)
        viewRetireesButton(party)
    }
}
