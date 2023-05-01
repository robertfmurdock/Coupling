package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.client.components.StubDispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.waitFor
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import js.core.jso
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import react.ReactNode
import react.create
import react.router.MemoryRouter
import react.router.PathRoute
import react.router.Routes
import kotlin.test.Test

class PartyConfigTest {

    @Test
    fun willDefaultPartyThatIsMissingData() = asyncSetup(object {
        val party = Party(PartyId("1"), name = "1")
    }) exercise {
        render(
            PartyConfig(
                party,
                StubDispatchFunc(),
            ).create(),
            jso { wrapper = MemoryRouter },
        )
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
        val party = Party(
            PartyId("1"),
            name = "1",
            alternateBadgeName = "alt",
            defaultBadgeName = "def",
            email = "email-y",
            pairingRule = PairingRule.PreferDifferentBadge,
        )
        val stubDispatcher = StubDispatcher()
    }) {
        render(
            MemoryRouter.create {
                Routes {
                    PathRoute {
                        path = "/parties/"
                        element = ReactNode("Parties!")
                    }
                    PathRoute {
                        path = "*"
                        element =
                            PartyConfig(party, stubDispatcher.func()).create()
                    }
                }
            },
        )
    } exercise {
        fireEvent.submit(screen.getByRole("form"))
        act { stubDispatcher.simulateSuccess<SavePartyCommand>() }
    } verify {
        waitFor {
            stubDispatcher.commandsDispatched<SavePartyCommand>()
                .assertIsEqualTo(listOf(SavePartyCommand(party)))
            screen.getByText("Parties!")
                .assertNotNull()
        }
    }

    @Test
    fun whenPartyIsNewWillSuggestIdAutomaticallyAndWillRetainIt() = asyncSetup(object {
        val party = Party(PartyId(""))
        val stubDispatcher = StubDispatcher()
    }) {
        render(
            PartyConfig(party, stubDispatcher.func()).create(),
            jso { wrapper = MemoryRouter },
        )
    } exercise {
        screen.getByLabelText("Unique Id").let { it as? HTMLInputElement }?.value
            .also { fireEvent.submit(screen.getByRole("form")) }
    } verify { automatedPartyId ->
        waitFor {
            stubDispatcher.commandsDispatched<SavePartyCommand>()
                .first()
                .party.id.value.run {
                    assertIsNotEqualTo("")
                    assertIsEqualTo(automatedPartyId)
                }
        }
        screen.getByLabelText("Unique Id").let { it as? HTMLInputElement }?.value
            .assertIsEqualTo(automatedPartyId)
    }
}
