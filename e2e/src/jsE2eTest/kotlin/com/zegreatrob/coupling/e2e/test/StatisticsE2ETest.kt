package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class StatisticsE2ETest {
    @Test
    fun pageShowsImportantElements() = sdkSetup(object : SdkContext() {
        val party = PartyDetails(PartyId("${randomInt()}-statsE2E"), name = "Funkytown")
        val players = generateSequence {
            defaultPlayer.copy(id = PlayerId("${randomInt()}-statsE2E".toNotBlankString().getOrThrow()))
        }
            .take(6).toList()
    }) {
        sdk.fire(SavePartyCommand(party))
        players.forEach { player -> sdk.fire(SavePlayerCommand(party.id, player)) }
    } exercise {
        StatisticsPage.goTo(party.id)
    } verify {
        with(StatisticsPage) {
            element.text()
                .assertIsEqualTo(party.name)
            rotationNumber().text()
                .assertIsEqualTo("5")
            pairReportsCount()
                .assertIsEqualTo(15)
        }
    }

    private suspend fun StatisticsPage.pairReportsCount(): Int = runCatching { pairReports().count() }
        .getOrElse {
            runCatching { pairReports().count() }
                .getOrElse { pairReports().count() }
        }
}
