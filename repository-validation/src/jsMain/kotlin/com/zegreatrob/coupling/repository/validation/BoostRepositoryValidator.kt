package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.BoostDelete
import com.zegreatrob.coupling.repository.BoostGet
import com.zegreatrob.coupling.repository.BoostSave
import com.zegreatrob.coupling.repository.ExtendedBoostRepository
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

interface BoostRepositoryValidator<R, SC : SharedContext<R>> where  R : BoostGet, R : BoostSave, R : BoostDelete {

    val repositorySetup: TestTemplate<SC>
    suspend fun buildRepository(user: User, clock: MagicClock): R

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = repositorySetup {
        repository.delete()
    } exercise {
        repository.get()
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun getSavedBoostWillReturnSuccessfully() = repositorySetup({ sharedContext ->
        object : SharedContext<R> by sharedContext {
            val boost by lazy { Boost(user.id, setOf(TribeId("${uuid4()}"), TribeId("${uuid4()}"))) }
        }
    }) {
        repository.save(boost)
    } exercise {
        repository.get()
    } verify { result ->
        result?.data.assertIsEqualTo(boost)
    }

    @Test
    fun deleteWillMakeBoostNotRecoverableThroughGet() = repositorySetup {
        repository.save(Boost(user.id, setOf(TribeId("${uuid4()}"), TribeId("${uuid4()}"))))
        repository.delete()
    } exercise {
        repository.get()
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun saveBoostRepeatedlyGetsLatest() = repositorySetup({ parent: SharedContext<R> ->
        object : SharedContext<R> by parent {
            val boost = Boost(user.id, setOf(TribeId("${uuid4()}"), TribeId("${uuid4()}")))
            val updatedBoost1 = boost.copy(tribeIds = emptySet())
            val updatedBoost2 = updatedBoost1.copy(tribeIds = setOf(TribeId("${uuid4()}")))
        }
    }) {
        with(repository) {
            save(boost)
            save(updatedBoost1)
            save(updatedBoost2)
        }
    } exercise {
        repository.get()
    } verify { result ->
        result?.data
            .assertIsEqualTo(updatedBoost2)
    }

}

interface ExtendedBoostRepositoryValidator<R : ExtendedBoostRepository, SC : SharedContext<R>> :
    BoostRepositoryValidator<R, SC> {

    @Test
    fun saveBoostRepeatedlyGetByTribeGetsLatest() = repositorySetup({ sharedContext ->
        object : SharedContext<R> by sharedContext {
            val tribeId = TribeId("${uuid4()}")
            val boost = Boost(user.id, setOf(tribeId, TribeId("${uuid4()}")))
            val updatedBoost1 = boost.copy(tribeIds = emptySet())
            val updatedBoost2 = updatedBoost1.copy(tribeIds = setOf(tribeId))
        }
    }) {
        with(repository) {
            save(boost)
            save(updatedBoost1)
            save(updatedBoost2)
        }
    } exercise {
        repository.getByTribeId(tribeId)
    } verify { result ->
        result?.data
            .assertIsEqualTo(updatedBoost2)
    }

    @Test
    fun getSavedBoostByTribeIdForBoostFromDifferentUserWillReturnContent() = repositorySetup({ sharedContext ->
        val altRepository = buildRepository(stubUser(), sharedContext.clock)
        object : SharedContext<R> by sharedContext {
            val altRepository = altRepository
            val tribeId = TribeId("${uuid4()}")
            val boost = Boost(user.id, setOf(TribeId("${uuid4()}"), tribeId, TribeId("${uuid4()}")))
        }
    }) {
        repository.save(boost)
    } exercise {
        altRepository.getByTribeId(tribeId)
    } verify { result ->
        result?.data.assertIsEqualTo(boost)
    }

    @Test
    fun getSavedBoostByTribeIdForBoostRemovedBoostWillReturnNull() = repositorySetup({ sharedContext ->
        object : SharedContext<R> by sharedContext {
            val tribeId = TribeId("${uuid4()}")
            val boost = Boost(user.id, setOf(TribeId("${uuid4()}"), tribeId, TribeId("${uuid4()}")))
        }
    }) {
        repository.save(boost)
        repository.save(boost.copy(tribeIds = boost.tribeIds.minus(tribeId)))
    } exercise {
        repository.getByTribeId(tribeId)
    } verify { result ->
        result?.data.assertIsEqualTo(null)
    }

}