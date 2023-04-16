package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.DeletePlayerCommand
import com.zegreatrob.coupling.action.SavePlayerCommand
import com.zegreatrob.coupling.client.components.StubDispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.act
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.fireEvent
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.waitFor
import com.zegreatrob.coupling.testreact.external.testinglibrary.userevent.UserEvent
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import org.w3c.dom.Window
import react.ReactNode
import react.create
import react.router.MemoryRouter
import react.router.PathRoute
import react.router.Routes
import kotlin.js.json
import kotlin.test.Test

class PlayerConfigTest {

    @Test
    fun selectingAvatarTypeWillAffectSavedPlayer() = asyncSetup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg", avatarType = null)
        val stubber = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), {}, stubber.func())
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.selectOptions(screen.getByRole("combobox", json("name" to "Avatar Type")), "DicebearAdventurer")
        actor.click(screen.getByRole("button", json("name" to "Save")))
    } verify {
        val expectedCommand = SavePlayerCommand(
            partyId = party.id,
            player = player.copy(avatarType = AvatarType.DicebearAdventurer),
        )
        stubber.commandsDispatched<SavePlayerCommand>()
            .assertIsEqualTo(listOf(expectedCommand))
    }

    @Test
    fun notSelectingAvatarTypeWillLeaveItNull() = asyncSetup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg", avatarType = null)
        val stubber = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), {}, stubber.func())
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.click(screen.getByRole("button", json("name" to "Save")))
    } verify {
        val expectedCommand = SavePlayerCommand(
            partyId = party.id,
            player = player,
        )
        stubber.commandsDispatched<SavePlayerCommand>()
            .assertIsEqualTo(listOf(expectedCommand))
    }

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = setup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg", avatarType = null)
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
        val player = Player(id = "blarg", badge = Badge.Alternate.value, avatarType = null)
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
        val player = Player(id = "blarg", badge = Badge.Default.value, avatarType = null)
        val reloaderSpy = SpyData<Unit, Unit>()
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), { reloaderSpy.spyFunction() }, stubDispatcher.func()).create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.type(screen.getByLabelText("Name"), "nonsense")

        fireEvent.submit(screen.getByRole("form"))
        act { stubDispatcher.simulateSuccess<SavePlayerCommand>() }
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
        val player = Player("blarg", badge = Badge.Alternate.value, avatarType = null)
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            MemoryRouter.create {
                Routes {
                    PathRoute {
                        path = "/${party.id.value}/pairAssignments/current/"
                        element = ReactNode("Fin")
                    }
                    PathRoute {
                        path = "*"
                        element = PlayerConfig(party, player, emptyList(), { }, stubDispatcher.func(), windowFuncs)
                            .create()
                    }
                }
            },
        )
    } exercise {
        actor.click(screen.getByText("Retire"))
        act { stubDispatcher.simulateSuccess<DeletePlayerCommand>() }
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
        val player = Player("blarg", badge = Badge.Alternate.value, avatarType = null)
        val actor = UserEvent.setup()
        val stubDispatcher = StubDispatcher()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), { }, stubDispatcher.func(), windowFunctions)
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.click(screen.getByText("Retire"))
    } verify {
        stubDispatcher.dispatchList.isEmpty().assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = asyncSetup(object {
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value, avatarType = null)
        val actor = UserEvent.setup()
    }) {
        render(
            PlayerConfig(party, player, emptyList(), { }, StubDispatchFunc())
                .create(),
            json("wrapper" to MemoryRouter),
        )
    } exercise {
        actor.type(screen.getByLabelText("Name"), "differentName")
    } verify {
//        wrapper.find(PromptComponent).props().`when`
//            .assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = asyncSetup(object {
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value, avatarType = null)
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
