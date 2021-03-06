package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class StatisticsE2ETest {
    @Test
    fun pageShowsImportantElements() = sdkSetup(object : SdkContext() {
        val tribe = Tribe(TribeId("${randomInt()}-statsE2E"), name = "Funkytown")
        val players = generateSequence { Player(id = "${randomInt()}-statsE2E") }
            .take(6).toList()
    }) {
        sdk.save(tribe)
        players.forEach { player -> sdk.save(tribe.id.with(player)) }
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
