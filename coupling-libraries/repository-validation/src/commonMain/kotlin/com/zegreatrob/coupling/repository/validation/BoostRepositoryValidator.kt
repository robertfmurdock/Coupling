
package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.BoostDelete
import com.zegreatrob.coupling.repository.BoostGet
import com.zegreatrob.coupling.repository.BoostSave
import com.zegreatrob.coupling.repository.ExtendedBoostRepository
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface BoostRepositoryValidator<R, SC : SharedContext<R>> where  R : BoostGet, R : BoostSave, R : BoostDelete {

    val repositorySetup: TestTemplate<SC>
    suspend fun buildRepository(user: User, clock: MagicClock): R

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = repositorySetup {
    } exercise {
        repository.delete()
    } verifyWithWait {
        repository.get()
            .assertIsEqualTo(null)
    }

    @Test
    fun getSavedBoostWillReturnSuccessfully() = repositorySetup.with({ sharedContext ->
        object : SharedContext<R> by sharedContext {
            val boost by lazy { Boost(user.id, setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))) }
        }
    }) exercise {
        repository.save(boost)
    } verifyWithWait {
        repository.get()?.data
            .assertIsEqualTo(boost)
    }

    @Test
    fun deleteWillMakeBoostNotRecoverableThroughGet() = repositorySetup {
    } exercise {
        repository.save(Boost(user.id, setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))))
        repository.delete()
    } verifyWithWait {
        repository.get()
            .assertIsEqualTo(null)
    }

    @Test
    fun saveBoostRepeatedlyGetsLatest() = repositorySetup.with({ parent: SharedContext<R> ->
        object : SharedContext<R> by parent {
            val boost = Boost(user.id, setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}")))
            val updatedBoost1 = boost.copy(partyIds = emptySet())
            val updatedBoost2 = updatedBoost1.copy(partyIds = setOf(PartyId("${uuid4()}")))
        }
    }) exercise {
        with(repository) {
            save(boost)
            save(updatedBoost1)
            save(updatedBoost2)
        }
    } verifyWithWait {
        repository.get()?.data
            .assertIsEqualTo(updatedBoost2)
    }

}

@ExperimentalTime
interface ExtendedBoostRepositoryValidator<R : ExtendedBoostRepository, SC : SharedContext<R>> :
    BoostRepositoryValidator<R, SC> {

    @Test
    fun saveBoostRepeatedlyGetByTribeGetsLatest() = repositorySetup.with({ sharedContext ->
        object : SharedContext<R> by sharedContext {
            val tribeId = PartyId("${uuid4()}")
            val boost = Boost(user.id, setOf(tribeId, PartyId("${uuid4()}")))
            val updatedBoost1 = boost.copy(partyIds = emptySet())
            val updatedBoost2 = updatedBoost1.copy(partyIds = setOf(tribeId))
        }
    }) exercise {
        with(repository) {
            save(boost)
            save(updatedBoost1)
            save(updatedBoost2)
        }
    } verifyWithWait {
        repository.getByPartyId(tribeId)?.data
            .assertIsEqualTo(updatedBoost2)
    }

    @Test
    fun getSavedBoostByTribeIdForBoostFromDifferentUserWillReturnContent() = repositorySetup.with({ sharedContext ->
        val altRepository = buildRepository(stubUser(), sharedContext.clock)
        object : SharedContext<R> by sharedContext {
            val altRepository = altRepository
            val tribeId = PartyId("${uuid4()}")
            val boost = Boost(user.id, setOf(PartyId("${uuid4()}"), tribeId, PartyId("${uuid4()}")))
        }
    }) {

    } exercise {
        repository.save(boost)
    } verifyWithWait {
        altRepository.getByPartyId(tribeId)?.data
            .assertIsEqualTo(boost)
    }

    @Test
    fun getSavedBoostByTribeIdForBoostRemovedBoostWillReturnNull() = repositorySetup.with({ sharedContext ->
        object : SharedContext<R> by sharedContext {
            val tribeId = PartyId("${uuid4()}")
            val boost = Boost(user.id, setOf(PartyId("${uuid4()}"), tribeId, PartyId("${uuid4()}")))
        }
    }) exercise {
        repository.save(boost)
        repository.save(boost.copy(partyIds = boost.partyIds.minus(tribeId)))
    } verifyWithWait {
        repository.getByPartyId(tribeId)?.data
            .assertIsEqualTo(null)
    }

}