package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pin.PinSave
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup

import kotlin.test.Test

class SavePinCommandTest {

    @Test
    fun whenPerformedWillSaveThePinInTheRepository() = asyncSetup(object : SavePinCommandDispatcher, PinSave {
        override val pinRepository: PinSave get() = this
        val saveSpy = SpyData<PartyElement<Pin>, Unit>()
        override suspend fun save(partyPin: PartyElement<Pin>) = saveSpy.spyFunction(partyPin)

        val party = Party(PartyId("thing"))
        val pin = Pin("1", "one", "icon 1")

    }) exercise {
        perform(SavePinCommand(party.id, pin))
    } verify {
        saveSpy.spyReceivedValues
            .assertIsEqualTo(listOf(party.id.with(pin)))
    }

}