package com.zegreatrob.coupling.client.components.contributor

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.playerConfigPage
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.hr
import react.router.useNavigate
import web.html.HTMLButtonElement

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
    val addEmailToExistingPlayer = { player: Player ->
        fun(_: MouseEvent<HTMLButtonElement, *>) {
            val updatedPlayer = player.copy(additionalEmails = player.additionalEmails + contributor.email)
            dispatchFunc { fire(SavePlayerCommand(partyId, updatedPlayer)) }()
        }
    }

    if (players.map(Player::id).contains(contributor.id)) {
        PlayerCard(contributor, size = 20)
        hr()
        CouplingButton {
            onClick = { navigate(partyId.with(contributor).playerConfigPage()) }
            +"Player Config"
        }
    } else {
        div { +contributor.name }
        div { +contributor.email }
        hr()
        div {
            CouplingButton {
                onClick = createPlayer
                +"Create Player"
            }
        }
        if (players.isNotEmpty()) {
            hr()
            div {
                h3 { +"Add Email to Existing Player" }
                div {
                    players.forEach { player ->
                        button {
                            key = player.id
                            ariaLabel = player.id
                            onClick = addEmailToExistingPlayer(player)
                            PlayerCard(player, size = 20)
                        }
                    }
                }
            }
        }
    }
}
