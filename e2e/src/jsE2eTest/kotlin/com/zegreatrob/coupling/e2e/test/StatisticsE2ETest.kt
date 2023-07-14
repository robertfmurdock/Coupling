package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class StatisticsE2ETest {
    @Test
    fun pageShowsImportantElements() = sdkSetup(object : SdkContext() {
        val party = PartyDetails(PartyId("${randomInt()}-statsE2E"), name = "Funkytown")
        val players = generateSequence { Player(id = "${randomInt()}-statsE2E", avatarType = null) }
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
            pairReports()
                .count()
                .assertIsEqualTo(15)
        }
    }
}
