package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SavePinCommandTest {

    @Test
    fun whenPerformedWillSaveThePinInTheRepository() = asyncSetup(object : SavePinCommand.Dispatcher {
        val saveSpy = SpyData<Pin, Unit>()
        override suspend fun perform(command: SavePinCommand) = saveSpy.spyFunction(command.updatedPin)
        val party = Party(PartyId("thing"))
        val pin = Pin("1", "one", "icon 1")
    }) exercise {
        perform(SavePinCommand(party.id, pin))
    } verify {
        saveSpy.spyReceivedValues
            .assertIsEqualTo(listOf(pin))
    }
}
