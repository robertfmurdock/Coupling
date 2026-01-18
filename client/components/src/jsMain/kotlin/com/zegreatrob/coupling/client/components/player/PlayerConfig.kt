package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.currentPairsPath
import com.zegreatrob.coupling.client.components.eventHandler
import com.zegreatrob.coupling.client.components.external.tanstack.reactrouter.UseBlockerOptions
import com.zegreatrob.coupling.client.components.external.tanstack.reactrouter.useBlocker
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
import kotlinx.browser.window
import react.Props
import react.useEffect
import react.useEffectOnceWithCleanup
import react.useState
import tanstack.react.router.useNavigate
import tanstack.router.core.RoutePath
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
    useEffectOnceWithCleanup {
        println("=== PlayerConfig mounted, blocker should be registered ===")
        onCleanup {
            println("=== PlayerConfig unmounting ===")
        }
    }
    useBlocker(
        UseBlockerOptions(
            shouldBlockFn = {
                println("sucka mc")
                if (updatedPlayer == player) {
                    false
                } else {
                    !window.confirm("You have unsaved data. Press OK to leave without saving.")
                }
            }
        ),
    )
    val navigate = useNavigate()
    useEffect(redirectUrl) {
        if (redirectUrl != null) {
            navigate(unsafeJso { to = RoutePath(redirectUrl) })
        }
    }

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

    if (redirectUrl == null) {
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
