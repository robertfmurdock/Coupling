package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.currentPairsPath
import com.zegreatrob.coupling.client.components.eventHandler
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.components.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.JsonPlayerData
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props
import react.router.Navigate
import react.router.dom.usePrompt
import react.useState
import kotlin.js.Json

external interface PlayerConfigProps<P> : Props
    where P : SavePlayerCommand.Dispatcher, P : DeletePlayerCommand.Dispatcher {
    var party: PartyDetails
    var boost: Boost?
    var player: Player
    var players: List<Player>
    var reload: () -> Unit
    var dispatchFunc: DispatchFunc<P>
    var windowFuncs: WindowFunctions?
}

@ReactFunc
val PlayerConfig by nfc<PlayerConfigProps<*>> { props ->
    val (party, boost, player, players, reload, dispatchFunc, windowFuncs) = props
    val (values, setValues) = useState(player.toSerializable().toJsonDynamic().unsafeCast<Json>())
    val onChange = eventHandler(setValues::invoke)
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val updatedPlayer = values.fromJsonDynamic<JsonPlayerData>().toModel()
    usePrompt(
        unsafeJso {
            `when` = updatedPlayer != player
            message = "You have unsaved data. Press OK to leave without saving."
        },
    )
    val onSubmit = dispatchFunc {
        fire(
            SavePlayerCommand(
                partyId = party.id,
                player = updatedPlayer.copy(
                    additionalEmails = updatedPlayer.additionalEmails.filterNot(String::isBlank).toSet(),
                ),
            ),
        )
        reload()
    }
    val onRemove = if (!players.contains(player)) {
        null
    } else {
        dispatchFunc {
            fire(DeletePlayerCommand(party.id, player.id))
            setRedirectUrl(party.id.currentPairsPath())
        }.requireConfirmation("Are you sure you want to delete this player?", windowFuncs ?: WindowFunctions)
    }
    val onPlayerChange = { changedPlayer: Player ->
        setValues {
            changedPlayer
                .toSerializable()
                .toJsonDynamic()
                .unsafeCast<Json>()
        }
    }

    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        PlayerConfigContent(
            party = party,
            boost = boost,
            player = updatedPlayer,
            players = players,
            onChange = onChange,
            onSubmit = onSubmit,
            onRemove = onRemove,
            onPlayerChange = onPlayerChange,
        )
    }
}
