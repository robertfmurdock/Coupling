package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.ComposeStatisticsAction
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo

@Suppress("unused")
class ComposeStatisticsActionTest {

    companion object : ComposeStatisticsAction.Dispatcher {
        val party = PartyDetails(PartyId("LOL"), PairingRule.LongestTime)

        fun makePlayers(numberOfPlayers: Int) = (1..numberOfPlayers)
            .map { number -> makePlayer("$number") }

        private fun makePlayer(id: String) = Player(id = id, avatarType = null)

        private fun List<CouplingPair>.assertMatch(expected: List<CouplingPair>) {
            assertIsEqualTo(
                expected,
                "------WE EXPECT\n${expected.describe()}\n------RESULTS\n${this.describe()}\n-----END\n",
            )
        }

        private fun List<CouplingPair>.describe() = map { it.map { player -> player.id } }
            .joinToString(", ").let { "[ $it ]" }
    }
}

expect fun loadJsonPartySetup(fileResource: String): PartySetup
expect inline fun <reified T> loadResource(fileResource: String): T

data class PartySetup(val party: PartyDetails, val players: List<Player>, val history: List<PairAssignmentDocument>)
