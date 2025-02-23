package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class PartyContributorQueryTest {

    @Test
    fun whenContributorIsNotInPlayerSetWillHaveNoDetails() = asyncSetup(object : PartyContributorQuery.Dispatcher {
        val partyId = stubPartyId()
        val email = "jimmy@james.jim"
        val contributions = listOf(stubContribution().copy(participantEmails = setOf(email)))
        override val playerRepository = PlayerListGet { emptyList() }
    }) exercise {
        perform(PartyContributorQuery(partyId, contributions))
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                Contributor(
                    email = email,
                    playerId = null,
                    details = null,
                ),
            ),
        )
    }
}
