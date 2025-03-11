package com.zegreatrob.coupling.client.components.contributor

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
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
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.objects.jso
import kotools.types.text.toNotBlankString
import react.ReactNode
import react.create
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.Test

class ContributorMenuTest {
    @Test
    fun whenContributorIsAlsoPlayerWillShowPlayerConfigButton() = asyncSetup(object {
        val contributor = stubPlayer()
        val partyId = stubPartyId()
        val players = (stubPlayers(3) + contributor).shuffled()
        val user = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        jso {
                            path = "/${partyId.value}/player/${contributor.id}"
                            element = ReactNode("Success")
                        },
                        jso {
                            path = "*"
                            element = ContributorMenu.create(contributor, players, partyId, StubDispatcher().func())
                        },
                    ),
                )
            },
        )
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
            jso { wrapper = TestRouter },
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
