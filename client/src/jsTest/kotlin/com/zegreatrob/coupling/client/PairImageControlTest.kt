package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.pairassignments.PairSection
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.browser.document
import kotlinx.coroutines.await
import react.create
import kotlin.test.AfterTest
import kotlin.test.Test

class PairImageControlTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun copyPairImageIsNamedAndReachableByKeyboard() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(
            PairSection.create {
                party = PartyDetails(PartyId("clipboard-party"))
                players = emptyList()
                pairAssignments = null
                allowSave = false
                setPairs = {}
                controls = Controls({ {} }) {}
            },
        )
    } exercise {
        actor.tab().await()
    } verify {
        val copy = screen.getByRole("button", RoleOptions(name = "Copy pair image to clipboard"))
        document.activeElement.assertIsEqualTo(copy)
    }
}
