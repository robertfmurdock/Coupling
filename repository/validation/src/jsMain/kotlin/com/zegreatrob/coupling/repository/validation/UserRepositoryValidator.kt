package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import com.zegreatrob.coupling.stubmodel.stubTribeId
import kotlin.test.Test
import kotlin.test.fail

interface UserRepositoryValidator {

    suspend fun withRepository(clock: TimeProvider, handler: suspend (UserRepository, User) -> Unit)

    private fun testRepository(
        block: suspend CoroutineScope.(UserRepository, User, MagicClock) -> Any?
    ) = testAsync {
        val clock = MagicClock()
        withRepository(clock) { repository, user -> block(repository, user, clock) }
    }

    @Test
    fun getUserWillNotExplodeWhenUserDoesNotExistInDatabase() = testRepository { repository, user, _ ->
        setupAsync(object {
        }) exerciseAsync {
            repository.getUser()
        } verifyAsync { result ->
            result.assertIsEqualTo(null)
        }
    }

    @Test
    fun saveUserThenGetWillContainAllSavedValues() = testRepository { repository, user, _ ->
        setupAsync(object {
            val updatedUser = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
        }) {
            repository.save(updatedUser)
        } exerciseAsync {
            repository.getUser()
        } verifyAsync { result ->
            result?.data
                .assertIsEqualTo(updatedUser)
        }
    }

    @Test
    fun saveUserThenGetWillIncludeMarkingInformation() = testRepository { repository, user, clock ->
        setupAsync(object {
            val updatedUser = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
        }) {
            clock.currentTime = DateTime.now().plus(10.days)
            repository.save(updatedUser)
        } exerciseAsync {
            repository.getUser()
        } verifyAsync { result: Record<User>? ->
            if (result == null)
                fail()
            result.modifyingUserEmail.assertIsEqualTo(user.email)
            result.timestamp.assertIsEqualTo(clock.currentTime)
        }
    }

    @Test
    fun saveUserRepeatedlyGetsLatest() = testRepository { repository, user, _ ->
        setupAsync(object {
            val updatedUser1 = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
            val updatedUser2 = user.copy(authorizedTribeIds = setOf(stubTribeId()))
        }) {
            repository.save(updatedUser1)
            repository.save(updatedUser2)
        } exerciseAsync {
            repository.getUser()
        } verifyAsync { result ->
            result?.data
                .assertIsEqualTo(updatedUser2)
        }
    }

}
