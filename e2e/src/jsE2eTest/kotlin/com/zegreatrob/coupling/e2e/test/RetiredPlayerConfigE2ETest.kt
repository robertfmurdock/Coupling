package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class RetiredPlayerConfigE2ETest {

    @Test
    fun willShowThePlayerData() = sdkSetup(object : SdkContext() {
        val party = PartyDetails(PartyId("${randomInt()}-RetiredPlayerConfigE2E"))
        val player = defaultPlayer.copy(
            id = "${randomInt()}-RetiredPlayerConfigE2E",
            name = "${randomInt()}-RetiredPlayerConfigE2E",
        )
    }) {
        sdk.fire(SavePartyCommand(party))
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(DeletePlayerCommand(party.id, player.id))
    } exercise {
        RetiredPlayerConfig.goTo(party.id, player.id)
    } verify {
        RetiredPlayerConfig.getPlayerNameTextField().attribute("value")
            .assertIsEqualTo(player.name)
    }
}
