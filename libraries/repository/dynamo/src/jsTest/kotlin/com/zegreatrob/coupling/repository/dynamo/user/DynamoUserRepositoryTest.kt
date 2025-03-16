package com.zegreatrob.coupling.repository.dynamo.user

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.dynamo.DynamoUserJsonMapping
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext
import com.zegreatrob.coupling.repository.dynamo.now
import com.zegreatrob.coupling.repository.dynamo.pairs.months
import com.zegreatrob.coupling.repository.dynamo.pairs.years
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotools.types.text.toNotBlankString
import kotlin.js.json
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue
import kotlin.uuid.Uuid

@Suppress("unused")
class DynamoUserRepositoryTest : UserRepositoryValidator<DynamoUserRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoUserRepository>>(sharedSetup = {
        val clock = MagicClock()
        val userId = "${Uuid.random()}".toNotBlankString().getOrThrow()
        val user = UserDetails(userId, "${Uuid.random()}".toNotBlankString().getOrThrow(), emptySet(), uuidString())
        val repository = DynamoUserRepository(userId, clock)
        SharedContextData(repository, clock, user)
    })

    // Performance test - Does not run in CI
    @Test
    @Ignore
    fun canHandleLargeNumberOfRecordRevisionsAndGetLatestOneFast() = asyncSetup(object {
        val userId = "${Uuid.random()}".toNotBlankString().getOrThrow()
        val user = UserDetails(userId, "${Uuid.random()}".toNotBlankString().getOrThrow(), emptySet(), null)
        lateinit var repository: DynamoUserRepository
    }) {
        repository = DynamoUserRepository(userId, Clock.System)
        coroutineScope {
            (1..5000).forEach { number ->
                launch {
                    repository.saveRawRecord(
                        Record(
                            data = user.copy(authorizedPartyIds = setOf(PartyId("party-$number"))),
                            modifyingUserId = null,
                            isDeleted = false,
                            timestamp = now().minus(1.days).plus(number.seconds),
                        ),
                    )
                }
            }
        }
        delay(10)
        repository.save(
            user.copy(authorizedPartyIds = setOf(PartyId("party-infinity"))),
        )
    } exercise {
        measureTimedValue {
            repository.getUsersWithEmail(user.email.toString())
        }
    } verify { (result, duration) ->
        val user = result.first().data
        user.authorizedPartyIds.contains(PartyId("party-infinity"))
            .assertIsEqualTo(true, "Oops, got ${user.authorizedPartyIds}")
        (duration.toDouble(DurationUnit.SECONDS) < 0.1)
            .assertIsEqualTo(true, "Too slow, $duration")
    }

    @Test
    fun getUserRecordsWillReturnAllRecordsForAllUsers() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val initialSaveTime = now().minus(3.days)
                val updatedUser = user.copy(authorizedPartyIds = setOf(PartyId("clone!")))
                val updatedSaveTime = initialSaveTime.plus(2.hours)
                val altUser = stubUserDetails()
            }
        },
    ) {
        clock.currentTime = initialSaveTime
        repository.save(user)
        repository.save(altUser)
        clock.currentTime = updatedSaveTime
        repository.save(updatedUser)
    } exercise {
        repository.getUserRecords()
    } verify { result ->
        result
            .assertContains(Record(user, user.id, false, initialSaveTime))
            .assertContains(Record(altUser, user.id, false, initialSaveTime))
            .assertContains(Record(updatedUser, user.id, false, updatedSaveTime))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val records = listOf(
                    Record(stubUserDetails(), uuidString().toNotBlankString().getOrThrow(), false, now().minus(3.months)),
                    Record(stubUserDetails(), uuidString().toNotBlankString().getOrThrow(), true, now().minus(2.years)),
                )
            }
        },
    ) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getUserRecords()) {
            records.forEach { assertContains(it) }
        }
    }

    @Test
    fun canReadEvenWhenRecordIncludesNullInTheAuthorizedTribeIdList() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context, DynamoUserJsonMapping by context.repository {
                override val clock: MagicClock = context.clock
                override val userId = uuidString().toNotBlankString().getOrThrow()
                val record = Record(stubUserDetails(), uuidString().toNotBlankString().getOrThrow(), false, now().minus(3.months))
            }
        },
    ) {
        DynamoUserRepository.performPutItem(
            record.recordJson().add(
                json(
                    "id" to record.data.id.toString(),
                    "email" to record.data.email.toString(),
                    "stripeCustomerId" to record.data.stripeCustomerId,
                    "authorizedTribeIds" to record.data.authorizedPartyIds.map { it.value.toString() }
                        .plus(null)
                        .toTypedArray(),
                ),
            ),
        )
    } exercise {
        repository.getUserRecords()
    } verify { results ->
        results.assertContains(record)
    }
}

private typealias Context = RepositoryContext<DynamoUserRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoUserRepository(user.id, clock) }()
}
