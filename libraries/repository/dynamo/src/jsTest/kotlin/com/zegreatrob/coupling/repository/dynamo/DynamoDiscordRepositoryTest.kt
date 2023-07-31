package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.DiscordWebhook
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.Clock
import kotlin.test.Test

class DynamoDiscordRepositoryTest {

    @Test
    fun canSaveAndLoadAccessTokenByPartyId() = asyncSetup(object {
        val partyId = stubPartyId()
        val discordTeamAccess = DiscordTeamAccess(
            accessToken = uuidString(),
            refreshToken = uuidString(),
            webhook = DiscordWebhook(
                id = uuidString(),
                token = uuidString(),
            ),
        )
        lateinit var repository: DynamoDiscordRepository
        val userEmail = uuidString()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val partyAccess = partyId.with(discordTeamAccess)
    }) {
        repository = DynamoDiscordRepository.invoke(userEmail, clock)
    } exercise {
        repository.save(partyAccess)
        repository.get(partyId)
    } verify { result: PartyRecord<DiscordTeamAccess>? ->
        result.assertIsEqualTo(
            Record(
                data = partyAccess,
                modifyingUserId = userEmail,
                isDeleted = false,
                timestamp = clock.now(),
            ),
        )
    }
}
