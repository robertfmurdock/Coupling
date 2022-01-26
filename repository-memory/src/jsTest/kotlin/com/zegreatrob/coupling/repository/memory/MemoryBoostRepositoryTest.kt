package com.zegreatrob.coupling.repository.memory

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class MemoryBoostRepositoryTest {

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = setup(object {
        val userId = "${uuid4()}"
        val repository = MemoryBoostRepository(userId = userId, clock = MagicClock())
    }) exercise {
        repository.get()
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun getSavedBoostWillReturnSuccessfully() = setup(object {
        val userId = "${uuid4()}"
        val repository = MemoryBoostRepository(userId = userId, clock = MagicClock())
        val boost = Boost("${uuid4()}", userId, setOf(TribeId("${uuid4()}"), TribeId("${uuid4()}")))
    }) {
        repository.save(boost)
    } exercise {
        repository.get()
    } verify { result ->
        result?.data.assertIsEqualTo(boost)
    }

    @Test
    fun saveBoostRepeatedlyGetsLatest() = setup(object {
        val userId = "${uuid4()}"
        val repository = MemoryBoostRepository(userId = userId, clock = MagicClock())
        val boost = Boost("${uuid4()}", userId, setOf(TribeId("${uuid4()}"), TribeId("${uuid4()}")))
        val updatedBoost1 = boost.copy(tribeIds = emptySet())
        val updatedBoost2 = updatedBoost1.copy(tribeIds = setOf(TribeId("${uuid4()}")))
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

    @Test
    fun getSavedBoostByTribeIdForBoostFromDifferentUserWillReturnContent() = setup(object {
        val userId = "${uuid4()}"
        val recordBackend = SimpleRecordBackend<Boost>()
        val boostRepository = MemoryBoostRepository(userId, MagicClock(), recordBackend)
        val altBoostRepository = MemoryBoostRepository("${uuid4()}", MagicClock(), recordBackend)
        val tribeId = TribeId("${uuid4()}")
        val boost = Boost("${uuid4()}", userId, setOf(TribeId("${uuid4()}"), tribeId, TribeId("${uuid4()}")))
    }) {
        boostRepository.save(boost)
    } exercise {
        altBoostRepository.getByTribeId(tribeId)
    } verify { result ->
        result?.data.assertIsEqualTo(boost)
    }


    @Test
    fun saveBoostRepeatedlyGetByTribeGetsLatest() = setup(object {
        val userId = "${uuid4()}"
        val repository = MemoryBoostRepository(userId = userId, clock = MagicClock())
        val tribeId = TribeId("${uuid4()}")
        val boost = Boost("${uuid4()}", userId, setOf(tribeId, TribeId("${uuid4()}")))
        val updatedBoost1 = boost.copy(tribeIds = emptySet())
        val updatedBoost2 = updatedBoost1.copy(tribeIds = setOf(tribeId))
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

}
