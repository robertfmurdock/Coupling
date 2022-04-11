package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.tribe.with
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
        val saveSpy = SpyData<TribeIdPin, Unit>()
        override suspend fun save(tribeIdPin: TribeIdPin) = saveSpy.spyFunction(tribeIdPin)

        val tribe = Party(PartyId("thing"))
        val pin = Pin("1", "one", "icon 1")

    }) exercise {
        perform(SavePinCommand(tribe.id, pin))
    } verify {
        saveSpy.spyReceivedValues
            .assertIsEqualTo(listOf(tribe.id.with(pin)))
    }

}