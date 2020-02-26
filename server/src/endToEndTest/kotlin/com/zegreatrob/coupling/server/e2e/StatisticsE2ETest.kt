package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.test.Test

class StatisticsE2ETest {

    @Test
    fun pageShowsImportantElements() = testAsync {
        val sdk = sdkProvider.await()
        setupAsync(object {
            val tribe = Tribe(TribeId("${randomInt()}-statsE2E"), name = "Funkytown")
            val players = generateSequence { Player(id = "${randomInt()}-statsE2E") }
                .take(6).toList()
        }) {
            sdk.save(tribe)
            players.forEach { player -> sdk.save(tribe.id.with(player)) }
            CouplingLogin.loginProvider
        } exerciseAsync {
            StatisticsPage.goTo(tribe.id)
        } verifyAsync {
            with(StatisticsPage) {
                TribeCard.element.getText().await()
                    .assertIsEqualTo(tribe.name)
                rotationNumber.getText().await()
                    .assertIsEqualTo("5")
                pairReports.count().await()
                    .assertIsEqualTo(15)
            }
        }
    }
}
