package com.zegreatrob.coupling.repositoryvalidation

import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubTribes
import kotlin.test.Test

abstract class TribeRepositoryValidator {

    abstract suspend fun withRepository(handler: suspend (TribeRepository) -> Unit)

    @Test
    fun saveMultipleThenGetListWillReturnSavedTribes() = testAsync {
        withRepository { repository ->
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
    }

}