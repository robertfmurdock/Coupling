package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.dynamo.slack.DynamoSlackRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.Clock
import kotlin.test.Test

class DynamoSlackRepositoryTest {

    @Test
    fun canSaveAndLoadAccessTokenByTeam() = asyncSetup(object {
        val slackTeamAccess = SlackTeamAccess(
            teamId = uuidString(),
            accessToken = uuidString(),
            appId = uuidString(),
            slackUserId = uuidString(),
            slackBotUserId = uuidString(),
        )
        lateinit var repository: DynamoSlackRepository
        val userId = UserId.new()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
    }) {
        repository = DynamoSlackRepository(userId, clock)
    } exercise {
        repository.save(slackTeamAccess)
        repository.get(slackTeamAccess.teamId)
    } verify { result: Record<SlackTeamAccess>? ->
        result.assertIsEqualTo(
            Record(
                data = slackTeamAccess,
                modifyingUserId = userId.value,
                isDeleted = false,
                timestamp = clock.now(),
            ),
        )
    }
}
