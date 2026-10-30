package com.zegreatrob.coupling.repository.dynamo.user

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class DynamoUserConnectionsTest {

    @Test
    fun redemptionConsumesSecretAndWritesBothUsers() = asyncSetup(Fixture()) {
        prepare()
    } exercise {
        callerRepository.connectUser(caller, pendingIssuer, secret)
    } verify {
        it.assertIsEqualTo(true)
        issuerRepository.getUser()?.data.assertIsEqualTo(issuer.copy(connectedEmails = setOf(caller.email)))
        callerRepository.getUser()?.data.assertIsEqualTo(caller.copy(connectedEmails = setOf(issuer.email)))
        callerRepository.getUsersWithEmail(issuer.email).single().data
            .assertIsEqualTo(issuerRepository.getUser()!!.data)
        issuerRepository.getUserRecords().any { it.data == issuer.copy(connectedEmails = setOf(caller.email)) }
            .assertIsEqualTo(true)
    }

    @Test
    fun usedTokenRejectsBothRelationshipWrites() = asyncSetup(Fixture()) {
        prepare()
        callerRepository.connectUser(caller, pendingIssuer, secret).assertIsEqualTo(true)
        clock.currentTime = clock.now() + 1.milliseconds
    } exercise {
        otherRepository.connectUser(other, pendingIssuer, secret)
    } verify {
        it.assertIsEqualTo(false)
        issuerRepository.getUser()?.data.assertIsEqualTo(issuer.copy(connectedEmails = setOf(caller.email)))
        callerRepository.getUser()?.data.assertIsEqualTo(caller.copy(connectedEmails = setOf(issuer.email)))
        otherRepository.getUser()?.data.assertIsEqualTo(other)
    }

    @Test
    fun restoringOldUserSnapshotDoesNotMakeTokenReusable() = asyncSetup(Fixture()) {
        prepare()
        callerRepository.connectUser(caller, pendingIssuer, secret).assertIsEqualTo(true)
        clock.currentTime = clock.now() + 1.milliseconds
        issuerRepository.save(pendingIssuer)
    } exercise {
        otherRepository.connectUser(other, issuerRepository.getUser()!!.data, secret)
    } verify {
        it.assertIsEqualTo(false)
        issuerRepository.getUser()?.data.assertIsEqualTo(pendingIssuer)
        otherRepository.getUser()?.data.assertIsEqualTo(other)
    }

    @Test
    fun existingPendingTokenCanBeRedeemedOnlyOnce() = asyncSetup(Fixture()) {
        issuerRepository = DynamoUserRepository(issuer.id, clock)
        callerRepository = DynamoUserRepository(caller.id, clock)
        issuerRepository.saveRawRecord(Record(pendingIssuer, issuer.id.value, false, clock.now()))
        callerRepository.save(caller)
        clock.currentTime = clock.now() + 1.milliseconds
    } exercise {
        callerRepository.connectUser(caller, pendingIssuer, secret) to callerRepository.connectUser(caller, pendingIssuer, secret)
    } verify { result ->
        result.assertIsEqualTo(true to false)
        issuerRepository.getUser()?.data.assertIsEqualTo(issuer.copy(connectedEmails = setOf(caller.email)))
        callerRepository.getUser()?.data.assertIsEqualTo(caller.copy(connectedEmails = setOf(issuer.email)))
    }

    private class Fixture {
        val clock = MagicClock().apply { currentTime = Instant.parse("2026-10-30T12:00:00Z") }
        val issuer = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val caller = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val other = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val secret = SecretId.new()
        val pendingIssuer = issuer.copy(connectSecretId = secret)
        lateinit var issuerRepository: DynamoUserRepository
        lateinit var callerRepository: DynamoUserRepository
        lateinit var otherRepository: DynamoUserRepository

        suspend fun prepare() {
            issuerRepository = DynamoUserRepository(issuer.id, clock)
            callerRepository = DynamoUserRepository(caller.id, clock)
            otherRepository = DynamoUserRepository(other.id, clock)
            issuerRepository.save(pendingIssuer)
            callerRepository.save(caller)
            otherRepository.save(other)
            clock.currentTime = clock.now() + 1.milliseconds
        }
    }
}
