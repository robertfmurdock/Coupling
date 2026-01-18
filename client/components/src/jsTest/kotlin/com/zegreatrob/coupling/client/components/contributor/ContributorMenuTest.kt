package com.zegreatrob.coupling.client.components.contributor

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.Paths.playerConfigPath
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.assertNotNull
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotools.types.text.toNotBlankString
import react.FC
import react.create
import tanstack.history.createMemoryHistory
import tanstack.react.router.RootRouteOptions
import tanstack.react.router.RouteOptions
import tanstack.react.router.RouterOptions
import tanstack.react.router.RouterProvider
import tanstack.react.router.createRootRoute
import tanstack.react.router.createRoute
import tanstack.react.router.createRouter
import tanstack.router.core.RoutePath
import kotlin.test.Test

class ContributorMenuTest {
    @Test
    fun whenContributorIsAlsoPlayerWillShowPlayerConfigButton() = asyncSetup(object {
        val contributor = stubPlayer()
        val partyId = stubPartyId()
        val players = (stubPlayers(3) + contributor).shuffled()
        val user = UserEvent.setup()
        val router = createRouter(
            options = RouterOptions(
                routeTree = createRootRoute(
                    options = RootRouteOptions(notFoundComponent = FC {
                        ContributorMenu(contributor, players, partyId, StubDispatcher().func())
                    }),
                ).also {
                    it.addChildren(
                        arrayOf(
                            createRoute(
                                RouteOptions(
                                    path = RoutePath(partyId.with(contributor).playerConfigPath()),
                                    getParentRoute = { it },
                                    component = FC { +"Success" },
                                ),
                            ),
                        ),
                    )
                },
                history = createMemoryHistory(),
            ),
        )
    }) {
        render { RouterProvider { router = this@asyncSetup.router } }
    } exercise {
        user.click(screen.findByText("Player Config"))
    } verify {
        screen.findByText("Success").assertNotNull()
    }

    @Test
    fun whenContributorIsAlsoPlayerButOnlyByIdWillShowPlayerConfigButton() = asyncSetup(object {
        val contributor = stubPlayer()
        val partyId = stubPartyId()
        val players = (stubPlayers(3) + stubPlayer().copy(id = contributor.id)).shuffled()
    }) exercise {
        render(
            ContributorMenu.create(contributor, players, partyId, StubDispatcher().func()),
            RenderOptions(wrapper = TestRouter),
        )
    } verify {
        screen.findByText("Player Config").assertNotNull()
    }

    @Test
    fun whenContributorIsNotPlayerCanUseCreatePlayerButton() = asyncSetup(object {
        val contributor = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubDispatcher = StubDispatcher()
        val user = UserEvent.setup()
    }) {
        render(TestRouter.create { ContributorMenu(contributor, players, partyId, stubDispatcher.func()) })
    } exercise {
        user.click(screen.findByText("Create Player"))
    } verify {
        stubDispatcher.receivedActions
            .map {
                if (it !is SavePlayerCommand) {
                    it
                } else {
                    it.copy(player = it.player.copy(id = PlayerId("generated".toNotBlankString().getOrThrow())))
                }
            }
            .assertIsEqualTo(
                listOf(
                    SavePlayerCommand(
                        partyId,
                        contributor.copy(id = PlayerId("generated".toNotBlankString().getOrThrow())),
                    ),
                ),
            )
    }

    @Test
    fun whenContributorIsNotPlayerAddEmailToExistingPlayerWillWork() = asyncSetup(object {
        val contributor = stubPlayer()
        val targetPlayer = stubPlayer()
        val partyId = stubPartyId()
        val players = (stubPlayers(2) + targetPlayer).shuffled()
        val stubDispatcher = StubDispatcher()
        val user = UserEvent.setup()
    }) {
        render(TestRouter.create { ContributorMenu(contributor, players, partyId, stubDispatcher.func()) })
    } exercise {
        val addEmailToExistingPlayerSection = screen.findByText("Add Email to Existing Player").parentElement
        user.click(
            within(addEmailToExistingPlayerSection).findByRole(
                "button",
                RoleOptions(name = targetPlayer.id.value.toString()),
            ),
        )
    } verify {
        stubDispatcher.receivedActions
            .assertIsEqualTo(
                listOf(
                    SavePlayerCommand(
                        partyId = partyId,
                        player = targetPlayer.copy(additionalEmails = targetPlayer.additionalEmails + contributor.email),
                    ),
                ),
            )
    }
}
