package com.zegreatrob.coupling.client.components.contributor

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
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
    var dispatchFunc: DispatchFunc<SavePlayerCommand.Dispatcher>
}

@ReactFunc
val ContributorMenu by nfc<ContributorMenuProps> { props ->
    val (contributor, players, partyId, dispatchFunc) = props
    val navigate = useNavigate()

    val createPlayer = dispatchFunc {
        fire(SavePlayerCommand(partyId, contributor.copy(id = "${uuid4()}")))
    }

    if (players.contains(contributor)) {
        CouplingButton {
            onClick = { navigate(partyId.with(contributor).playerConfigPage()) }
            +"Player Config"
        }
    } else {
        CouplingButton {
            onClick = createPlayer
            +"Create Player"
        }
    }
}
