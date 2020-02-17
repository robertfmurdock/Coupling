package com.zegreatrob.coupling.repositoryvalidation

import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import stubTribe
import stubTribes
import kotlin.test.Test

interface TribeRepositoryValidator {

    suspend fun withRepository(handler: suspend (TribeRepository) -> Unit)

    private fun testRepository(block: suspend CoroutineScope.(TribeRepository) -> Any?) = testAsync {
        withRepository { repository -> block(repository) }
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedTribes() = testRepository { repository ->
        setupAsync(object {
            val tribes = stubTribes(3)
        }) {
            tribes.forEach { repository.save(it) }
        } exerciseAsync {
            repository.getTribes()
        } verifyAsync { result ->
            result.takeLast(tribes.size)
                .assertIsEqualTo(tribes)
        }
    }

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedTribes() = testRepository { repository ->
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
    fun deleteWillMakeTribeInaccessible() = testRepository { repository ->
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
            listResult.find { it.id == tribe.id }
                .assertIsEqualTo(null)
            getResult.assertIsEqualTo(null)
        }
    }

}