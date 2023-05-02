package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.StubDispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import js.core.jso
import react.router.MemoryRouter
import kotlin.test.Test

class PinConfigEditorTest {

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object {
        val party = Party(PartyId(""))
        val pin = Pin(id = null)
    }) exercise {
        render(
            PinConfig(
                party,
                pin,
                emptyList(),
                {},
                StubDispatchFunc(),
            ).create(),
            jso { wrapper = MemoryRouter },
        )
    } verify {
        screen.queryByText("Retire")
            .assertIsEqualTo(null)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object {
        val party = Party(PartyId(""))
        val pin = Pin(id = "excellent id")
    }) exercise {
        render(
            PinConfig(
                party,
                pin,
                emptyList(),
                {},
                StubDispatchFunc(),
            ).create(),
            jso { wrapper = MemoryRouter },
        )
    } verify {
        screen.queryByText("Retire")
            .assertNotNull()
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = asyncSetup(object {
        val party = Party(PartyId("dumb party"))
        val pin = Pin(id = null, name = "")
        val newName = "pin new name"
        val newIcon = "pin new icon"

        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render(
            PinConfig(party, pin, emptyList(), {}, stubDispatcher.func()).create(),
            jso { wrapper = MemoryRouter },
        )
        actor.type(screen.getByLabelText("Name"), newName)
        actor.type(screen.getByLabelText("Icon"), newIcon)
    } exercise {
        fireEvent.submit(screen.getByRole("form"))
    } verify {
        stubDispatcher.commandsDispatched<SavePinCommand>()
            .assertIsEqualTo(listOf(SavePinCommand(party.id, Pin(name = newName, icon = newIcon))))
    }
}
