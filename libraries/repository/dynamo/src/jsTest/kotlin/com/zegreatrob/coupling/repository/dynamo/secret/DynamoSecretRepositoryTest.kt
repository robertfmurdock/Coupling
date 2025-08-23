package com.zegreatrob.coupling.repository.dynamo.secret

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.dynamo.now
import com.zegreatrob.coupling.repository.dynamo.pairs.months
import com.zegreatrob.coupling.repository.dynamo.pairs.years
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubSecret
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Suppress("unused")
class DynamoSecretRepositoryTest {

    @Test
    fun canSaveAndGetSecrets() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoSecretRepository
        val secrets = listOf(
            stubSecret(),
            stubSecret(),
            stubSecret(),
        )
    }) {
        repository = DynamoSecretRepository(UserId.new(), MagicClock())
    } exercise {
        partyId.with(secrets).forEach { repository.save(it) }
    } verifyWithWait {
        repository.getSecrets(partyId)
            .map { it.data.element }
            .assertIsEqualTo(secrets)
    }

    @Test
    fun saveThenDeleteWillNotShowThatSecret() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoSecretRepository
        val secrets = listOf(
            stubSecret(),
            stubSecret(),
            stubSecret(),
        )
    }) {
        repository = DynamoSecretRepository(UserId.new(), MagicClock())
    } exercise {
        partyId.with(secrets).forEach {
            repository.save(it)
        }
        repository.deleteSecret(partyId, secrets[1].id)
    } verifyWithWait {
        repository.getSecrets(partyId).map { it.data.element }
            .assertContains(secrets[0])
            .assertContains(secrets[2])
            ?.size
            .assertIsEqualTo(2)
    }

    @Test
    fun deleteWillFailWhenSecretDoesNotExist() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoSecretRepository
    }) {
        repository = DynamoSecretRepository(UserId.new(), MagicClock())
    } exercise {
        repository.deleteSecret(partyId, SecretId.new())
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun givenNoSecretsWillReturnEmptyList() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoSecretRepository
    }) {
        repository = DynamoSecretRepository(UserId.new(), MagicClock())
    } exercise {
        repository.getSecrets(partyId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun savedSecretsIncludeModificationDateAndUsername() = asyncSetup(object {
        val clock = MagicClock()
        val user = stubUserDetails()
        val partyId = stubPartyId()
        lateinit var repository: DynamoSecretRepository
        val secret = stubSecret()
    }) {
        repository = DynamoSecretRepository(user.id, clock)
    } exercise {
        clock.currentTime = now().plus(4.hours)
        repository.save(partyId.with(secret))
    } verifyWithWait {
        val result = repository.getSecrets(partyId)
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsEqualTo(clock.currentTime)
            modifyingUserId.assertIsEqualTo(user.id.value)
        }
    }

    @Test
    fun getSecretRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup(object {
        val partyId = stubPartyId()
        val clock = MagicClock()
        val user = stubUserDetails()
        lateinit var repository: DynamoSecretRepository
        val secret = stubSecret()
        val initialSaveTime = now().minus(3.days)
        val updatedSecret = secret
        val updatedSaveTime = initialSaveTime.plus(2.hours)
        val updatedSaveTime2 = initialSaveTime.plus(4.hours)
    }) {
        repository = DynamoSecretRepository(user.id, clock)
    } exercise {
        clock.currentTime = initialSaveTime
        repository.save(partyId.with(secret))
        clock.currentTime = updatedSaveTime
        repository.save(partyId.with(updatedSecret))
        clock.currentTime = updatedSaveTime2
        repository.deleteSecret(partyId, secret.id)
    } verifyWithWait {
        repository.getSecretRecords(partyId)
            .assertContains(Record(partyId.with(secret), user.id.value, false, initialSaveTime))
            .assertContains(Record(partyId.with(updatedSecret), user.id.value, false, updatedSaveTime))
            .assertContains(Record(partyId.with(updatedSecret), user.id.value, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoSecretRepository
        val records = listOf(
            partyRecord(partyId, stubSecret(), uuidString().toNotBlankString().getOrThrow(), false, now().minus(3.months)),
            partyRecord(partyId, stubSecret(), uuidString().toNotBlankString().getOrThrow(), true, now().minus(2.years)),
        )
    }) {
        repository = DynamoSecretRepository(UserId.new(), MagicClock())
    } exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verifyWithWait {
        val loadedRecords = repository.getSecretRecords(partyId)
        records.forEach { loadedRecords.assertContains(it) }
    }
}
