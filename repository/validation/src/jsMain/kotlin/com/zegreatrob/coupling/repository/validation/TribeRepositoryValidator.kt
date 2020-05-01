package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubTribes
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.async.waitForTest
import kotlin.test.Test

interface TribeRepositoryValidator {

    suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit)

    fun testRepository(block: (TribeRepository, User, MagicClock) -> dynamic) = testAsync {
        val clock = MagicClock()
        withRepository(clock) { repository, user -> waitForTest { block(repository, user, clock) } }
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedTribes() = testRepository { repository, _, _ ->
        asyncSetup(object {
            val tribes = stubTribes(3)
        }) {
            tribes.forEach { repository.save(it) }
        } exercise {
            repository.getTribes()
        } verify { result ->
            result.takeLast(tribes.size)
                .map { it.data }
                .assertIsEqualTo(tribes)
        }
    }

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedTribes() = testRepository { repository, _, _ ->
        asyncSetup(object {
            val tribes = stubTribes(3)
        }) {
            tribes.forEach { repository.save(it) }
        } exercise {
            tribes.map { repository.getTribeRecord(it.id)?.data }
        } verify { result ->
            result.takeLast(tribes.size)
                .assertIsEqualTo(tribes)
        }
    }

    @Test
    fun saveWillIncludeModificationInformation() = testRepository { repository, user, clock ->
        asyncSetup(object {
            val tribe = stubTribe()
        }) {
            clock.currentTime = DateTime.now().minus(3.days)
            repository.save(tribe)
        } exercise {
            repository.getTribes()
        } verify { result ->
            result.first { it.data.id == tribe.id }.apply {
                modifyingUserId.assertIsEqualTo(user.email)
                timestamp.assertIsEqualTo(clock.currentTime)
            }
        }
    }

    @Test
    fun deleteWillMakeTribeInaccessible() = testRepository { repository, _, _ ->
        asyncSetup(object {
            val tribe = stubTribe()
        }) {
            repository.save(tribe)
        } exercise {
            repository.delete(tribe.id)
            Pair(
                repository.getTribes(),
                repository.getTribeRecord(tribe.id)?.data
            )
        } verify { (listResult, getResult) ->
            listResult.find { it.data.id == tribe.id }
                .assertIsEqualTo(null)
            getResult.assertIsEqualTo(null)
        }
    }

}