package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.external.react.useForm
import com.zegreatrob.coupling.client.external.react.windowTmFC
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import react.router.Navigate
import react.useState
import kotlin.js.Json

data class PlayerConfig(
    val tribe: Party,
    val player: Player,
    val players: List<Player>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) : DataPropsBind<PlayerConfig>(playerConfig)

val playerConfig by lazy { playerConfigFunc(WindowFunctions) }

val playerConfigFunc = windowTmFC<PlayerConfig> { props, windowFuncs ->
    val (tribe, player, players, reload, dispatchFunc) = props
    val (values, onChange) = useForm(player.toSerializable().toJsonDynamic().unsafeCast<Json>())

    val (redirectUrl, setRedirectUrl) = useState<String?>(null)

    val updatedPlayer = values.fromJsonDynamic<JsonPlayerData>().toModel()
    val onSubmit = dispatchFunc({ SavePlayerCommand(tribe.id, updatedPlayer) }, { reload() })
    val onRemove = dispatchFunc({ DeletePlayerCommand(tribe.id, player.id) },
        { setRedirectUrl(tribe.id.currentPairsPage()) })
        .requireConfirmation("Are you sure you want to delete this player?", windowFuncs)

    if (redirectUrl != null)
        Navigate { to = redirectUrl }
    else
        child(PlayerConfigContent(tribe, updatedPlayer, players, onChange, onSubmit, onRemove))
}
