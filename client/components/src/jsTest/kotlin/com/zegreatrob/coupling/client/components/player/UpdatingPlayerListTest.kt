package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommandWrapper
import com.zegreatrob.coupling.action.party.fire
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
import kotlin.test.Test

class UpdatingPlayerListTest {

    @Test
    fun whenSavePartyCommandSucceedsWillAddPlayerToList() = asyncSetup(object {
        val newPlayer = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubCannon = StubCannon<SavePartyCommand.Dispatcher>(mutableListOf()).apply {
            givenAny(SavePartyCommandWrapper::class, VoidResult.Accepted)
        }
        var lastPlayersCallback: List<Player>? = null
        var dispatchFunc: DispatchFunc<SavePartyCommand.Dispatcher>? = null
    }) {
        render {
            UpdatingPlayerList(players, dispatchFunc = stubDispatchFunc(stubCannon)) { players, dispatcher ->
                lastPlayersCallback = players
                dispatchFunc = dispatcher
                +"lol"
            }
        }
    } exercise {
        act { dispatchFunc?.invoke { fire(SavePartyCommand(partyId = partyId, players = listOf(newPlayer))) }() }
    } verify {
        stubCannon.receivedActions.contains(SavePartyCommand(partyId = partyId, players = listOf(newPlayer)))
        lastPlayersCallback.assertIsEqualTo(players + newPlayer)
    }

    @Test
    fun whenSavePartyCommandSucceedsWillReplacePlayerInList() = asyncSetup(object {
        val targetPlayer = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubCannon = StubCannon<SavePartyCommand.Dispatcher>(mutableListOf()).apply {
            givenAny(SavePartyCommandWrapper::class, VoidResult.Accepted)
        }
        var lastPlayersCallback: List<Player>? = null
        var dispatchFunc: DispatchFunc<SavePartyCommand.Dispatcher>? = null
        val updatedPlayer = targetPlayer.copy(name = "Bill")
    }) {
        render {
            UpdatingPlayerList(
                players + targetPlayer,
                dispatchFunc = stubDispatchFunc(stubCannon),
            ) { players, dispatcher ->
                lastPlayersCallback = players
                dispatchFunc = dispatcher
                +"lol"
            }
        }
    } exercise {
        act { dispatchFunc?.invoke { fire(SavePartyCommand(partyId = partyId, players = listOf(updatedPlayer))) }() }
    } verify {
        stubCannon.receivedActions.contains(SavePartyCommand(partyId = partyId, players = listOf(updatedPlayer)))
        lastPlayersCallback.assertIsEqualTo(players + updatedPlayer)
    }

    @Test
    fun whenSavePartyCommandFailsWillNotAddPlayerToList() = asyncSetup(object {
        val newPlayer = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubDispatcher = StubDispatcher.Channel()
        var lastPlayersCallback: List<Player>? = null
        var dispatchFunc: DispatchFunc<SavePartyCommand.Dispatcher>? = null
    }) {
        render {
            UpdatingPlayerList(players, dispatchFunc = stubDispatcher.func()) { players, dispatcher ->
                lastPlayersCallback = players
                dispatchFunc = dispatcher
                +"lol"
            }
        }
    } exercise {
        act {
            dispatchFunc?.invoke { fire(SavePartyCommand(partyId = partyId, players = listOf(newPlayer))) }()
            stubDispatcher.onActionReturn(VoidResult.Rejected)
        }
    } verify {
        lastPlayersCallback.assertIsEqualTo(players)
    }
}
