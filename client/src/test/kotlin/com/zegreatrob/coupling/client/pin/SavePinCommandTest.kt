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
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class SavePinCommandTest {

    @Test
    fun whenPerformedWillSaveThePinInTheRepository() = testAsync {
        setupAsync(object : SavePinCommandDispatcher, PinSave {
            override val pinRepository: PinSave get() = this
            val saveSpy = object : Spy<TribeIdPin, Unit> by SpyData() {}.apply { spyWillReturn(Unit) }
            override suspend fun save(tribeIdPin: TribeIdPin) = saveSpy.spyFunction(tribeIdPin)

            val tribe = Tribe(TribeId("thing"))
            val pin = Pin("1", "one", "icon 1")

        }) exerciseAsync {
            SavePinCommand(tribe.id, pin)
                .perform()
        } verifyAsync {
            saveSpy.spyReceivedValues
                .assertIsEqualTo(
                    listOf(tribe.id.with(pin))
                )
        }

    }
}