package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class DynamoDiscordOAuthRepositoryTest {
    @Test
    fun stateExpiresAtItsDeadlineWithoutTtlCleanup() = asyncSetup(object {
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val user = UserId.new()
        val party = stubPartyDetails().id
        val stateHash = uuidString()
        val repository = DynamoDiscordOAuthRepository(clock)
    }) {
        DynamoDiscordRepository(user, clock)
        repository.savePending(stateHash, user, party, "challenge", (clock.now() + 10.minutes).toEpochMilliseconds())
    } exercise {
        clock.currentTime = clock.now() + 10.minutes
        val expired = repository.consume(stateHash, user, party, "challenge")
        clock.currentTime = clock.now() - 1.minutes
        expired to repository.consume(stateHash, user, party, "challenge")
    } verify { result ->
        // Moving time back cannot resurrect an expired record encountered and cleaned up.
        result.assertIsEqualTo(false to false)
    }

    @Test
    fun mismatchedAttemptPreservesPendingStateAndIntegrationReads() = asyncSetup(object {
        val clock = MagicClock()
        val user = UserId.new()
        val party = stubPartyDetails().id
        val stateHash = uuidString()
        val repository = DynamoDiscordOAuthRepository(clock)
        lateinit var integrations: DynamoDiscordRepository
    }) {
        integrations = DynamoDiscordRepository(user, clock)
        repository.savePending(stateHash, user, party, "challenge", (clock.now() + 10.minutes).toEpochMilliseconds())
    } exercise {
        val wrongUser = repository.consume(stateHash, UserId.new(), party, "challenge")
        val integration = integrations.get(party)
        val valid = repository.consume(stateHash, user, party, "challenge")
        Triple(wrongUser, integration, valid)
    } verify { (wrongUser, integration, valid) ->
        wrongUser.assertIsEqualTo(false)
        integration.assertIsEqualTo(null)
        valid.assertIsEqualTo(true)
    }
}
