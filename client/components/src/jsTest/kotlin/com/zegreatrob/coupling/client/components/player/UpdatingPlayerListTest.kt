package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import react.ReactNode
import kotlin.test.Test

class UpdatingPlayerListTest {

    @Test
    fun whenSavePlayerCommandSucceedsWillAddPlayerToList() = asyncSetup(object {
        val newPlayer = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubDispatcher = StubDispatcher()
        var lastPlayersCallback: List<Player>? = null
        var dispatchFunc: DispatchFunc<SavePlayerCommand.Dispatcher>? = null
    }) {
        render {
            UpdatingPlayerList<SavePlayerCommand.Dispatcher>(
                players,
                dispatchFunc = stubDispatcher.func<SavePlayerCommand.Dispatcher>(),
                child = { players, dispatcher ->
                    lastPlayersCallback = players
                    dispatchFunc = dispatcher
                    ReactNode("lol")
                },
            )
        }
    } exercise {
        act { dispatchFunc?.invoke { fire(SavePlayerCommand(partyId, newPlayer)) }() }
    } verify { result ->
        stubDispatcher.receivedActions.contains(SavePlayerCommand(partyId, newPlayer))
        lastPlayersCallback.assertIsEqualTo(players + newPlayer)
    }
}
