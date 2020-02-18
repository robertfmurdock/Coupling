package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import stubTribe
import stubTribes
import kotlin.test.Test

interface TribeRepositoryValidator {

    suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit)

    fun testRepository(block: suspend CoroutineScope.(TribeRepository, User, MagicClock) -> Any?) =
        testAsync {
            val clock = MagicClock()
            withRepository(clock) { repository, user -> block(repository, user, clock) }
        }

    @Test
    fun saveMultipleThenGetListWillReturnSavedTribes() = testRepository { repository, _, _ ->
        setupAsync(object {
            val tribes = stubTribes(3)
        }) {
            tribes.forEach { repository.save(it) }
        } exerciseAsync {
            repository.getTribes()
        } verifyAsync { result ->
            result.takeLast(tribes.size)
                .map { it.data }
                .assertIsEqualTo(tribes)
        }
    }

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedTribes() = testRepository { repository, _, _ ->
        setupAsync(object {
            val tribes = stubTribes(3)
        }) {
            tribes.forEach { repository.save(it) }
        } exerciseAsync {
            tribes.map { repository.getTribe(it.id) }
        } verifyAsync { result ->
            result.takeLast(tribes.size)
                .assertIsEqualTo(tribes)
        }
    }

    @Test
    fun saveWillIncludeModificationInformation() = testRepository { repository, user, clock ->
        setupAsync(object {
            val tribe = stubTribe()
        }) {
            clock.currentTime = DateTime.now().minus(3.days)
            repository.save(tribe)
        } exerciseAsync {
            repository.getTribes()
        } verifyAsync { result ->
            result.first { it.data.id == tribe.id }.apply {
                modifyingUserEmail.assertIsEqualTo(user.email)
                timestamp.assertIsEqualTo(clock.currentTime)
            }
        }
    }

    @Test
    fun deleteWillMakeTribeInaccessible() = testRepository { repository, _, _ ->
        setupAsync(object {
            val tribe = stubTribe()
        }) {
            repository.save(tribe)
        } exerciseAsync {
            repository.delete(tribe.id)
            Pair(
                repository.getTribes(),
                repository.getTribe(tribe.id)
            )
        } verifyAsync { (listResult, getResult) ->
            listResult.find { it.data.id == tribe.id }
                .assertIsEqualTo(null)
            getResult.assertIsEqualTo(null)
        }
    }

}