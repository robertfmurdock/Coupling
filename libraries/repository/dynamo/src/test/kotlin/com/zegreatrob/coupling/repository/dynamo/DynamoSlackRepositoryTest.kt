package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.repository.dynamo.slack.DynamoSlackRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import korlibs.time.DateTime
import kotlin.test.Test

class DynamoSlackRepositoryTest {

    @Test
    fun canSaveAndLoadAccessTokenByTeam() = asyncSetup(object {
        val slackTeamAccess = SlackTeamAccess(
            teamId = uuidString(),
            accessToken = uuidString(),
            appId = uuidString(),
            slackUserId = uuidString(),
        )
        lateinit var repository: DynamoSlackRepository
        val userEmail = uuidString()
        val clock = MagicClock().apply { currentTime = DateTime.now() }
    }) {
        repository = DynamoSlackRepository(userEmail, clock)
    } exercise {
        repository.save(slackTeamAccess)
        repository.get(slackTeamAccess.teamId)
    } verify { result: Record<SlackTeamAccess>? ->
        result.assertIsEqualTo(
            Record(
                data = slackTeamAccess,
                modifyingUserId = userEmail,
                isDeleted = false,
                timestamp = clock.now(),
            ),
        )
    }
}
