package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.currentPairsPage
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.components.external.w3c.requireConfirmation
import com.zegreatrob.coupling.client.components.useForm
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
import js.core.jso
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
    var dispatchFunc: DispatchFunc<out P>
    var windowFuncs: WindowFunctions?
}

@ReactFunc
val PlayerConfig by nfc<PlayerConfigProps<*>> { props ->
    val (party, boost, player, players, reload, dispatchFunc, windowFuncs) = props
    val (values, onChange) = useForm(player.toSerializable().toJsonDynamic().unsafeCast<Json>())
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val updatedPlayer = values.fromJsonDynamic<JsonPlayerData>().toModel()
    usePrompt(
        jso {
            `when` = updatedPlayer != player
            message = "You have unsaved data. Press OK to leave without saving."
        },
    )
    val onSubmit = dispatchFunc {
        fire(SavePlayerCommand(party.id, updatedPlayer))
        reload()
    }
    val onRemove = dispatchFunc {
        fire(DeletePlayerCommand(party.id, player.id))
        setRedirectUrl(party.id.currentPairsPage())
    }.requireConfirmation("Are you sure you want to delete this player?", windowFuncs ?: WindowFunctions)

    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        PlayerConfigContent(party, boost, updatedPlayer, players, onChange, onSubmit, onRemove)
    }
}
