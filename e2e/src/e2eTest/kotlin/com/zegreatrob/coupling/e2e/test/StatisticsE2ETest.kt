package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class StatisticsE2ETest {
    @Test
    fun pageShowsImportantElements() = sdkSetup(object : SdkContext() {
        val party = Party(PartyId("${randomInt()}-statsE2E"), name = "Funkytown")
        val players = generateSequence { Player(id = "${randomInt()}-statsE2E") }
            .take(6).toList()
    }) {
        sdk.partyRepository.save(party)
        players.forEach { player -> sdk.playerRepository.save(party.id.with(player)) }
    } exercise {
        StatisticsPage.goTo(party.id)
    } verify {
        with(StatisticsPage) {
            PartyCard.element().text()
                .assertIsEqualTo(party.name)
            rotationNumber.text()
                .assertIsEqualTo("5")
            pairReport.count()
                .assertIsEqualTo(15)
        }
    }
}
