package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import stubTribeId
import kotlin.test.Test

interface UserRepositoryValidator {

    suspend fun withRepository(handler: suspend (UserRepository, User) -> Unit)

    private fun testRepository(block: suspend CoroutineScope.(UserRepository, User) -> Any?) = testAsync {
        withRepository { repository, user -> block(repository, user) }
    }

    @Test
    fun saveUserThenGetWillContainAllSavedValues() = testRepository { repository, user ->
        setupAsync(object {
            val updatedUser = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
        }) {
            repository.save(updatedUser)
        } exerciseAsync {
            repository.getUser()
        } verifyAsync { result ->
            result.assertIsEqualTo(updatedUser)
        }
    }

    @Test
    fun saveUserRepeatedlyGetsLatest() = testRepository { repository, user ->
        setupAsync(object {
            val updatedUser1 = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
            val updatedUser2 = user.copy(authorizedTribeIds = setOf(stubTribeId()))
        }) {
            repository.save(updatedUser1)
            repository.save(updatedUser2)
        } exerciseAsync {
            repository.getUser()
        } verifyAsync { result ->
            result.assertIsEqualTo(updatedUser2)
        }
    }

}
