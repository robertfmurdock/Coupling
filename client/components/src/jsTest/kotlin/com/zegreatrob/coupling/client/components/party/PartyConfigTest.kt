package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
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
import js.objects.unsafeJso
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import react.Fragment
import react.ReactNode
import react.create
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.Test

class PartyConfigTest {

    @Test
    fun willDefaultPartyThatIsMissingData() = asyncSetup(object {
        val party = PartyDetails(PartyId("1"), name = "1")
    }) exercise {
        render(unsafeJso { wrapper = TestRouter }) {
            PartyConfig(party = party, boost = null, isNew = false, dispatchFunc = DispatchFunc { {} })
        }
    } verify {
        within(screen.getByLabelText("Pairing Rule"))
            .getByRole("option", RoleOptions(selected = true))
            .let { it as? HTMLOptionElement }
            ?.label
            .assertIsEqualTo("Prefer Longest Time")
        screen.getByLabelText("Default Badge Name")
            .let { it as? HTMLInputElement }
            ?.value
            .assertIsEqualTo("Default")
        screen.getByLabelText("Alt Badge Name")
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
    }) {
        render(
            RouterProvider.create {
                router = createMemoryRouter(
                    routes = arrayOf(
                        unsafeJso {
                            path = "/parties/"
                            element = ReactNode("Parties!")
                        },
                        unsafeJso {
                            path = "*"
                            element = Fragment.create {
                                PartyConfig(
                                    party = party,
                                    boost = null,
                                    isNew = false,
                                    dispatchFunc = stubDispatcher.func(),
                                )
                            }
                        },
                    ),
                )
            },
        )
    } exercise {
        fireEvent.submit(screen.getByRole("form"))
        act { stubDispatcher.onActionReturn(VoidResult.Accepted) }
    } verify { action ->
        action.assertIsEqualTo(SavePartyCommand(party))

        screen.getByText("Parties!")
            .assertNotNull()
    }

    @Test
    fun whenPartyIsNewWillSuggestIdAutomaticallyAndWillRetainIt() = asyncSetup(object {
        val party = PartyDetails(stubPartyId())
        val stubDispatcher = StubDispatcher()
    }) {
        render(unsafeJso { wrapper = TestRouter }) {
            PartyConfig(party = party, boost = null, isNew = true, dispatchFunc = stubDispatcher.func())
        }
    } exercise {
        screen.getByLabelText("Unique Id").let { it as? HTMLInputElement }?.value
            .also { act { fireEvent.submit(screen.getByRole("form")) } }
    } verify { automatedPartyId ->
        stubDispatcher.receivedActions
            .filterIsInstance<SavePartyCommand>()
            .first()
            .party.id.value.toString().run {
                assertIsNotEqualTo("")
                assertIsEqualTo(automatedPartyId)
            }
        screen.getByLabelText("Unique Id").let { it as? HTMLInputElement }?.value
            .assertIsEqualTo(automatedPartyId)
    }
}
