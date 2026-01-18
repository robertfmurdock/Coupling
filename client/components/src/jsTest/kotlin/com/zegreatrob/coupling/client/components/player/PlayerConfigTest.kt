package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.assertNotNull
import com.zegreatrob.coupling.client.components.dispatchFunc
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.waitFor
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlinx.browser.window
import org.w3c.dom.Window
import react.FC
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.button
import tanstack.react.router.Link
import tanstack.react.router.RootRouteOptions
import tanstack.react.router.RouteOptions
import tanstack.react.router.RouterOptions
import tanstack.react.router.RouterProvider
import tanstack.react.router.createRootRoute
import tanstack.react.router.createRoute
import tanstack.react.router.createRouter
import tanstack.router.core.RoutePath
import kotlin.js.json
import kotlin.test.Test

class PlayerConfigTest {

    private suspend fun addEmailButton() = screen.findByRole("button", RoleOptions(name = "Add Additional Email"))
    private suspend fun email2Input() = screen.queryByLabelText("Email 2")

    @Test
    fun selectingAvatarTypeWillAffectSavedPlayer() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = stubPlayer()
        val actor = UserEvent.setup()
        val stubDispatcher = StubDispatcher()
    }) {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = emptyList(),
                    reload = {},
                    dispatchFunc = stubDispatcher.func(),
                )
            }
        }
        val element = screen.findByRole("combobox", RoleOptions(name = "Avatar Type"))
        act {
            actor.selectOptions(element, "DicebearAdventurer")
        }
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions(name = "Save")))
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
        val player = stubPlayer().copy(avatarType = AvatarType.Retro)
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = emptyList(),
                    reload = {},
                    dispatchFunc = stubDispatcher.func(),
                )
            }
        }
    } exercise {
        act {
            actor.selectOptions(screen.findByRole("combobox", RoleOptions(name = "Avatar Type")), "")
            actor.click(screen.findByRole("button", RoleOptions(name = "Save")))
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
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = stubPlayer()
    }) exercise {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    boost = null,
                    player = player,
                    players = emptyList(),
                    reload = {},
                    dispatchFunc = dispatchFunc { {} },
                )
            }
        }
    } verify { wrapper ->
        screen.findByText("Player Configuration")
            .assertNotNull()
        wrapper.baseElement
            .querySelectorAll("select[name='badge'] [value='${Badge.Default.name}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenTheGivenPlayerHasAltBadgeWillNotModifyPlayer() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = stubPlayer().copy(badge = Badge.Alternate)
    }) exercise {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    boost = null,
                    player = player,
                    players = emptyList(),
                    reload = {},
                    dispatchFunc = dispatchFunc { {} },
                )
            }
        }
    } verify { wrapper ->
        screen.findByText("Player Configuration")
            .assertNotNull()
        wrapper.baseElement
            .querySelectorAll("select[name='badge'] [value='${Badge.Alternate.name}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun canAddAdditionalEmailFieldAndSaveIt() = asyncSetup(object {
        val party = PartyDetails(id = PartyId("party"), badgesEnabled = true, name = "Party tribe")
        val player = stubPlayer().copy(
            email = "blarg@heh.io",
            additionalEmails = emptySet(),
            avatarType = AvatarType.Multiavatar,
        )
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
        val secondEmail = uuidString().take(10)
    }) {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = listOf(player),
                    reload = {},
                    dispatchFunc = stubDispatcher.func(),
                )
            }
        }
        actor.click(addEmailButton())
        actor.type(email2Input(), secondEmail)
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Save")))
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
        val player = stubPlayer().copy(email = "blarg@heh.io", avatarType = AvatarType.Multiavatar)
        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = emptyList(),
                    reload = {},
                    dispatchFunc = stubDispatcher.func(),
                )
            }
        }
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
        val player = stubPlayer().copy(avatarType = AvatarType.Retro, additionalEmails = emptySet())
        val stubDispatcher = StubDispatcher()
    }) exercise {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = emptyList(),
                    reload = {},
                    dispatchFunc = stubDispatcher.func(),
                )
            }
        }
    } verify {
        email2Input()
            .assertIsEqualTo(null)
    }

    @Test
    fun submitWillSaveAndReload() = asyncSetup(object {
        val party = PartyDetails(PartyId("party"))
        val player = stubPlayer().copy(name = "", badge = Badge.Default)
        val reloaderSpy = SpyData<Unit, Unit>()
        val altStubDispatcher = StubDispatcher.Channel()
        val actor = UserEvent.setup()
    }) {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = listOf(player),
                    reload = { reloaderSpy.spyFunction() },
                    dispatchFunc = altStubDispatcher.func(),
                )
            }
        }
    } exercise {
        actor.type(screen.findByRole("textbox", RoleOptions("Name")), "nonsense")

        fireEvent.submit(screen.findByRole("form"))
        act { altStubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(SavePlayerCommand(party.id, player.copy(name = "nonsense")))
        reloaderSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun clickingDeleteAndConfirmingWillRemoveAndRerouteToCurrentPairAssignments() = asyncSetup(object {
        val windowFuncs = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        }
        val pathSetterSpy = SpyData<String, Unit>()
        val party = PartyDetails(PartyId("party"))
        val player = stubPlayer().copy(badge = Badge.Alternate)
        val altStubDispatcher = StubDispatcher.Channel()
        val actor = UserEvent.setup()
        val router = createRouter(
            options = RouterOptions(
                routeTree = createRootRoute().also {
                    it.addChildren(
                        arrayOf(
                            createRoute(
                                RouteOptions(
                                    path = RoutePath("/${party.id.value}/pairAssignments/current/"),
                                    getParentRoute = { it },
                                    component = FC { ReactNode("Fin") },
                                ),
                            ),
                        ),
                    )
                },
                defaultComponent = FC {
                    PlayerConfig(
                        party = party,
                        player = player,
                        players = listOf(player),
                        reload = { },
                        dispatchFunc = altStubDispatcher.func(),
                        windowFuncs = windowFuncs,
                    )
                },
            ),
        )
    }) {
        render { RouterProvider { router = this@asyncSetup.router } }
    } exercise {
        actor.click(screen.findByText("Retire"))
        act { altStubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(DeletePlayerCommand(party.id, player.id))
        pathSetterSpy.spyReceivedValues.contains(
            "/${party.id.value}/pairAssignments/current/",
        )
    }

    @Test
    fun whenPlayerIsNotInPlayerListRetireButtonIsDisabled() = asyncSetup(object {
        val party = PartyDetails(PartyId("party"))
        val player = stubPlayer().copy(badge = Badge.Alternate)
    }) exercise {
        render(
            TestRouter.create {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = emptyList(),
                    reload = { },
                    dispatchFunc = StubDispatcher().func(),
                )
            },
        )
    } verify {
        screen.queryByText("Retire").assertIsEqualTo(null)
    }

    @Test
    fun whenPlayerIsEditedRetireButtonRemainsEnabled() = asyncSetup(object {
        val party = PartyDetails(PartyId("party"))
        val player = stubPlayer().copy(badge = Badge.Alternate)
        val actor = UserEvent.setup()
    }) {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = listOf(player),
                    reload = { },
                    dispatchFunc = StubDispatcher().func(),
                )
            }
        }
    } exercise {
        actor.type(screen.findByLabelText("Name"), "differentName")
    } verify {
        screen.findByText("Retire").assertNotNull()
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = asyncSetup(object {
        val windowFunctions = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        }
        val party = PartyDetails(PartyId("party"))
        val player = stubPlayer().copy(badge = Badge.Alternate)
        val actor = UserEvent.setup()
        val stubDispatcher = StubDispatcher()
    }) {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    player = player,
                    players = listOf(player),
                    reload = { },
                    dispatchFunc = stubDispatcher.func(),
                    windowFuncs = windowFunctions,
                )
            }
        }
    } exercise {
        actor.click(screen.findByText("Retire"))
    } verify {
        stubDispatcher.receivedActions.isEmpty().assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = asyncSetup(object {
        val party = PartyDetails(PartyId("party"))
        val player = stubPlayer().copy(badge = Badge.Alternate)
        val actor = UserEvent.setup()
        val spy = SpyData<String, Boolean>().apply { spyWillReturn(true) }
        val confirmFunc: (message: String) -> Boolean = window::confirm
        val component = FC {
            Link {
                to = RoutePath("elsewhere")
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
        val router = createRouter(
            options = RouterOptions(
                routeTree = createRootRoute(
                    RootRouteOptions(notFoundComponent = component),

                ).also {
                    it.addChildren(
                        arrayOf(
                            createRoute(
                                RouteOptions(
                                    path = RoutePath("elsewhere"),
                                    getParentRoute = { it },
                                    component = FC { +"Elsewhere" },
                                ),
                            ),
                        ),
                    )
                },
            ),
        )
    }) {
        window.asDynamic()["confirm"] = spy::spyFunction
        println("begin")
        render { RouterProvider { router = this@asyncSetup.router } }
        println("after render")
        actor.type(screen.findByLabelText("Name"), "differentName")
        println("after type")
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions(name = "Leave")))
        println("after click")
    } verifyAnd {
        waitFor {
            println(spy.spyReceivedValues.size)
            spy.spyReceivedValues
                .assertIsEqualTo(listOf("You have unsaved data. Press OK to leave without saving."))
        }
    } teardown {
        window.asDynamic()["confirm"] = confirmFunc
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = asyncSetup(object {
        val party = PartyDetails(PartyId("party"))
        val player = stubPlayer().copy(badge = Badge.Alternate)
        val spy = SpyData<String, Boolean>().apply { spyWillReturn(true) }
        val confirmFunc: (message: String) -> Boolean = window::confirm
    }) {
        window.asDynamic()["confirm"] = spy::spyFunction
    } exercise {
        render {
            TestRouter {
                PlayerConfig(
                    party = party,
                    boost = null,
                    player = player,
                    players = emptyList(),
                    reload = { },
                    dispatchFunc = dispatchFunc { {} },
                )
            }
        }
    } verifyAnd { _ ->
        spy.spyReceivedValues
            .assertIsEqualTo(emptyList())
    } teardown {
        window.asDynamic()["confirm"] = confirmFunc
    }
}
