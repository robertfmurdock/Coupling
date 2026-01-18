package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.assertNotNull
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlin.test.Test

class PinConfigEditorTest {

    @Test
    fun whenGivenPinIsNotInListWillNotShowDeleteButton() = asyncSetup(object {
        val party = PartyDetails(stubPartyId())
        val pin = stubPin()
    }) exercise {
        render {
            TestRouter {
                PinConfig(
                    party = party,
                    pin = pin,
                    pinList = emptyList(),
                    reload = {},
                    dispatchFunc = DispatchFunc { {} },
                )
            }
        }
    } verify {
        screen.queryByText("Retire")
            .assertIsEqualTo(null)
    }

    @Test
    fun whenGivenPinIsInListWillShowDeleteButton() = asyncSetup(object {
        val party = PartyDetails(stubPartyId())
        val pin = stubPin()
    }) exercise {
        render {
            TestRouter {
                PinConfig(
                    party = party,
                    boost = null,
                    pin = pin,
                    pinList = listOf(pin),
                    reload = {},
                    dispatchFunc = DispatchFunc { {} },
                )
            }
        }
    } verify {
        screen.findByText("Retire")
            .assertNotNull()
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = asyncSetup(object {
        val party = PartyDetails(PartyId("dumb party"))
        val pin = stubPin().copy(name = "", icon = "")
        val newName = "pin new name"
        val newIcon = "pin new icon"

        val stubDispatcher = StubDispatcher()
        val actor = UserEvent.setup()
    }) {
        render {
            TestRouter {
                PinConfig(
                    party = party,
                    boost = null,
                    pin = pin,
                    pinList = emptyList(),
                    reload = {},
                    dispatchFunc = stubDispatcher.func(),
                )
            }
        }
        actor.type(screen.findByLabelText("Name"), newName)
        actor.type(screen.findByLabelText("Icon"), newIcon)
    } exercise {
        act { fireEvent.submit(screen.getByRole("form")) }
    } verify {
        stubDispatcher.receivedActions
            .assertIsEqualTo(listOf(SavePinCommand(party.id, pin.copy(name = newName, icon = newIcon))))
    }
}
