package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommandWrapper
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.stubDispatchFunc
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.coupling.testaction.StubCannon
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
        val stubCannon = StubCannon<SavePlayerCommand.Dispatcher>(mutableListOf()).apply {
            givenAny(SavePlayerCommandWrapper::class, VoidResult.Accepted)
        }
        var lastPlayersCallback: List<Player>? = null
        var dispatchFunc: DispatchFunc<SavePlayerCommand.Dispatcher>? = null
    }) {
        render {
            UpdatingPlayerList<SavePlayerCommand.Dispatcher>(
                players,
                dispatchFunc = stubDispatchFunc(stubCannon),
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
        stubCannon.receivedActions.contains(SavePlayerCommand(partyId, newPlayer))
        lastPlayersCallback.assertIsEqualTo(players + newPlayer)
    }

    @Test
    fun whenSavePlayerCommandSucceedsWillReplacePlayerInList() = asyncSetup(object {
        val targetPlayer = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubCannon = StubCannon<SavePlayerCommand.Dispatcher>(mutableListOf()).apply {
            givenAny(SavePlayerCommandWrapper::class, VoidResult.Accepted)
        }
        var lastPlayersCallback: List<Player>? = null
        var dispatchFunc: DispatchFunc<SavePlayerCommand.Dispatcher>? = null
        val updatedPlayer = targetPlayer.copy(name = "Bill")
    }) {
        render {
            UpdatingPlayerList<SavePlayerCommand.Dispatcher>(
                players + targetPlayer,
                dispatchFunc = stubDispatchFunc(stubCannon),
                child = { players, dispatcher ->
                    lastPlayersCallback = players
                    dispatchFunc = dispatcher
                    ReactNode("lol")
                },
            )
        }
    } exercise {
        act { dispatchFunc?.invoke { fire(SavePlayerCommand(partyId, updatedPlayer)) }() }
    } verify { result ->
        stubCannon.receivedActions.contains(SavePlayerCommand(partyId, updatedPlayer))
        lastPlayersCallback.assertIsEqualTo(players + updatedPlayer)
    }

    @Test
    fun whenSavePlayerCommandFailsWillNotAddPlayerToList() = asyncSetup(object {
        val newPlayer = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubDispatcher = StubDispatcher.Channel()
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
        act {
            dispatchFunc?.invoke { fire(SavePlayerCommand(partyId, newPlayer)) }()
            stubDispatcher.onActionReturn(VoidResult.Rejected)
        }
    } verify { result ->
        lastPlayersCallback.assertIsEqualTo(players)
    }
}
