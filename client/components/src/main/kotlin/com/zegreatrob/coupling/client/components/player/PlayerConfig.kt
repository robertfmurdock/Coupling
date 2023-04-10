package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.DeletePlayerCommand
import com.zegreatrob.coupling.action.SavePlayerCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.currentPairsPage
import com.zegreatrob.coupling.client.components.useForm
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.JsonPlayerData
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import react.router.Navigate
import react.useState
import kotlin.js.Json

data class PlayerConfig<P>(
    val party: Party,
    val player: Player,
    val players: List<Player>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out P>,
    val windowFuncs: WindowFunctions = WindowFunctions,
) : DataPropsBind<PlayerConfig<P>>(component.unsafeCast<TMFC>())
    where P : SavePlayerCommand.Dispatcher, P : DeletePlayerCommand.Dispatcher {
    companion object {
        private val component = playerConfig<Dispatcho>()
    }
}

private interface Dispatcho : SavePlayerCommand.Dispatcher, DeletePlayerCommand.Dispatcher

private fun <P> playerConfig() where P : SavePlayerCommand.Dispatcher, P : DeletePlayerCommand.Dispatcher =
    tmFC<PlayerConfig<P>> { props ->
        val (party, player, players, reload, dispatchFunc, windowFuncs) = props
        val (values, onChange) = useForm(player.toSerializable().toJsonDynamic().unsafeCast<Json>())

        val (redirectUrl, setRedirectUrl) = useState<String?>(null)

        val updatedPlayer = values.fromJsonDynamic<JsonPlayerData>().toModel()
        val onSubmit = dispatchFunc({ SavePlayerCommand(party.id, updatedPlayer) }, { reload() })
        val onRemove = dispatchFunc(
            { DeletePlayerCommand(party.id, player.id) },
            { setRedirectUrl(party.id.currentPairsPage()) },
        ).requireConfirmation("Are you sure you want to delete this player?", windowFuncs)

        if (redirectUrl != null) {
            Navigate { to = redirectUrl }
        } else {
            add(PlayerConfigContent(party, updatedPlayer, players, onChange, onSubmit, onRemove))
        }
    }
