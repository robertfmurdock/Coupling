package com.zegreatrob.coupling.client.components.contributor

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.Paths.playerConfigPage
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.router.useNavigate

external interface ContributorMenuProps : Props {
    var contributor: Player
    var players: List<Player>

    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
}

@ReactFunc
val ContributorMenu by nfc<ContributorMenuProps> { props ->
    val (contributor, _, partyId) = props
    val navigate = useNavigate()
    CouplingButton {
        onClick = { navigate(partyId.with(contributor).playerConfigPage()) }
        +"Player Config"
    }
}
