package com.zegreatrob.coupling.client.pin

import Spy
import SpyData
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pin.PinSave
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync2
import kotlin.test.Test

class SavePinCommandTest {

    @Test
    fun whenPerformedWillSaveThePinInTheRepository() = setupAsync2(object : SavePinCommandDispatcher, PinSave {
        override val pinRepository: PinSave get() = this
        val saveSpy = object : Spy<TribeIdPin, Unit> by SpyData() {}.apply { spyWillReturn(Unit) }
        override suspend fun save(tribeIdPin: TribeIdPin) = saveSpy.spyFunction(tribeIdPin)

        val tribe = Tribe(TribeId("thing"))
        val pin = Pin("1", "one", "icon 1")

    }) exercise {
        SavePinCommand(tribe.id, pin)
            .perform()
    } verify {
        saveSpy.spyReceivedValues
            .assertIsEqualTo(listOf(tribe.id.with(pin)))
    }

}