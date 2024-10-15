package com.zegreatrob.coupling.client.components.contributor

import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
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
                            element = ContributorMenu.create(contributor, players, partyId)
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
}
