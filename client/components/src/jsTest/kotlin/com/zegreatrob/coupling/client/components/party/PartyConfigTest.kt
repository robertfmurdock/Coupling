package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.assertNotNull
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import react.FC
import tanstack.react.router.RootRouteOptions
import tanstack.react.router.RouteOptions
import tanstack.react.router.RouterOptions
import tanstack.react.router.RouterProvider
import tanstack.react.router.createRootRoute
import tanstack.react.router.createRoute
import tanstack.react.router.createRouter
import tanstack.router.core.RoutePath
import kotlin.test.Test

class PartyConfigTest {

    @Test
    fun willDefaultPartyThatIsMissingData() = asyncSetup(object {
        val party = PartyDetails(PartyId("1"), name = "1")
    }) exercise {
        render(RenderOptions(wrapper = TestRouter)) {
            PartyConfig(party = party, boost = null, isNew = false, dispatchFunc = DispatchFunc { {} })
        }
    } verify {
        within(screen.findByLabelText("Pairing Rule"))
            .getByRole("option", RoleOptions(selected = true))
            .let { it as? HTMLOptionElement }
            ?.label
            .assertIsEqualTo("Prefer Longest Time")
        screen.findByLabelText("Default Badge Name")
            .let { it as? HTMLInputElement }
            ?.value
            .assertIsEqualTo("Default")
        screen.findByLabelText("Alt Badge Name")
            .let { it as? HTMLInputElement }
            ?.value
            .assertIsEqualTo("Alternate")
    }

    @Test
    fun whenClickTheSaveButtonWillUseCouplingServiceToSaveTheParty() = asyncSetup(object {
        val party = PartyDetails(
            PartyId("1"),
            pairingRule = PairingRule.PreferDifferentBadge,
            defaultBadgeName = "def",
            alternateBadgeName = "alt",
            email = "email-y",
            name = "1",
        )
        val stubDispatcher = StubDispatcher.Channel()
        val router = createRouter(
            options = RouterOptions(
                routeTree = createRootRoute(
                    options = RootRouteOptions(
                        notFoundComponent = FC {
                            PartyConfig(
                                party = party,
                                boost = null,
                                isNew = false,
                                dispatchFunc = stubDispatcher.func()
                            )
                        },
                    ),
                ).also {
                    it.addChildren(
                        arrayOf(
                            createRoute(
                                RouteOptions(
                                    path = RoutePath("/parties/"),
                                    getParentRoute = { it },
                                    component = FC { +"Parties!" },
                                ),
                            ),
                        ),
                    )
                },
            ),
        )
    }) {
        render { RouterProvider { router = this@asyncSetup.router } }
    } exercise {
        fireEvent.submit(screen.findByRole("form"))
        act { stubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(SavePartyCommand(party))

        screen.findByText("Parties!")
            .assertNotNull()
    }

    @Test
    fun whenPartyIsNewWillSuggestIdAutomaticallyAndWillRetainIt() = asyncSetup(object {
        val party = PartyDetails(stubPartyId())
        val stubDispatcher = StubDispatcher()
    }) {
        render(RenderOptions(wrapper = TestRouter)) {
            PartyConfig(party = party, boost = null, isNew = true, dispatchFunc = stubDispatcher.func())
        }
    } exercise {
        screen.findByLabelText("Unique Id").let { it as? HTMLInputElement }?.value
            .also { act { fireEvent.submit(screen.findByRole("form")) } }
    } verify { automatedPartyId ->
        stubDispatcher.receivedActions
            .filterIsInstance<SavePartyCommand>()
            .first()
            .party.id.value.toString().run {
                assertIsNotEqualTo("")
                assertIsEqualTo(automatedPartyId)
            }
        screen.findByLabelText("Unique Id").let { it as? HTMLInputElement }?.value
            .assertIsEqualTo(automatedPartyId)
    }
}
