package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.ActionWrapper
import com.zegreatrob.testmints.action.async.SuspendAction
import react.Props
import react.ReactNode
import react.useState

external interface UpdatingPlayerListProps<D> : Props where D : SavePlayerCommand.Dispatcher {
    var players: List<Player>
    var dispatchFunc: DispatchFunc<D>
    var child: (List<Player>, DispatchFunc<D>) -> ReactNode
}

@ReactFunc
val UpdatingPlayerList by nfc<UpdatingPlayerListProps<Nothing>> { props ->
    var players by useState<List<Player>>(props.players)
    val childFunc = props.child

    val newDispatchFunc = SecretDispatchFunc(props.dispatchFunc) { players = players + it }
    +childFunc(players, newDispatchFunc)
}

private class SecretDispatchFunc(val dispatchFunc: DispatchFunc<Nothing>, val addPlayer: (Player) -> Unit) : DispatchFunc<Nothing> {
    override fun invoke(block: suspend ActionCannon<Nothing>.() -> Unit) = dispatchFunc { SecretCannon(this, addPlayer).block() }
}

private class SecretCannon(
    val parent: ActionCannon<Nothing>,
    val addPlayer: (Player) -> Unit,
) : ActionCannon<Nothing> by parent {
    override suspend fun <R> fire(action: SuspendAction<Nothing, R>): R {
        val unwrappedAction = action.unwrap()
        @Suppress("USELESS_IS_CHECK")
        if (unwrappedAction is SavePlayerCommand) {
            addPlayer(unwrappedAction.player)
        }
        return parent.fire(action)
    }
}

private fun <R> SuspendAction<*, R>.unwrap() = if (this is ActionWrapper<*, *>) {
    action
} else {
    this
}
