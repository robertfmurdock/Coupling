package com.zegreatrob.coupling.client.components.contributor

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.objects.jso
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
    fun whenContributorIsNotPlayerWillShowCreatePlayerButton() = asyncSetup(object {
        val contributor = stubPlayer()
        val partyId = stubPartyId()
        val players = stubPlayers(3)
        val stubDispatcher = StubDispatcher()
        val user = UserEvent.setup()
    }) {
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    arrayOf(
                        jso {
                            path = "*"
                            element = ContributorMenu.create(contributor, players, partyId, stubDispatcher.func())
                        },
                    ),
                )
            },
        )
    } exercise {
        user.click(screen.findByText("Create Player"))
    } verify {
        stubDispatcher.receivedActions
            .map {
                if (it !is SavePlayerCommand) {
                    it
                } else {
                    it.copy(player = it.player.copy(id = "generated"))
                }
            }
            .assertIsEqualTo(listOf(SavePlayerCommand(partyId, contributor.copy(id = "generated"))))
    }
}
