package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.dispatchFunc
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.objects.jso
import kotlinx.browser.window
import org.w3c.dom.Window
import react.ChildrenBuilder
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

    private fun addEmailButton() = screen.getByRole("button", RoleOptions(name = "Add Additional Email"))
    private fun email2Input() = screen.queryByLabelText("Email 2")

    @Test
    fun selectingAvatarTypeWillAffectSavedPlayer() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = defaultPlayer.copy(id = "blarg")
        val actor = UserEvent.setup()
        val stubDispatcher = StubDispatcher()
    }) {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = emptyList(),
                        reload = {},
                        dispatchFunc = stubDispatcher.func(),
                    )
                }
            },
        )
        val element = screen.getByRole("combobox", RoleOptions(name = "Avatar Type"))
        act {
            actor.selectOptions(element, "DicebearAdventurer")
        }
    } exercise {
        act { actor.click(screen.getByRole("button", RoleOptions(name = "Save"))) }
    } verify {
        val expectedCommand = SavePlayerCommand(
            partyId = party.id,
            player = player.copy(avatarType = AvatarType.DicebearAdventurer),
        )
        stubDispatcher.receivedActions
            .assertIsEqualTo(listOf(expectedCommand))
    }

    @Test
    fun deselectingAvatarTypeWillRemoveIt() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = defaultPlayer.copy(id = "blarg", avatarType = AvatarType.Retro)
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = emptyList(),
                        reload = {},
                        dispatchFunc = stubDispatcher.func(),
                    )
                }
            },
        )
    } exercise {
        act {
            actor.selectOptions(screen.getByRole("combobox", RoleOptions(name = "Avatar Type")), "")
            actor.click(screen.getByRole("button", RoleOptions(name = "Save")))
        }
    } verify {
        val expectedCommand = SavePlayerCommand(
            partyId = party.id,
            player = player.copy(avatarType = null),
        )
        stubDispatcher.receivedActions
            .assertIsEqualTo(listOf(expectedCommand))
    }

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = setup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = defaultPlayer.copy(id = "blarg")
    }) exercise {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        boost = null,
                        player = player,
                        players = emptyList(),
                        reload = {},
                        dispatchFunc = dispatchFunc { {} },
                    )
                }
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
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = defaultPlayer.copy(id = "blarg", badge = Badge.Alternate.value)
    }) exercise {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        boost = null,
                        player = player,
                        players = emptyList(),
                        reload = {},
                        dispatchFunc = dispatchFunc { {} },
                    )
                }
            },
        )
    } verify { wrapper ->
        wrapper.baseElement
            .querySelectorAll("select[name='badge'] [value='${Badge.Alternate.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun canAddAdditionalEmailFieldAndSaveIt() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = defaultPlayer.copy(id = "blarg", email = "blarg@heh.io", avatarType = AvatarType.Multiavatar)
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
        val secondEmail = uuidString()
    }) {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = emptyList(),
                        reload = {},
                        dispatchFunc = stubDispatcher.func(),
                    )
                }
            },
        )
    } exercise {
        act { actor.click(addEmailButton()) }
        act { actor.type(email2Input(), secondEmail) }
        act { actor.click(screen.getByRole("button", RoleOptions(name = "Save"))) }
    } verify {
        val expectedCommand = SavePlayerCommand(
            partyId = party.id,
            player = player.copy(additionalEmails = setOf(secondEmail)),
        )
        stubDispatcher.receivedActions
            .assertIsEqualTo(listOf(expectedCommand))
    }

    @Test
    fun canAddAdditionalEmailFieldAndSavingItBlankDoesNotSaveBlank() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = defaultPlayer.copy(id = "blarg", email = "blarg@heh.io", avatarType = AvatarType.Multiavatar)
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = emptyList(),
                        reload = {},
                        dispatchFunc = stubDispatcher.func(),
                    )
                }
            },
        )
    } exercise {
        act { actor.click(addEmailButton()) }
        act { actor.click(screen.getByRole("button", RoleOptions(name = "Save"))) }
    } verify {
        stubDispatcher.receivedActions
            .assertIsEqualTo(listOf(SavePlayerCommand(partyId = party.id, player = player)))
    }

    @Test
    fun noAdditionalEmailFieldsAreShownByDefault() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = defaultPlayer.copy(id = "blarg", avatarType = AvatarType.Retro)
        val stubDispatcher = StubDispatcher()
    }) exercise {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = emptyList(),
                        reload = {},
                        dispatchFunc = stubDispatcher.func(),
                    )
                }
            },
        )
    } verify {
        email2Input()
            .assertIsEqualTo(null)
    }

    @Test
    fun submitWillSaveAndReload() = asyncSetup(object {
        val party = PartyDetails(PartyId("party"))
        val player = defaultPlayer.copy(id = "blarg", badge = Badge.Default.value)
        val reloaderSpy = SpyData<Unit, Unit>()
        val altStubDispatcher = StubDispatcher.Channel()
        val actor = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = emptyList(),
                        reload = { reloaderSpy.spyFunction() },
                        dispatchFunc = altStubDispatcher.func(),
                    )
                }
            },
        )
    } exercise {
        actor.type(screen.getByLabelText("Name"), "nonsense")

        fireEvent.submit(screen.getByRole("form"))
        act { altStubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(SavePlayerCommand(party.id, player.copy(name = "nonsense")))
        reloaderSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun clickingDeleteWhenConfirmedWillRemoveAndRerouteToCurrentPairAssignments() = asyncSetup(object {
        val windowFuncs = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        }
        val pathSetterSpy = SpyData<String, Unit>()
        val party = PartyDetails(PartyId("party"))
        val player = defaultPlayer.copy("blarg", badge = Badge.Alternate.value)
        val altStubDispatcher = StubDispatcher.Channel()
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
                            element = Fragment.create {
                                PlayerConfig(
                                    party = party,
                                    player = player,
                                    players = emptyList(),
                                    reload = { },
                                    dispatchFunc = altStubDispatcher.func(),
                                    windowFuncs = windowFuncs,
                                )
                            }
                        },
                    ),
                )
            },
        )
    } exercise {
        actor.click(screen.getByText("Retire"))
        act { altStubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(DeletePlayerCommand(party.id, player.id))
        pathSetterSpy.spyReceivedValues.contains(
            "/${party.id.value}/pairAssignments/current/",
        )
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = asyncSetup(object {
        val windowFunctions = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        }
        val party = PartyDetails(PartyId("party"))
        val player = defaultPlayer.copy("blarg", badge = Badge.Alternate.value)
        val actor = UserEvent.setup()
        val stubDispatcher = StubDispatcher()
    }) {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = emptyList(),
                        reload = { },
                        dispatchFunc = stubDispatcher.func(),
                        windowFuncs = windowFunctions,
                    )
                }
            },
        )
    } exercise {
        actor.click(screen.getByText("Retire"))
    } verify {
        stubDispatcher.receivedActions.isEmpty().assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = asyncSetup(object {
        val party = PartyDetails(PartyId("party"))
        val player = defaultPlayer.copy("blarg", badge = Badge.Alternate.value)
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
                                Link {
                                    to = "elsewhere"
                                    button { +"Leave" }
                                }
                                PlayerConfig(
                                    party = party,
                                    boost = null,
                                    player = player,
                                    players = emptyList(),
                                    reload = { },
                                    dispatchFunc = dispatchFunc { {} },
                                )
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
        val party = PartyDetails(PartyId("party"))
        val player = defaultPlayer.copy("blarg", badge = Badge.Alternate.value)
        val spy = SpyData<String, Boolean>().apply { spyWillReturn(true) }
        val confirmFunc: (message: String) -> Boolean = window::confirm
    }) {
        window.asDynamic()["confirm"] = spy::spyFunction
    } exercise {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PlayerConfig(
                        party = party,
                        boost = null,
                        player = player,
                        players = emptyList(),
                        reload = { },
                        dispatchFunc = dispatchFunc { {} },
                    )
                }
            },
        )
    } verifyAnd { _ ->
        spy.spyReceivedValues
            .assertIsEqualTo(emptyList())
    } teardown {
        window.asDynamic()["confirm"] = confirmFunc
    }
}

fun singleRouteRouter(block: ChildrenBuilder.() -> Unit) = createMemoryRouter(
    arrayOf(
        jso {
            path = "*"
            this.element = Fragment.create(block)
        },
    ),
)
