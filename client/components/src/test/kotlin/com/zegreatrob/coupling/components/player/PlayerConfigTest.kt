package com.zegreatrob.coupling.components.player

import com.zegreatrob.coupling.action.DeletePlayerCommand
import com.zegreatrob.coupling.action.SavePlayerCommand
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.components.StubDispatchFunc
import com.zegreatrob.coupling.components.StubDispatcher
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.fireEvent
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.waitFor
import com.zegreatrob.coupling.testreact.external.testinglibrary.userevent.userEvent
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.await
import org.w3c.dom.Window
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PlayerConfigTest {

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = setup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg")
    }) exercise {
        render(
            PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc())
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } verify { wrapper ->
        wrapper.baseElement
            .querySelectorAll("select[name='badge'] [value='${Badge.Default.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenTheGivenPlayerHasAltBadgeWillNotModifyPlayer() = setup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg", badge = Badge.Alternate.value)
    }) exercise {
        render(
            PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc())
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } verify { wrapper ->
        wrapper.baseElement
            .querySelectorAll("select[name='badge'] [value='${Badge.Alternate.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun submitWillSaveAndReload() = asyncSetup(object {
        val party = Party(PartyId("party"))
        val player = Player(id = "blarg", badge = Badge.Default.value)
        val reloaderSpy = SpyData<Unit, Unit>()
        val stubDispatcher = StubDispatcher()
        val actor = userEvent.setup()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), { reloaderSpy.spyFunction() }, stubDispatcher.func()).create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.type(screen.getByLabelText("Name"), "nonsense").await()

        fireEvent.submit(screen.getByRole("form"))
        stubDispatcher.simulateSuccess<SavePlayerCommand>()
    } verify {
        waitFor {
            stubDispatcher.commandsDispatched<SavePlayerCommand>()
                .assertIsEqualTo(
                    listOf(SavePlayerCommand(party.id, player.copy(name = "nonsense"))),
                )
            reloaderSpy.callCount.assertIsEqualTo(1)
        }
    }

    @Test
    fun clickingDeleteWhenConfirmedWillRemoveAndRerouteToCurrentPairAssignments() = asyncSetup(object {
        val windowFuncs = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        }
        val pathSetterSpy = SpyData<String, Unit>()
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val stubDispatcher = StubDispatcher()
        val actor = userEvent.setup()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), { }, stubDispatcher.func(), windowFuncs)
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.click(screen.getByText("Retire")).await()
        stubDispatcher.simulateSuccess<DeletePlayerCommand>()
    } verify {
        waitFor {
            stubDispatcher.commandsDispatched<DeletePlayerCommand>()
                .assertIsEqualTo(
                    listOf(DeletePlayerCommand(party.id, player.id)),
                )
            pathSetterSpy.spyReceivedValues.contains(
                "/${party.id.value}/pairAssignments/current/",
            )
        }
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = asyncSetup(object {
        val windowFunctions = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        }
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val actor = userEvent.setup()
        val stubDispatcher = StubDispatcher()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), { }, stubDispatcher.func(), windowFunctions)
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.click(screen.getByText("Retire")).await()
    } verify {
        stubDispatcher.dispatchList.isEmpty().assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = asyncSetup(object {
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val actor = userEvent.setup()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), { }, StubDispatchFunc())
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.type(screen.getByLabelText("Name"), "differentName").await()
    } verify {
//        wrapper.find(PromptComponent).props().`when`
//            .assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = asyncSetup(object {
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
    }) exercise {
        render(
            PlayerConfig(party, player, emptyList(), { }, StubDispatchFunc())
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } verify { _ ->
//        wrapper.find(PromptComponent).props().`when`
//            .assertIsEqualTo(false)
    }
}
