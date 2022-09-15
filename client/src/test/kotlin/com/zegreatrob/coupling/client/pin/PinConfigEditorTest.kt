package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.StubDispatcher
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.pairassignments.assertNotNull
import com.zegreatrob.coupling.components.pin.SavePinCommand
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.coupling.testreact.external.testinglibrary.userevent.userEvent
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.await
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PinConfigEditorTest {

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object {
        val party = Party(PartyId(""))
        val pin = Pin(id = null)
    }) exercise {
        render(PinConfig(party, pin, emptyList(), {}, StubDispatchFunc()).create(), json("wrapper" to MemoryRouter))
    } verify {
        screen.queryByText("Retire")
            .assertIsEqualTo(null)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object {
        val party = Party(PartyId(""))
        val pin = Pin(id = "excellent id")
    }) exercise {
        render(PinConfig(party, pin, emptyList(), {}, StubDispatchFunc()).create(), json("wrapper" to MemoryRouter))
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
        val actor = userEvent.setup()
    }) {
        render(PinConfig(party, pin, emptyList(), {}, stubDispatcher.func()).create(), json("wrapper" to MemoryRouter))
        actor.type(screen.getByLabelText("Name"), newName).await()
        actor.type(screen.getByLabelText("Icon"), newIcon).await()
    } exercise {
        actor.click(screen.getByText("Save")).await()
    } verify {
        stubDispatcher.commandsDispatched<SavePinCommand>()
            .assertIsEqualTo(listOf(SavePinCommand(party.id, Pin(name = newName, icon = newIcon))))
    }
}
