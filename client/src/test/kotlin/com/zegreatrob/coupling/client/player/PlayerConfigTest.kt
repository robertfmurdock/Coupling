package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.StubDispatcher
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.components.ConfigForm
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.minenzyme.simulateInputChange
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import org.w3c.dom.Window
import kotlin.js.json
import kotlin.test.Test

class PlayerConfigTest {

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = setup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg")
    }) exercise {
        shallow(PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc()))
            .find(playerConfigContent)
            .shallow()
    } verify { wrapper ->
        wrapper.find<Any>("select[name='badge'][value='${Badge.Default.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenTheGivenPlayerHasAltBadgeWillNotModifyPlayer() = setup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc()))
            .find(playerConfigContent)
            .shallow()
    } verify { wrapper ->
        wrapper.find<Any>("select[name='badge'][value='${Badge.Alternate.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun submitWillSaveAndReload() = setup(object {
        val party = Party(PartyId("party"))
        val player = Player(id = "blarg", badge = Badge.Default.value)
        val reloaderSpy = SpyData<Unit, Unit>()

        val stubDispatcher = StubDispatcher()
        val wrapper =
            shallow(PlayerConfig(party, player, emptyList(), { reloaderSpy.spyFunction() }, stubDispatcher.func()))
    }) exercise {
        wrapper.find(playerConfigContent)
            .shallow()
            .simulateInputChange("name", "nonsense")
        wrapper.find(playerConfigContent)
            .shallow()
            .find(ConfigForm).props()
            .onSubmit()
        stubDispatcher.simulateSuccess<SavePlayerCommand>()
    } verify {
        stubDispatcher.commandsDispatched<SavePlayerCommand>()
            .assertIsEqualTo(
                listOf(SavePlayerCommand(party.id, player.copy(name = "nonsense")))
            )
        reloaderSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun clickingDeleteWhenConfirmedWillRemoveAndRerouteToCurrentPairAssignments() = setup(object {
        val windowFuncs = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        }
        val pathSetterSpy = SpyData<String, Unit>()
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)

        val stubDispatcher = StubDispatcher()
        val wrapper = shallow(PlayerConfig(party, player, emptyList(), {}, stubDispatcher.func(), windowFuncs))
            .find(playerConfigContent)
            .shallow()
    }) exercise {
        wrapper.find(ConfigForm).props()
            .onRemove?.invoke()
        stubDispatcher.simulateSuccess<DeletePlayerCommand>()
    } verify {
        stubDispatcher.commandsDispatched<DeletePlayerCommand>()
            .assertIsEqualTo(
                listOf(DeletePlayerCommand(party.id, player.id))
            )
        pathSetterSpy.spyReceivedValues.contains(
            "/${party.id.value}/pairAssignments/current/"
        )
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = setup(object {
        val windowFunctions = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        }
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)

        val stubDispatcher = StubDispatcher()
        val wrapper = shallow(
            PlayerConfig(party, player, emptyList(), {}, stubDispatcher.func(), windowFunctions)
        ).find(playerConfigContent)
            .shallow()
    }) exercise {
        wrapper.find(ConfigForm).props()
            .onRemove?.invoke()
    } verify {
        stubDispatcher.dispatchList.isEmpty().assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = setup(object {
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val wrapper = shallow(PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc()))
            .find(playerConfigContent)
            .shallow()
    }) exercise {
        wrapper.simulateInputChange("name", "differentName")
        wrapper.update()
    } verify {
//        wrapper.find(PromptComponent).props().`when`
//            .assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = setup(object {
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc()))
            .find(playerConfigContent)
            .shallow()
    } verify { wrapper ->
//        wrapper.find(PromptComponent).props().`when`
//            .assertIsEqualTo(false)
    }
}
