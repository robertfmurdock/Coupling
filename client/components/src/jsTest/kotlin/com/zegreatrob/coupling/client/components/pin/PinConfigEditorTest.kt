package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
import com.zegreatrob.coupling.client.components.player.singleRouteRouter
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.act
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.fireEvent
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import react.create
import react.router.RouterProvider
import kotlin.test.Test

class PinConfigEditorTest {

    @Test
    fun whenGivenPinIsNotInListWillNotShowDeleteButton() = setup(object {
        val party = PartyDetails(stubPartyId())
        val pin = stubPin()
    }) exercise {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PinConfig(
                        party = party,
                        pin = pin,
                        pinList = emptyList(),
                        reload = {},
                        dispatchFunc = DispatchFunc { {} },
                    )
                }
            },
        )
    } verify {
        screen.queryByText("Retire")
            .assertIsEqualTo(null)
    }

    @Test
    fun whenGivenPinIsInListWillShowDeleteButton() = setup(object {
        val party = PartyDetails(stubPartyId())
        val pin = stubPin()
    }) exercise {
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PinConfig(
                        party = party,
                        boost = null,
                        pin = pin,
                        pinList = listOf(pin),
                        reload = {},
                        dispatchFunc = DispatchFunc { {} },
                    )
                }
            },
        )
    } verify {
        screen.queryByText("Retire")
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
        render(
            RouterProvider.create {
                router = singleRouteRouter {
                    PinConfig(
                        party = party,
                        boost = null,
                        pin = pin,
                        pinList = emptyList(),
                        reload = {},
                        dispatchFunc = stubDispatcher.func(),
                    )
                }
            },
        )
        actor.type(screen.getByLabelText("Name"), newName)
        actor.type(screen.getByLabelText("Icon"), newIcon)
    } exercise {
        act { fireEvent.submit(screen.getByRole("form")) }
    } verify {
        stubDispatcher.receivedActions
            .assertIsEqualTo(listOf(SavePinCommand(party.id, pin.copy(name = newName, icon = newIcon))))
    }
}
