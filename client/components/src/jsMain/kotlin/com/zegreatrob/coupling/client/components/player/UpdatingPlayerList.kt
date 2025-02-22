package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.VoidResult
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
val UpdatingPlayerList by nfc<UpdatingPlayerListProps<*>> { props ->
    var players by useState<List<Player>>(props.players)
    val addPlayer = { updated: Player -> players = players.merge(updated) }

    @Suppress("UNCHECKED_CAST")
    val dispatchFunc = props.dispatchFunc as DispatchFunc<Nothing>

    val newDispatchFunc = DispatchFunc { block ->
        dispatchFunc { block(SecretCannon(this, addPlayer)) }
    }
    +props.child(players, newDispatchFunc)
}

private fun List<Player>.merge(updated: Player): List<Player> {
    val existingPlayerIndex = indexOfFirst { p -> p.id == updated.id }
    return if (existingPlayerIndex == -1) {
        this + updated
    } else {
        replaceAtIndex(existingPlayerIndex, updated)
    }
}

private fun List<Player>.replaceAtIndex(targetIndex: Int, replacement: Player) = mapIndexed { index, player ->
    if (index == targetIndex) {
        replacement
    } else {
        player
    }
}

private class SecretCannon(
    val parent: ActionCannon<Nothing>,
    val addPlayer: (Player) -> Unit,
) : ActionCannon<Nothing> by parent {
    override suspend fun <R> fire(action: SuspendAction<Nothing, R>): R = parent.fire(action).also { updatePlayerState(action, it) }

    private fun <R> updatePlayerState(
        action: SuspendAction<Nothing, R>,
        result: R,
    ) {
        val unwrappedAction = action.unwrap()
        @Suppress("USELESS_IS_CHECK")
        if (unwrappedAction is SavePlayerCommand && result == VoidResult.Accepted) {
            addPlayer(unwrappedAction.player)
        }
    }
}

private fun <R> SuspendAction<*, R>.unwrap() = if (this is ActionWrapper<*, *>) {
    action
} else {
    this
}
