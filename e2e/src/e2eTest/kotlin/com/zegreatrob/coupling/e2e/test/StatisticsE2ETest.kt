package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class StatisticsE2ETest {
    @Test
    fun pageShowsImportantElements() = sdkSetup(object : SdkContext() {
        val tribe = Party(PartyId("${randomInt()}-statsE2E"), name = "Funkytown")
        val players = generateSequence { Player(id = "${randomInt()}-statsE2E") }
            .take(6).toList()
    }) {
        sdk.tribeRepository.save(tribe)
        players.forEach { player -> sdk.playerRepository.save(tribe.id.with(player)) }
    } exercise {
        StatisticsPage.goTo(tribe.id)
    } verify {
        with(StatisticsPage) {
            TribeCard.element().text()
                .assertIsEqualTo(tribe.name)
            rotationNumber.text()
                .assertIsEqualTo("5")
            pairReport.count()
                .assertIsEqualTo(15)
        }
    }
}
