package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class RetiredPlayerConfigE2ETest {

    @Test
    fun willShowThePlayerData() = sdkSetup(object : SdkContext() {
        val party = Party(PartyId("${randomInt()}-RetiredPlayerConfigE2E"))
        val player = Player(
            "${randomInt()}-RetiredPlayerConfigE2E",
            name = "${randomInt()}-RetiredPlayerConfigE2E",
            avatarType = null,
        )
    }) {
        sdk.partyRepository.save(party)
        sdk.playerRepository.save(party.id.with(player))
        sdk.playerRepository.deletePlayer(party.id, player.id)
    } exercise {
        RetiredPlayerConfig.goTo(party.id, player.id)
    } verify {
        RetiredPlayerConfig.getPlayerNameTextField().attribute("value")
            .assertIsEqualTo(player.name)
    }
}
