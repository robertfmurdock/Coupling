package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class RetiredPlayerConfigE2ETest {

    @Test
    fun willShowThePlayerData() = sdkSetup(object : SdkContext() {
        val tribe = Party(PartyId("${randomInt()}-RetiredPlayerConfigE2E"))
        val player = Player("${randomInt()}-RetiredPlayerConfigE2E", name = "${randomInt()}-RetiredPlayerConfigE2E")
    }) {
        sdk.partyRepository.save(tribe)
        sdk.playerRepository.save(tribe.id.with(player))
        sdk.playerRepository.deletePlayer(tribe.id, player.id)
    } exercise {
        RetiredPlayerConfig.goTo(tribe.id, player.id)
    } verify {
        RetiredPlayerConfig.playerNameTextField.attribute("value")
            .assertIsEqualTo(player.name)
    }

}
