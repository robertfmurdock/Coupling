package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.StubDispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.create
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.waitFor
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.core.jso
import kotlinx.browser.window
import org.w3c.dom.Window
import react.Fragment
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.button
import react.router.RouterProvider
import react.router.createMemoryRouter
import react.router.dom.Link
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
            RouterProvider.create {
                router = singleRouteRouter(
                    PlayerConfig(party, player, emptyList(), {}, stubber.func()),
                )
            },
        )
        val element = screen.getByRole("combobox", RoleOptions(name = "Avatar Type"))
        actor.selectOptions(element, "DicebearAdventurer")
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Save")))
    } verify {
        val expectedCommand = SavePlayerCommand(
            partyId = party.id,
            player = player.copy(avatarType = AvatarType.DicebearAdventurer),
        )
        stubber.commandsDispatched<SavePlayerCommand>()
            .assertIsEqualTo(listOf(expectedCommand))
    }

    @Test
    fun deselectingAvatarTypeWillRemoveIt() = asyncSetup(object {
        val party = Party(id = PartyId("party"), name = "Party tribe", badgesEnabled = true)
        val player = Player(id = "blarg", avatarType = AvatarType.BoringBeam)
        val stubber = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = singleRouteRouter(
                    PlayerConfig(party, player, emptyList(), {}, stubber.func()),
                )
            },
        )
    } exercise {
        actor.selectOptions(screen.getByRole("combobox", RoleOptions(name = "Avatar Type")), "")
        actor.click(screen.getByRole("button", RoleOptions(name = "Save")))
    } verify {
        val expectedCommand = SavePlayerCommand(
            partyId = party.id,
            player = player.copy(avatarType = null),
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
            RouterProvider.create {
                router = singleRouteRouter(
                    PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc()),
                )
            },
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
            RouterProvider.create {
                router = singleRouteRouter(
                    PlayerConfig(party, player, emptyList(), {}, StubDispatchFunc()),
                )
            },
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
            RouterProvider.create {
                router = singleRouteRouter(
                    PlayerConfig(party, player, emptyList(), { reloaderSpy.spyFunction() }, stubDispatcher.func()),
                )
            },
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
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        jso {
                            path = "/${party.id.value}/pairAssignments/current/"
                            element = ReactNode("Fin")
                        },
                        jso {
                            path = "*"
                            element = PlayerConfig(party, player, emptyList(), { }, stubDispatcher.func(), windowFuncs)
                                .create()
                        },
                    ),
                )
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
            RouterProvider.create {
                router = singleRouteRouter(
                    PlayerConfig(party, player, emptyList(), { }, stubDispatcher.func(), windowFunctions),
                )
            },
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
        val spy = SpyData<String, Boolean>().apply { spyWillReturn(true) }
        val confirmFunc: (message: String) -> Boolean = window::confirm
    }) {
        window.asDynamic()["confirm"] = spy::spyFunction
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        jso {
                            path = "elsewhere"
                            element = ReactNode("Elsewhere")
                        },
                        jso {
                            path = "*"
                            element = Fragment.create {
                                Link { to = "elsewhere"; button { +"Leave" } }
                                add(PlayerConfig(party, player, emptyList(), { }, StubDispatchFunc()))
                            }
                        },
                    ),
                )
            },
        )
        actor.type(screen.getByLabelText("Name"), "differentName")
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Leave")))
    } verifyAnd {
        spy.spyReceivedValues
            .assertIsEqualTo(listOf("You have unsaved data. Press OK to leave without saving."))
    } teardown {
        window.asDynamic()["confirm"] = confirmFunc
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = asyncSetup(object {
        val party = Party(PartyId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value, avatarType = null)
        val spy = SpyData<String, Boolean>().apply { spyWillReturn(true) }
        val confirmFunc: (message: String) -> Boolean = window::confirm
    }) {
        window.asDynamic()["confirm"] = spy::spyFunction
    } exercise {
        render(
            RouterProvider.create {
                router = singleRouteRouter(
                    PlayerConfig(party, player, emptyList(), { }, StubDispatchFunc()),
                )
            },
        )
    } verifyAnd { _ ->
        spy.spyReceivedValues
            .assertIsEqualTo(emptyList())
    } teardown {
        window.asDynamic()["confirm"] = confirmFunc
    }
}

fun singleRouteRouter(element: DataProps<*>) = createMemoryRouter(
    arrayOf(
        jso {
            path = "*"
            this.element = element.create()
        },
    ),
)
